package momenso.barometrum;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

import momenso.barometrum.ReadingsData.PressureMode;
import momenso.barometrum.ReadingsData.PressureUnit;


public class Preferences {

	private PressureMode pressureMode;
	private PressureUnit pressureUnit;
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
	
    private void savePreferences() {
    	try {
    		FileOutputStream fos = context.openFileOutput("preferences", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		os.writeObject(pressureMode);
    		os.writeObject(pressureUnit);
		} catch (IOException e) { }
    }
    
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
    		
    		return;
    		
    	} catch (Exception ex) { }
    	
    	pressureMode = PressureMode.BAROMETRIC;
    	pressureUnit = PressureUnit.Bar;
    }

}
