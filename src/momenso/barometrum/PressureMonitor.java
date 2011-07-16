package momenso.barometrum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;


public class PressureMonitor extends Activity implements SensorEventListener {

	private PressureData pressureData;
	private XYSeries series1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pressureData = new PressureData();
        
        registerSensor();
        initializeGraph();
    }
    
    private void registerSensor() {
	    SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	    Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (barometer != null) {
	    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }
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
    		plot.removeSeries(series1);
    		
    		series1 = new SimpleXYSeries(
			        Arrays.asList(pressureData.get()),
			        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
					"Air Pressure");
			 		
			LineAndPointFormatter series1Format = 
				new LineAndPointFormatter(
			            Color.rgb(0, 255, 0),          // line color
			            Color.GREEN,                   // point color
			            Color.TRANSPARENT);            // fill color (optional)
			
			plot.addSeries(series1, series1Format);
			plot.setRangeBoundaries(pressureData.getMinimum(), pressureData.getMaximum(), BoundaryMode.FIXED);
			
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
        minimumValueText.setText(dec.format(pressureData.getMinimum()) + "\nmin");
        TextView currentValueText = (TextView) findViewById(R.id.currentReading);
        currentValueText.setText(dec.format(currentValue) + "\n" + pressureData.getTrend());
        TextView maximumValueText = (TextView) findViewById(R.id.maximumReading);
    	maximumValueText.setText(dec.format(pressureData.getMaximum()) + "\nmax");
    	
    	updateGraph();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerSensor();
	}
    
}