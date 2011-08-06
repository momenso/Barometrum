package momenso.barometrum;

import java.util.Observable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Barometer extends Observable implements SensorEventListener {
	
	private Context context;
	private long lastReadingMark;
	private boolean barometerRegistered;
	
	public Barometer(Context context) {
		this.context = context;
		this.barometerRegistered = false;
		this.lastReadingMark = 0;
	}
	
	public void registerPressureSensor() {
    	SensorManager sm = 
    		(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    	Sensor barometer = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
	    if (barometer != null) {
	    	sm.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
	    }
	    
//	    final CustomTextView barometerDisplay = 
//    		(CustomTextView)this.findViewById(R.id.currentReading);
//	    barometerDisplay.setText("...");
    }
    
	public void unregisterPressureSensor() {
		SensorManager sm = 
			(SensorManager)context.getSystemService(Context.SENSOR_SERVICE); 
		sm.unregisterListener(this);
		
//		final CustomTextView barometerDisplay = 
//    		(CustomTextView)this.findViewById(R.id.currentReading);
//	    barometerDisplay.setText("Paused");
	}
    
    public boolean switchPressureSensor() {
    	
    	if (!barometerRegistered) {
		    registerPressureSensor();
		    barometerRegistered = true;
	    } else {
	    	unregisterPressureSensor();
		    barometerRegistered = false;
	    }
	    
    	return barometerRegistered;
    }
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
    	
		if (System.currentTimeMillis() - lastReadingMark < 500)
			return;
		
		this.lastReadingMark = System.currentTimeMillis();
		Float currentValue = event.values[0];
		
		setChanged();
		notifyObservers(currentValue);
	}
}
