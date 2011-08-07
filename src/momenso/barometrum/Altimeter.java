package momenso.barometrum;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observable;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Altimeter extends Observable implements LocationListener {
	
	private Context context;
	private float altitude;
	private boolean isSensorActive;
	
	public Altimeter(Context context) {
		this.context = context;
		this.isSensorActive = false;
		this.altitude = retreiveAltitude();
	}

	public float getAltitude() {
		return altitude;
	}

	public void setAltitude(float altitude) {
		if (altitude != this.altitude) {
			this.altitude = altitude;
			persistAltitude(altitude);
		}
	}
	
	private void persistAltitude(float altitude) {
    	try {
    		FileOutputStream fos = context.openFileOutput("altitude", Context.MODE_PRIVATE);
    		ObjectOutputStream os = new ObjectOutputStream(fos);
    		Float altitudeObject = altitude;
    		
    		os.writeObject(altitudeObject);
    		os.close();
    		
		} catch (IOException e) { }
	}

	private float retreiveAltitude() {
    	try {
    		FileInputStream fis = context.openFileInput("altitude");
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item = is.readObject();
    		is.close();
    		fis.close();
    		
    		if (item instanceof Float) {
    			return (Float)item;
    		}
    	} catch (Exception ex) { }
    	
    	return 0;
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

	public void enable() {
    	LocationManager lm = 
    		(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);		
	}
	
	public void disable() {
		LocationManager lm = 
			(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}
	
	public void onLocationChanged(Location location) {
		//float accuracy = location.getAccuracy();
		
		Float altitude = (float)location.getAltitude();
		setAltitude(altitude);
		setChanged();
		notifyObservers(altitude);
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
