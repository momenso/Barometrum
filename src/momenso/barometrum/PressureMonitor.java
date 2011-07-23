package momenso.barometrum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import momenso.barometrum.ReadingsData.PressureMode;
import momenso.barometrum.gui.CustomTextView;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;


public class PressureMonitor 
	extends Activity 
	implements SensorEventListener, LocationListener 
{

	private ReadingsData pressureData;
	private XYSeries pressureSeries;
	private boolean GPSRegistered = false;
	private boolean barometerRegistered = false;
	private float lastKnownAltitude = 0;
	private Preferences preferences;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pressureData = new ReadingsData();
        lastKnownAltitude = loadLastKnownAltitude();
        preferences = new Preferences(getApplicationContext());        
        
        loadReadings();
        initializeGraph();
    }
        
    @Override
	protected void onPause() {
		super.onPause();
		
		unregisterPressureSensor();
		
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		
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
		saveLastKnownAltitude(lastKnownAltitude);
		
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
    		case R.id.itemMenuClear:
        		pressureData.clear();
    			return true;
    			
    		case R.id.Barometric:
    			preferences.setPressureMode(PressureMode.BAROMETRIC);
    			pressureData.setMode(PressureMode.BAROMETRIC);
    			item.setChecked(true);
    			return true;
    			
    		case R.id.MSLP:
    			preferences.setPressureMode(PressureMode.MSLP);
    			pressureData.setMode(PressureMode.MSLP, lastKnownAltitude);
    			item.setChecked(true);
    			return true;
    			
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
        
	private void saveLastKnownAltitude(float altitude)
	{
    	try {
    		FileOutputStream fos = openFileOutput("altitude", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		Float altitudeObject = altitude;
    		os.writeObject(altitudeObject);
		} catch (IOException e) { }
	}

	private float loadLastKnownAltitude() {
    	try {
    		FileInputStream fis = openFileInput("altitude");
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item = is.readObject();
    		if (item instanceof Float) {
    			return (Float)item;
    		}
    	} catch (Exception ex) { }
    	
    	return 0;
	}
    
    private void saveReadings() {

    	unregisterPressureSensor();
    	
    	try {
    		FileOutputStream fos = openFileOutput("readings", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		List<PressureDataPoint> data = pressureData.get();
    		int qtd = 0;
    		for(PressureDataPoint n : data) {
    			os.writeObject(n);
    			qtd++;
    		}
		} catch (IOException e) {
			AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Save");
    		alertDialog.setMessage("Failed to save readings: " + e.getLocalizedMessage());
    		alertDialog.show();
		} finally {
			registerPressureSensor();
		}
    }
    
    private void loadReadings() {
    	
    	unregisterPressureSensor();
    	
    	List<PressureDataPoint> data = new ArrayList<PressureDataPoint>();
    	int qtd = 0;
    	
    	try {
    		FileInputStream fis = openFileInput("readings");
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item;
    		while ((item = is.readObject()) != null) {
    			if (item instanceof PressureDataPoint)
    			data.add((PressureDataPoint)item);
    			qtd++;
    		}
    	} catch (EOFException ex) {
    		this.pressureData.set(data);    		
    	} catch (Exception e) {
    		AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Load");
    		alertDialog.setMessage("Failed to load readings: " + e.getLocalizedMessage());
    		alertDialog.show();
		} finally {
			//registerPressureSensor();
    		updateGraph();
		}
    }
    
    private void registerSensor() {
	    registerPressureSensor();
    	//loadReadings();
	    
	    final CustomTextView altimeter = (CustomTextView)this.findViewById(R.id.altitudeReading);
	    altimeter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (switchGPSSensor()) {
			    	altimeter.setText("...");
			    } else {
			    	altimeter.setText("Altimeter\nDisabled");
			    }
			}
		});
	    
	    final CustomTextView barometer = (CustomTextView)this.findViewById(R.id.currentReading);
	    barometer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchPressureSensor();
			}
		});
    }
    
    private void registerPressureSensor() {
    	SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (barometer != null) {
	    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }
	    
	    final CustomTextView barometerDisplay = 
    		(CustomTextView)this.findViewById(R.id.currentReading);
	    barometerDisplay.setText("...");
    }
    
    private void unregisterPressureSensor() {
		SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
		
		final CustomTextView barometerDisplay = 
    		(CustomTextView)this.findViewById(R.id.currentReading);
	    barometerDisplay.setText("Paused");
	}
    
    private boolean switchPressureSensor() {
    	
    	if (!barometerRegistered) {
		    registerPressureSensor();
		    barometerRegistered = true;
	    } else {
	    	unregisterPressureSensor();
		    barometerRegistered = false;
	    }
	    
    	return barometerRegistered;
    }
    
    private boolean switchGPSSensor() {
    	
    	LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	if (!GPSRegistered) {
		    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		    GPSRegistered = true;
    	} else {
    		lm.removeUpdates(this);
    		GPSRegistered = false;
    	}
    	
    	return GPSRegistered;
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
					pressureData.getMinimum() - 0.4, 
					pressureData.getMaximum() + 0.4, 
					BoundaryMode.FIXED);

			plot.redraw();
    	}
    			
		//Log.v("updateGraph", "time=" + (System.currentTimeMillis() - start));
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
    	
		float currentValue = event.values[0];
		pressureData.add(currentValue);
        
        DecimalFormat dec = new DecimalFormat("0.00");
    	//String value = String.valueOf(dec.format(currentValue));
        
        TextView minimumValueText = (TextView) findViewById(R.id.minimumReading);
        minimumValueText.setText("Lowest " + dec.format(pressureData.getMinimum()));
        TextView maximumValueText = (TextView) findViewById(R.id.maximumReading);
    	maximumValueText.setText("Highest " + dec.format(pressureData.getMaximum()));

    	TextView currentValueText = (TextView) findViewById(R.id.currentReading);
        currentValueText.setText(dec.format(pressureData.getAverage()));
    	
    	float trend = pressureData.getTrend();
    	float degrees = (float)Math.toDegrees(Math.atan(trend));
    	ImageView arrow = (ImageView) findViewById(R.id.arrowImage);
    	arrow.setRotation(degrees);
    	
    	updateGraph();
	}

	public void onLocationChanged(Location location) {
		this.lastKnownAltitude = (float)location.getAltitude();
		//float accuracy = location.getAccuracy();
		saveLastKnownAltitude(lastKnownAltitude);
		
		TextView altitudeText = (TextView)findViewById(R.id.altitudeReading);
		altitudeText.setText("Elevation\n" + String.format("%.0fm", this.lastKnownAltitude));
		pressureData.setCurrentElevation(this.lastKnownAltitude);
	}

	public void onProviderDisabled(String provider) {
		Log.v("ALTITUDE", "Provider disabled=" + provider);
	}

	public void onProviderEnabled(String provider) {
		Log.v("ALTITUDE", "Provider enabled=" + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.v("ALTITUDE", "Status changed=" + status);
	}
    
}