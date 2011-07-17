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
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;


public class PressureMonitor extends Activity implements SensorEventListener, LocationListener {

	private ReadingsData pressureData;
	private XYSeries pressureSeries;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pressureData = new ReadingsData();
        
        registerSensor();
        initializeGraph();
    }
    
    private void registerSensor() {
	    SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	    Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (barometer != null) {
	    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }
	    
	    LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
			        Arrays.asList(pressureData.get()),
			        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
					"Air Pressure");
			LineAndPointFormatter pressureLineFormat = 
				new LineAndPointFormatter(
			            Color.GREEN,          // line color
			            Color.GREEN,          // point color
			            Color.TRANSPARENT);   // fill color (optional)
			plot.addSeries(pressureSeries, pressureLineFormat);
			
			plot.setRangeBoundaries(
					pressureData.getMinimum() - 1, 
					pressureData.getMaximum() + 1, 
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
        
        DecimalFormat dec = new DecimalFormat("0.0");
    	//String value = String.valueOf(dec.format(currentValue));
        
        TextView minimumValueText = (TextView) findViewById(R.id.minimumReading);
        minimumValueText.setText("Lowest " + dec.format(pressureData.getMinimum()));
        TextView currentValueText = (TextView) findViewById(R.id.currentReading);
        currentValueText.setText(dec.format(pressureData.getAverage()));
        TextView maximumValueText = (TextView) findViewById(R.id.maximumReading);
    	maximumValueText.setText("Highest " + dec.format(pressureData.getMaximum()));
    	
    	float trend = pressureData.getTrend();
    	ImageView arrow = (ImageView) findViewById(R.id.arrowImage);
    	arrow.setRotation((float)Math.atan(trend / 100));
    	
    	updateGraph();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
		
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerSensor();
	}

	public void onLocationChanged(Location location) {
		float currentAltitude = (float)location.getAltitude();
		//float accuracy = location.getAccuracy();
		
		TextView altitudeText = (TextView)findViewById(R.id.altitudeReading);
		//DecimalFormat dec = new DecimalFormat("0.00");
		altitudeText.setText("Elevation\n" + String.format("%.0fm", currentAltitude)/*dec.format(altitude)*/);
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