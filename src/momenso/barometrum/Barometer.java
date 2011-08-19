package momenso.barometrum;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Barometer extends Observable implements SensorEventListener {
	
	private Context context;
	//private long startReadingTime;
	private boolean isSensorActive;
	private Timer workerThread;
	
	public Barometer(Context context) {
		this.context = context;
		this.isSensorActive = false;
		//this.startReadingTime = 0;
		
		workerThread = new Timer();
		workerThread.schedule(new TimerTask() { 
			@Override
			public void run() {
				active();
			}}, 0, 2000);
	}
	
	private void active() {
		Log.v("Barometer", "Active: enabling barometer");
		
		enable();
	}
	
	public void enable() {
		Log.v("Barometer", "Enable: registering sensor");
		
		if (!isSensorActive) {
	    	SensorManager sm = 
	    		(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    	Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
		    if (barometer != null) {
		    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
		    	
		    	isSensorActive = true;
		    	//startReadingTime = System.currentTimeMillis();
		    }
		}
    }
    
	public void disable() {
		
		Log.v("Barometer", "Disable: unregistering sensor");
		
		SensorManager sm = 
			(SensorManager)context.getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
		
		isSensorActive = false;
	}
    
    public boolean switchSensor() {
    	
    	Log.v("Barometer", "Switching sensor");
    	
    	if (!isSensorActive) {
		    enable();
	    } else {
	    	disable();
	    }
	    
    	return isSensorActive;
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not interested in this event
	}

	public void onSensorChanged(SensorEvent event) {
		
		Log.v("Barometer", "onSensorChanged: received new data");
    	
		float currentValue = event.values[0];
		
		setChanged();
		notifyObservers(currentValue);
		
		/*if (System.currentTimeMillis() - startReadingTime > 1000)*/ {
			disable();
		}
		
	}
	
}
