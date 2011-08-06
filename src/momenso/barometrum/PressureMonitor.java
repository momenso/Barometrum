package momenso.barometrum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import momenso.barometrum.ReadingsData.PressureMode;
import momenso.barometrum.gui.BlockView;
import momenso.barometrum.gui.ChartView;
import momenso.barometrum.gui.CustomTextView;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;


public class PressureMonitor extends Activity 
	implements Observer
{
	private ReadingsData pressureData;
	private XYSeries pressureSeries;
	private Altimeter altimeter;
	private Barometer barometer;
	private Preferences preferences;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        barometer = new Barometer(getApplicationContext());
        barometer.addObserver(this);
        altimeter = new Altimeter(getApplicationContext());
        altimeter.addObserver(this);
        
        pressureData = new ReadingsData();
        preferences = new Preferences(getApplicationContext());
        pressureData.setMode(preferences.getPressureMode(), altimeter.getAltitude());

        // initialize pressure reading font
        CustomTextView currentReading = (CustomTextView) findViewById(R.id.currentReading);
        Typeface font = Typeface.createFromAsset(getAssets(), "DS-DIGIB.TTF");
        currentReading.setTypeface(font);
        currentReading.setTextColor(Color.WHITE);
        
        Typeface standardFont = Typeface.createFromAsset(getAssets(), "ProFontWindows.ttf");
        BlockView maxReading = (BlockView) findViewById(R.id.maximumReading);
        maxReading.setTypeface(standardFont);
        maxReading.setLabelWidth(8);
        BlockView minReading = (BlockView) findViewById(R.id.minimumReading);
        minReading.setTypeface(standardFont);
        minReading.setLabelWidth(8);
        BlockView altitudeReading = (BlockView) findViewById(R.id.altitudeReading);
        altitudeReading.setTypeface(standardFont);
        altitudeReading.setText(String.format("%.0f", altimeter.getAltitude()));
        altitudeReading.setLabelWidth(8);
        
        loadReadings();
        initializeGraph();
    }
        
    @Override
	protected void onPause() {
		super.onPause();
		
		altimeter.disableGPS();
		barometer.unregisterPressureSensor();
		
		saveReadings();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerSensor();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveReadings();
		
		super.onSaveInstanceState(outState);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu_options, menu);
    	
    	PressureMode mode = preferences.getPressureMode();
    	MenuItem item;
    	switch (mode) {
    		case BAROMETRIC:
    			item = menu.findItem(R.id.Barometric);
    			item.setChecked(true);
    			break;
    			
    		case MSLP:
    			item = menu.findItem(R.id.MSLP);
    			item.setChecked(true);
    			break;
    	}
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    			
    		case R.id.Barometric:
    			preferences.setPressureMode(PressureMode.BAROMETRIC);
    			pressureData.setMode(PressureMode.BAROMETRIC);
    			item.setChecked(true);
    			return true;
    			
    		case R.id.MSLP:
    			preferences.setPressureMode(PressureMode.MSLP);
    			pressureData.setMode(PressureMode.MSLP, altimeter.getAltitude());
    			item.setChecked(true);
    			return true;

    		case R.id.itemAltimeter:
    			if (altimeter.switchGPSSensor()) {
    				item.setTitle(R.string.altimeterDisable);
    			} else {
    				item.setTitle(R.string.altimeterEnable);
    			}
    			return true;
    			
    		case R.id.itemMenuClear:
        		pressureData.clear();
    			return true;

    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void saveReadings() {

    	barometer.unregisterPressureSensor();
    	
    	try {
    		persistReadings(pressureData.get(), "readings");
    		persistReadings(pressureData.getHistory(), "history");
		} catch (IOException e) {
			/*AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Save");
    		alertDialog.setMessage("Failed to save readings: " + e.getLocalizedMessage());
    		alertDialog.show();*/
		} finally {
			barometer.registerPressureSensor();
		}
    }
    
    private void persistReadings(List<PressureDataPoint> data, String fileName) throws IOException {
		
    	FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		
		for(PressureDataPoint n : data) {
			os.writeObject(n);
		}
    }
    
    private void loadReadings() {
    	
    	barometer.unregisterPressureSensor();
    	
    	try {
    		
    		List<PressureDataPoint> readings = restoreReadings("readings");
    		this.pressureData.set(readings);
    		
    		List<PressureDataPoint> history = restoreReadings("history");
    		this.pressureData.setHistory(history);
    		
    	} catch (Exception e) {
    		/*AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Load");
    		alertDialog.setMessage("Failed to load readings: " + e.getLocalizedMessage());
    		alertDialog.show();*/
		} finally {
			//registerPressureSensor();
    		updateGraph();
		}
    }
    
    private List<PressureDataPoint> restoreReadings(String fileName) throws IOException, ClassNotFoundException {
    	
    	List<PressureDataPoint> data = new ArrayList<PressureDataPoint>();
    	
    	try {
    		FileInputStream fis = openFileInput(fileName);
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item;
    		while ((item = is.readObject()) != null) {
    			if (item instanceof PressureDataPoint)
    			data.add((PressureDataPoint)item);
    		}
    	} catch (EOFException ex) {
    		// happens at the end of the file
    	}

		return data;
    }
    
    private void registerSensor() {
	    barometer.registerPressureSensor();
    	//loadReadings();
	    
	    /*final BlockView altimeter = (BlockView)this.findViewById(R.id.altitudeReading);
	    altimeter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (switchGPSSensor()) {
			    	altimeter.setText("...");
			    } else {
			    	altimeter.setText("Altimeter\nDisabled");
			    }
			}
		});*/
	    
	    final CustomTextView barometerReading = (CustomTextView)this.findViewById(R.id.currentReading);
	    barometerReading.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				barometer.switchPressureSensor();
			}
		});
    }
    
    private void initializeGraph() {
    	XYPlot plot = (XYPlot)this.findViewById(R.id.mySimpleXYPlot);
    	if (plot != null) {
    		plot.setTicksPerRangeLabel(3);
    		plot.disableAllMarkup();
    		plot.setDomainLabel("");
    		plot.setRangeLabel("");
    	} else {
    		AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Real-time plot");
    		alertDialog.setMessage("Failed to initialize real-time ploting.");
    		alertDialog.show();
    	}
    }
    
    private void updateGraph()
    {    	
    	XYPlot plot = (XYPlot)this.findViewById(R.id.mySimpleXYPlot);
    	if (plot != null)
    	{
    		plot.removeSeries(pressureSeries);
    		pressureSeries = new SimpleXYSeries(
			        pressureData.getPressure(),
			        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
					"Air Pressure");
			LineAndPointFormatter pressureLineFormat = 
				new LineAndPointFormatter(
			            Color.GREEN,          // line color
			            Color.GREEN,          // point color
			            Color.TRANSPARENT);   // fill color (optional)
			plot.addSeries(pressureSeries, pressureLineFormat);
			
			plot.setRangeBoundaries(
					pressureData.getMinimum() - 0.1, 
					pressureData.getMaximum() + 0.1, 
					BoundaryMode.FIXED);

			plot.redraw();
    	}
    	
    	// update History graph
    	ChartView historyChart = (ChartView)this.findViewById(R.id.historyChart);
    	if (historyChart != null) {
    		historyChart.updateData(pressureData.getHistory());
    	}
    	
		//Log.v("updateGraph", "time=" + (System.currentTimeMillis() - start));
    }

	public void update(Observable observable, Object data) {
		if (observable.getClass() == Altimeter.class) {
			
			float altitude = (Float)data;
			pressureData.setCurrentElevation(altitude);
			
			BlockView altitudeText = (BlockView)findViewById(R.id.altitudeReading);
			altitudeText.setText(String.format("%.0fm",altitude));
			
		} else if (observable.getClass() == Barometer.class) {
			
			float pressure = (Float)data;
			pressureData.add(pressure);
			
			BlockView minimumValueText = (BlockView) findViewById(R.id.minimumReading);
	        minimumValueText.setText(String.format("%.2f", pressureData.getMinimum()));
	        BlockView maximumValueText = (BlockView) findViewById(R.id.maximumReading);
	    	maximumValueText.setText(String.format("%.2f", pressureData.getMaximum()));

	    	TextView currentValueText = (TextView) findViewById(R.id.currentReading);
	        currentValueText.setText(String.format("%.2f", pressureData.getAverage()));
	    	/*
	    	float trend = */pressureData.getTrend();
	    	/*float degrees = (float)Math.toDegrees(Math.atan(trend));
	    	ImageView arrow = (ImageView) findViewById(R.id.arrowImage);
	    	arrow.setRotation(degrees);
	    	*/
	    	updateGraph();
		}
	}
    
}