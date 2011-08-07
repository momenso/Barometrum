package momenso.barometrum;

import java.util.Observable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Barometer extends Observable implements SensorEventListener {
	
	private Context context;
	private long lastReadingTime;
	private boolean isSensorActive;
	
	public Barometer(Context context) {
		this.context = context;
		this.isSensorActive = false;
		this.lastReadingTime = 0;
	}
	
	public void enable() {
    	SensorManager sm = 
    		(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    	Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (barometer != null) {
	    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }
    }
    
	public void disable() {
		SensorManager sm = 
			(SensorManager)context.getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
	}
    
    public boolean switchSensor() {
    	
    	if (!isSensorActive) {
		    enable();
		    isSensorActive = true;
	    } else {
	    	disable();
		    isSensorActive = false;
	    }
	    
    	return isSensorActive;
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not interested in this event
	}

	public void onSensorChanged(SensorEvent event) {
    	
		if (System.currentTimeMillis() - lastReadingTime < 500)
			return;
		
		this.lastReadingTime = System.currentTimeMillis();
		Float currentValue = event.values[0];
		
		setChanged();
		notifyObservers(currentValue);
	}
	
}
