package momenso.barometrum;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import momenso.barometrum.PressureDataPoint.PressureMode;
import momenso.barometrum.PressureDataPoint.PressureUnit;

import android.content.Context;

public class Preferences {

	private PressureMode pressureMode;
	private PressureUnit pressureUnit;
	private Integer loggingInterval;
	private Context context;
	
	public Preferences(Context context) {
		this.context = context;
		
		restorePreferences();
	}
	
	public void setPressureMode(PressureMode mode) {
		this.pressureMode = mode;
		
		savePreferences();
	}
		
	public PressureMode getPressureMode() {
		return this.pressureMode;
	}
	
	public void setPressureUnit(PressureUnit unit) {
		this.pressureUnit = unit;
		
		savePreferences();
	}

	public PressureUnit getPressureUnit() {
		return this.pressureUnit;
	}
	
    public Integer getLoggingInterval() {
		return loggingInterval;
	}

	public void setLoggingInterval(Integer loggingInterval) {
		this.loggingInterval = loggingInterval;
	}

	private void savePreferences() {
    	try {
    		FileOutputStream fos = context.openFileOutput("preferences", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		os.writeObject(pressureMode);
    		os.writeObject(pressureUnit);
    		os.writeObject(loggingInterval);
		} catch (IOException e) { }
    }
    
    // TODO: make it so that when a parameter reading fails, load the default (just for the one)
    private void restorePreferences() {
    	try {
    		FileInputStream fis = context.openFileInput("preferences");
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item = is.readObject();
    		if (item instanceof PressureMode) {
    			pressureMode = (PressureMode)item;
    		}

    		item = is.readObject();
    		if (item instanceof PressureUnit) {
    			pressureUnit = (PressureUnit)item;
    		}
    		
    		item = is.readObject();
    		if (item instanceof Integer) {
    			loggingInterval = (Integer)item;
    		}
    		
    		return;
    		
    	} catch (Exception ex) { }
    	
    	pressureMode = PressureMode.BAROMETRIC;
    	pressureUnit = PressureUnit.Bar;
    	loggingInterval = 60000;
    }

}
