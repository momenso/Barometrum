package momenso.barometrum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import momenso.barometrum.PressureDataPoint.PressureMode;
import momenso.barometrum.PressureDataPoint.PressureUnit;
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
	private XYSeries pressureSeries;
	private ReadingsData pressureData;
	private Altimeter altimeter;
	private Barometer barometer;
	private Preferences preferences;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Context context = getApplicationContext();
        preferences = new Preferences(context);

        altimeter = new Altimeter(context);
        altimeter.addObserver(this);
        
        barometer = new Barometer(context);
        barometer.addObserver(this);

        pressureData = ReadingsData.getInstance(context);
        pressureData.setMode(preferences.getPressureMode(), altimeter.getAltitude());
        pressureData.setUnit(preferences.getPressureUnit());
        
        // initialize pressure reading display
        final CustomTextView currentReading = (CustomTextView) findViewById(R.id.currentReading);
        Typeface font = Typeface.createFromAsset(getAssets(), "DS-DIGIB.TTF");
        currentReading.setTypeface(font);
        currentReading.setTextColor(Color.WHITE);
        currentReading.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (barometer.switchSensor()) {
					currentReading.setTextColor(Color.WHITE);
				} else {
					currentReading.setTextColor(Color.GRAY);
				}
			}
		});
        
        // initialize max pressure display
        Typeface standardFont = Typeface.createFromAsset(getAssets(), "ProFontWindows.ttf");
        BlockView maxReading = (BlockView) findViewById(R.id.maximumReading);
        maxReading.setTypeface(standardFont);
        maxReading.setLabelWidth(8);
        maxReading.setUnit(ReadingsData.getUnitName());
        maxReading.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showMaximum();
			}
		});

        // initialize max pressure display
        BlockView minReading = (BlockView) findViewById(R.id.minimumReading);
        minReading.setTypeface(standardFont);
        minReading.setLabelWidth(8);
		minReading.setUnit(ReadingsData.getUnitName());
        minReading.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showMinimum();
			}
		});
        
		// initialize altimeter display
		BlockView altitudeReading = (BlockView) findViewById(R.id.altitudeReading);
        altitudeReading.setTypeface(standardFont);
        altitudeReading.setText(String.format("%.0f", altimeter.getAltitude()));
        altitudeReading.setLabelWidth(8);
        
        initializeGraph();
    }
    
    private void showMaximum() {
		try {
			AlertDialog alertDialog = 
				new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Highest Reading");
			String message = 
				String.format("The maximum reading of %.2f%s was recorded on %s",
						pressureData.getMaximum(), ReadingsData.getUnitName(),
						pressureData.getDateMaximum().toLocaleString());
			alertDialog.setMessage(message);
			alertDialog.show();
		} catch (Throwable t) {
			Log.v("HIGH", t.toString());
			Log.v("HIGH", t.getMessage());
		}
    }
    
    private void showMinimum() {
		try {
			AlertDialog alertDialog = 
				new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Lowest Reading");
			String message = 
				String.format("The minimum reading of %.2f%s was recorded on %s",
						pressureData.getMinimum(), ReadingsData.getUnitName(),
						pressureData.getDateMinimum().toLocaleString());
			alertDialog.setMessage(message);
			alertDialog.show();
		} catch (Throwable t) {
			Log.v("LOW", t.toString());
			Log.v("LOW", t.getMessage());
		}
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		
		// disable sensors when leaving the activity
		altimeter.disable();
		barometer.disable();
		
		pressureData.saveReadings();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		barometer.enable();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//altimeter.disable();
		//barometer.disable();
		pressureData.saveReadings();
		
		super.onSaveInstanceState(outState);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu_options, menu);
    	    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    			
    		case R.id.itemBarometerMode:
    			selectBarometerMode();
    			return true;
    			
    		case R.id.itemPressureUnit:
    			selectPressureUnit();
    			return true;
    			
    		case R.id.itemAltimeter:
    			if (altimeter.switchSensor()) {
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
    
    private void selectPressureUnit() {
    	final CharSequence[] items = { "Bar", "Torr", "Pascal" };
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Pressure Unit");
    	builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
						        
				if (item == 0) {
					pressureData.setUnit(PressureUnit.Bar);
					preferences.setPressureUnit(PressureUnit.Bar);
				} else if (item == 1) {
					pressureData.setUnit(PressureUnit.Torr);
					preferences.setPressureUnit(PressureUnit.Torr);
				} else if (item == 2) {
					pressureData.setUnit(PressureUnit.Pascal);
					preferences.setPressureUnit(PressureUnit.Pascal);
				}
				
		        BlockView maxReading = (BlockView) findViewById(R.id.maximumReading);
				maxReading.setUnit(ReadingsData.getUnitName());
				BlockView minReading = (BlockView) findViewById(R.id.minimumReading);
				minReading.setUnit(ReadingsData.getUnitName());

			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void selectBarometerMode() {
    	final CharSequence[] items = { "Barometric", "Mean sea level" };
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Reading Mode");
    	builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				if (item == 0) {
					pressureData.setMode(PressureMode.BAROMETRIC);
					preferences.setPressureMode(PressureMode.BAROMETRIC);
				} else if (item == 1) {
					pressureData.setMode(PressureMode.MSLP);
					preferences.setPressureMode(PressureMode.MSLP);
				}				
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
        
    private void initializeGraph() {
    	XYPlot plot = (XYPlot)this.findViewById(R.id.mySimpleXYPlot);
    	if (plot != null) {
    		plot.setTicksPerRangeLabel(3);
    		plot.disableAllMarkup();
    		plot.setDomainLabel("");
    		plot.setRangeLabel("");
    		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.rgb(30, 30, 30));
    		plot.getGraphWidget().getGridLinePaint().setColor(Color.rgb(150, 150, 150));
    		plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.rgb(150, 150, 150));
    		plot.getGraphWidget().getBackgroundPaint().setColor(Color.rgb(30, 30, 30));
    		plot.getBackgroundPaint().setColor(Color.rgb(30, 30, 30));
    		plot.getBorderPaint().setColor(Color.rgb(150, 150, 150));
    		plot.getBorderPaint().setStrokeWidth(3);
    	} else {
    		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Graphics");
    		alertDialog.setMessage("Failed to initialize graph ploting.");
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
					pressureData.getMinimum() - 0.2, 
					pressureData.getMaximum() + 0.2, 
					BoundaryMode.FIXED);

			plot.redraw();
    	}
    	
    	// update History graph
    	ChartView historyChart = (ChartView)this.findViewById(R.id.historyChart);
    	if (historyChart != null) {
    		historyChart.updateData(pressureData);
    	}
    	
		//Log.v("updateGraph", "time=" + (System.currentTimeMillis() - start));
    }

	public void update(Observable observable, Object data) {
		if (observable.getClass() == Altimeter.class) {
			
			float altitude = (Float)data;
			pressureData.setCurrentElevation(altitude);
			
			BlockView altitudeText = (BlockView)findViewById(R.id.altitudeReading);
			altitudeText.setText(String.format("%.0fm", altitude));
			
		} else if (observable.getClass() == Barometer.class) {
			
			float pressure = (Float)data;
			pressureData.add(pressure);
			
			BlockView minimumValueText = (BlockView) findViewById(R.id.minimumReading);
	        minimumValueText.setText(String.format("%.2f", 
	        		Math.round(pressureData.getMinimum() * 100.0) / 100.0));
	        BlockView maximumValueText = (BlockView) findViewById(R.id.maximumReading);
	    	maximumValueText.setText(String.format("%.2f", 
	    			Math.round(pressureData.getMaximum() * 100.0) / 100.0));

	    	TextView currentValueText = (TextView) findViewById(R.id.currentReading);
	        currentValueText.setText(String.format("%.2f", 
	        		Math.round(pressureData.getAverage() * 100.0) / 100.0));
	    	
	        /*
	    	float trend = pressureData.getTrend();
	    	float degrees = (float)Math.toDegrees(Math.atan(trend));
	    	ImageView arrow = (ImageView) findViewById(R.id.arrowImage);
	    	arrow.setRotation(degrees);
	    	*/
	    	
	    	updateGraph();
		}
	}
    
}