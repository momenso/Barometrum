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
import java.util.ArrayList;
import java.util.Arrays;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;


public class PressureMonitor extends Activity implements SensorEventListener {

	private ArrayList<PressureDataPoint> readingSamples = new ArrayList<PressureDataPoint>();
	private long eventTime;
	private XYSeries series1;
	private float minValue = Float.MAX_VALUE;
	private float maxValue = Float.MIN_VALUE;
	private float average;
	private String tendency;
	private long lastRead = 0;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        eventTime = System.currentTimeMillis();
        
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
    
    private void updateGraph(float pressureValue)
    {
    	long now = System.currentTimeMillis();
    	if (now - lastRead < 2000)
    	{
    		return;
    	}
    	
    	if (readingSamples.size() > 100)
    	{
    		readingSamples.remove(0);
    	}
    	
    	PressureDataPoint newSample = new PressureDataPoint(
    			(System.currentTimeMillis() - eventTime),
    			pressureValue);
    	readingSamples.add(newSample);
    	
    	PressureDataPoint[] values = readingSamples.toArray(new PressureDataPoint[0]);
    	Number[] data = new Number[values.length];
    	int i = 0;
    	for (PressureDataPoint m : values) {
    		data[i++] = m.getValue();
    	}
    	
    	average = (minValue + maxValue) / 2; 
    	
    	if (pressureValue > average) {
    		tendency = "up";
    	} else if (pressureValue == average) {
    		tendency = "stable";
    	} else {
    		tendency = "down";
    	}    	
    	
    	XYPlot plot = (XYPlot)this.findViewById(R.id.mySimpleXYPlot);
    	if (plot != null)
    	{
    		plot.removeSeries(series1);
    		
    		series1 = new SimpleXYSeries(
			        Arrays.asList(data),
			        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
					"Air Pressure");
			 		
			LineAndPointFormatter series1Format = new LineAndPointFormatter(
			            Color.rgb(0, 255, 0),                   // line color
			            Color.GREEN,                   // point color
			            Color.TRANSPARENT);              // fill color (optional)
			
			plot.addSeries(series1, series1Format);
			
			plot.setRangeBoundaries(minValue, maxValue, BoundaryMode.FIXED);
			
			plot.redraw();
    	}
    			
		//Log.v("updateGraph", "time=" + (System.currentTimeMillis() - start));
    	lastRead = System.currentTimeMillis();
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
    	
        float currentValue = event.values[0];
        if (currentValue > maxValue)
        	maxValue = currentValue;
        if (currentValue < minValue)
        	minValue = currentValue;
        
        DecimalFormat dec = new DecimalFormat("0.00");
    	//String value = String.valueOf(dec.format(currentValue));
        
        TextView minimumValueText = (TextView) findViewById(R.id.minimumReading);
        minimumValueText.setText(dec.format(minValue) + "\nmin");
        TextView currentValueText = (TextView) findViewById(R.id.currentReading);
        currentValueText.setText(dec.format(currentValue) + "\n" + tendency);
        TextView maximumValueText = (TextView) findViewById(R.id.maximumReading);
    	maximumValueText.setText(dec.format(maxValue) + "\nmax");
    	
    	updateGraph(event.values[0]);
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