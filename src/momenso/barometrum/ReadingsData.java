package momenso.barometrum;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;


public class ReadingsData {
	
	public enum PressureMode { BAROMETRIC, MSLP };
	
	private Context context;
	private List<PressureDataPoint> historySamples;
	private List<PressureDataPoint> readingSamples;
	private PressureDataPoint minValue = new PressureDataPoint(0, Float.MAX_VALUE);
	private PressureDataPoint maxValue = new PressureDataPoint(0, Float.MIN_VALUE);

	private float average = 0;
	private PressureMode mode = PressureMode.BAROMETRIC;
	private float currentElevation = 0;

	public ReadingsData(Context context) 
	{
		this.context = context;
		
		readingSamples = new ArrayList<PressureDataPoint>();
		historySamples = new ArrayList<PressureDataPoint>();
		
		loadReadings();
	}
	
	private float estimateElevationAt(float pressure) {
		return (float)
			(1 - Math.pow(pressure * 100.0F / 101325.0F, 1.0F / 5.25588F)) / 0.0000225577F;
	}

	public void add(float pressureValue) 
	{
		//long timeFrame = 3600000; // hour
		long timeFrame = 60000; // minute
		
		if (currentElevation == 0 && readingSamples.size() == 0) {
			currentElevation = estimateElevationAt(pressureValue);
		}
		
		PressureDataPoint newSample = 
    		new PressureDataPoint((System.currentTimeMillis()), pressureValue);
		readingSamples.add(newSample);
		
		// clean old reading samples
		if (readingSamples.size() > 0) {
			PressureDataPoint first = readingSamples.get(0);
			long firstDate = first.getTime() / timeFrame;
			long currentDate = System.currentTimeMillis() / timeFrame;
			if (firstDate < currentDate) {
				readingSamples.remove(0);
			}
		}
		
		updateMinMax();
		
    	//update history
		if (historySamples.size() > 0)
		{
			PressureDataPoint lastHistory = historySamples.get(historySamples.size() - 1);
			long lastDate = lastHistory.getTime() / timeFrame;
			long currentDate = newSample.getTime() /  timeFrame;
			if (lastDate  == currentDate) {
				historySamples.remove(historySamples.size() - 1);
			}
		}
		
		PressureDataPoint updatedCurrent = new PressureDataPoint(System.currentTimeMillis(), average);
		historySamples.add(updatedCurrent);
		
		// limit the recorded history
		if (historySamples.size() > 60) {
			historySamples.remove(0);
		}
	}
	
	public List<PressureDataPoint> getHistory() {
		return this.historySamples;
	}
	
	public List<PressureDataPoint> get() {
		return readingSamples;
	}
	
	public List<Number> getPressure() 
	{
		List<Number> data = new ArrayList<Number>();
		for (PressureDataPoint m : readingSamples) {
			if (mode == PressureMode.MSLP) {
				data.add(convertToBarometric(m.getValue()));
			} else {
				data.add(m.getValue());
			}
		}

    	return data;
	}
	
	private void updateMinMax() 
	{
		this.minValue = new PressureDataPoint(0, Float.MAX_VALUE);
		this.maxValue = new PressureDataPoint(0, Float.MIN_VALUE);
		
		for (PressureDataPoint p : this.historySamples) {
			if (this.minValue.getValue() > p.getValue()) {
				this.minValue = p;
			} 
			if (this.maxValue.getValue() < p.getValue()) {
				this.maxValue = p;
			}
		}
	}
	
	public void set(List<PressureDataPoint> data) 
	{
		this.readingSamples.clear();
		this.readingSamples.addAll(data);

		updateMinMax();
		
		if (currentElevation == 0 && readingSamples.size() == 0) {
			// estimate elevation based on pressure
			currentElevation = estimateElevationAt(data.get(data.size() - 1).getValue());
		}
	}
	
	public void setHistory(List<PressureDataPoint> data) 
	{
		this.historySamples.clear();
		this.historySamples.addAll(data);		
	}
	
	public float getTrend()
    {
		// wait for a minimum reading samples
		if (readingSamples.size() < 1) {
			return 0;
		}
		
		// calculates the slope of the trend line
    	float sumX = 0;
		float y = 0;
    	for (PressureDataPoint point : readingSamples) {
    		float x = point.getValue();
    		sumX += x;
    		y++;
    	}

    	average = sumX / y;
    	    	
    	PressureDataPoint mark;
    	PressureDataPoint last = readingSamples.get(readingSamples.size() - 1);
    	if ((last.getValue() - minValue.getValue()) > (maxValue.getValue() - last.getValue())) {
    		mark = minValue;
    	} else {
    		mark = maxValue;
    	}
    	
    	return (last.getValue() - mark.getValue()) / 
    		((last.getTime() - mark.getTime()) / 2000);
    }
	
	private float convertToBarometric(float barometricPressure)
	{
		double localStandardPressure = 
			101325.0F * Math.pow(1.0F - 0.0000225577F * currentElevation, 5.25588F);
		double pressureDifference = 101325 - localStandardPressure;			
		float mslp = barometricPressure + (float)pressureDifference / 100;
		
		return mslp;
	}
	
	public float getMinimum()
	{
		float value = minValue.getValue();
		
		if (value == Float.MAX_VALUE) {
			return 0;
		}
		
		if (mode == PressureMode.MSLP) {
			return convertToBarometric(value);
		} else {
			return value;
		}
	}
	
	public float getMaximum()
	{
		float value = maxValue.getValue();
		
		if (value == Float.MIN_VALUE) {
			return 0;
		}
		
		if (mode == PressureMode.MSLP) {
			return convertToBarometric(value);
		} else {
			return value;
		}
	}
	
	public float getAverage() 
	{
		if (mode == PressureMode.MSLP) {
			return convertToBarometric(average);
		} else {
			return average;
		}
	}

	public void clear() {
		this.readingSamples.clear();
		this.minValue = new PressureDataPoint(0, Float.MAX_VALUE);
		this.maxValue = new PressureDataPoint(0, Float.MIN_VALUE);
	}

	public void setCurrentElevation(float altitude) {
		this.currentElevation = altitude;
	}

	public void setMode(PressureMode mode) {
		this.mode = mode;
	}

	public void setMode(PressureMode mode, float altitude) {
		this.mode = mode;
		this.currentElevation = altitude;
	}
	
	
	// -------------------------------------------------------------------------
	// Persistence methods
	// -------------------------------------------------------------------------
	
	public void saveReadings() {
    	try {
    		persistReadings(get(), "readings");
    		persistReadings(getHistory(), "history");
		} catch (IOException e) {
			/*AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Save");
    		alertDialog.setMessage("Failed to save readings: " + e.getLocalizedMessage());
    		alertDialog.show();*/
		}
    }
    
    private void persistReadings(List<PressureDataPoint> data, String fileName) throws IOException {
		
    	FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		
		for(PressureDataPoint n : data) {
			os.writeObject(n);
		}
    }
    
    private void loadReadings() {
    	
    	try {
    		
    		List<PressureDataPoint> readings = restoreReadings("readings");
    		set(readings);
    		
    		List<PressureDataPoint> history = restoreReadings("history");
    		setHistory(history);
    		
    	} catch (Exception e) {
    		/*AlertDialog alertDialog;
    		alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Load");
    		alertDialog.setMessage("Failed to load readings: " + e.getLocalizedMessage());
    		alertDialog.show();*/
		} finally {
			//registerPressureSensor();
    		//updateGraph();
		}
    }
    
    private List<PressureDataPoint> restoreReadings(String fileName) throws IOException, ClassNotFoundException {
    	
    	List<PressureDataPoint> data = new ArrayList<PressureDataPoint>();
    	
    	try {
    		FileInputStream fis = context.openFileInput(fileName);
    		ObjectInputStream is = new ObjectInputStream(fis);
    		
    		Object item;
    		while ((item = is.readObject()) != null) {
    			if (item instanceof PressureDataPoint)
    			data.add((PressureDataPoint)item);
    		}
    	} catch (EOFException ex) {
    		// happens at the end of the file
    	}

		return data;
    }
}
