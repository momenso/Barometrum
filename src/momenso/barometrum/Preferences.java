package momenso.barometrum;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;
import momenso.barometrum.ReadingsData.PressureMode;

public class Preferences {

	private PressureMode pressureMode;
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
	
    private void savePreferences() {
    	try {
    		FileOutputStream fos = context.openFileOutput("preferences", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		os.writeObject(pressureMode);
		} catch (IOException e) { Log.v("PREFS", "Failed: " + e.getMessage()); }
		
		Log.v("PREFS", "Saved as: " + pressureMode);
    }
    
    private void restorePreferences() {
    	try {
    		FileInputStream fis = context.openFileInput("preferences");
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item = is.readObject();
    		if (item instanceof PressureMode) {
    			pressureMode = (PressureMode)item;
    			
    			return;
    		}
    	} catch (Exception ex) { Log.v("PREFS", "Failed: " + ex.getMessage()); }
    	
    	pressureMode = PressureMode.BAROMETRIC;
    	
    	Log.v("PREFS", "Restore to: " + pressureMode);
    }

}
