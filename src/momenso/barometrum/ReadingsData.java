package momenso.barometrum;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import momenso.barometrum.PressureDataPoint.PressureMode;
import momenso.barometrum.PressureDataPoint.PressureUnit;

import android.content.Context;


public class ReadingsData {
		
	private Context context;
	private List<PressureDataPoint> historySamples;
	private List<PressureDataPoint> readingSamples;
	private PressureDataPoint minValue;
	private PressureDataPoint maxValue;

	private PressureDataPoint average;
	private static PressureMode mode = PressureMode.BAROMETRIC;
	private static PressureUnit unit = PressureUnit.Bar;
	private static float currentElevation = 0;
	private static int loggingInterval = 60000;
	private static ReadingsData instance;

	private ReadingsData(Context context) 
	{
		this.context = context;
		this.average = new PressureDataPoint();
		
		readingSamples = new ArrayList<PressureDataPoint>();
		historySamples = new ArrayList<PressureDataPoint>();
		
		initializeMinMax();
		loadReadings();
	}
	
	public static ReadingsData getInstance(Context context) {
		if (instance != null) {
			instance.context = context;
		} else {
			instance = new ReadingsData(context);
		}
		
		return instance;
	}
	
	private float estimateElevationAt(float pressure) {
		return (float)
			(1 - Math.pow(pressure * 100.0F / 101325.0F, 1.0F / 5.25588F)) / 0.0000225577F;
	}

	public void add(float pressureValue) 
	{
		if (currentElevation == 0 && readingSamples.size() == 0) {
			currentElevation = estimateElevationAt(pressureValue);
		}
		
		PressureDataPoint newSample = 
    		new PressureDataPoint((System.currentTimeMillis()), pressureValue);
		readingSamples.add(newSample);
		
		// clean old reading samples
		// basically keeps only readings concerning
		// the current time frame 
		if (readingSamples.size() > 0) {
			PressureDataPoint first = readingSamples.get(0);
			long firstDate = first.getTime() / loggingInterval;
			long currentDate = System.currentTimeMillis() / loggingInterval;
			if (firstDate < currentDate) {
				readingSamples.remove(0);
			}
		}
		
		updateStatistics();
		
    	// update history
		// removes obsolete historic reading since
		// an updated reading for the current time frame
		// is available
		if (historySamples.size() > 0)
		{
			PressureDataPoint lastHistory = historySamples.get(historySamples.size() - 1);
			long lastDate = lastHistory.getTime() / loggingInterval;
			long currentDate = newSample.getTime() /  loggingInterval;
			if (lastDate  == currentDate) {
				historySamples.remove(historySamples.size() - 1);
			}
		}
		
		PressureDataPoint updatedCurrent = 
			new PressureDataPoint(System.currentTimeMillis(), average.getRawValue());
		historySamples.add(updatedCurrent);
		
		// limit the recorded history
		if (historySamples.size() > 45) { // 59 
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
			data.add(m.getValue(mode, unit, currentElevation));
		}

    	return data;
	}
	
	private void initializeMinMax() {
		if (this.minValue == null)
			this.minValue = new PressureDataPoint(0, Float.MAX_VALUE);
		if (this.maxValue == null)
			this.maxValue = new PressureDataPoint(0, Float.MIN_VALUE);	
	}
	
	private void updateMinMax(PressureDataPoint data) {
		if (this.minValue.getRawValue() > data.getRawValue()) {
			this.minValue = data;
		} 
		if (this.maxValue.getRawValue() < data.getRawValue()) {
			this.maxValue = data;
		}
	}
	
	private void updateStatistics() 
	{		
		// updates min/max based on reading history
		for (PressureDataPoint p : this.historySamples) {
			updateMinMax(p);
		}
		
		// computes the current average reading
		float sumValues = 0;
		for (PressureDataPoint p : this.readingSamples) {
			float value = p.getRawValue();
			sumValues += value;
		}
		
		this.average.setValue(sumValues / this.readingSamples.size());
		this.average.setTime(System.currentTimeMillis());
		
		updateMinMax(average);
	}
		
	/*public float getTrend()
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
    }*/
	
	public void set(List<PressureDataPoint> data)
	{
		this.readingSamples.clear();
		this.readingSamples.addAll(data);

		updateStatistics();
		
		if (currentElevation == 0 && readingSamples.size() == 0) {
			currentElevation = estimateElevationAt(data.get(data.size() - 1).getRawValue());
		}
	}
	
	public void setHistory(List<PressureDataPoint> data) 
	{
		this.historySamples.clear();
		this.historySamples.addAll(data);		
	}
	
	public float getMinimum()
	{			
		return minValue.getValue(mode, unit, currentElevation);
	}
	
	public Date getDateMinimum() {
		Date date = new Date(minValue.getTime());
		
		return date;
	}
	
	public float getMaximum()
	{
		return maxValue.getValue(mode, unit, currentElevation);
	}
	
	public PressureDataPoint getMaximumValue() {
		return maxValue;
	}
	
	public PressureDataPoint getMinimumValue() {
		return minValue;
	}
	
	public Date getDateMaximum() {
		Date date = new Date(maxValue.getTime());
		
		return date;
	}
	
	public float getAverage() 
	{
		return average.getValue(mode, unit, currentElevation);
	}
	
	public static float getReadingValue(PressureDataPoint value) {
		return value.getValue(mode, unit, currentElevation);
	}
	
	/*public static String getValueString(PressureDataPoint value) {
		String reading = String.format("%.2f%s",
				getReadingValue(value), getUnitName());
		
		return reading;
	}*/

	public void clear() {
		this.readingSamples.clear();
		this.historySamples.clear();
		this.minValue = new PressureDataPoint(0, Float.MAX_VALUE);
		this.maxValue = new PressureDataPoint(0, Float.MIN_VALUE);
	}

	public void setCurrentElevation(float altitude) {
		ReadingsData.currentElevation = altitude;
	}

	public void setMode(PressureMode mode) {
		ReadingsData.mode = mode;
	}

	public void setMode(PressureMode mode, float altitude) {
		ReadingsData.mode = mode;
		ReadingsData.currentElevation = altitude;
	}
	
	public void setHistoryInterval(int interval) {
		loggingInterval = interval;
	}

	public void setUnit(PressureUnit unit) {
		ReadingsData.unit = unit;
	}
	
	public PressureUnit getUnit() {
		return ReadingsData.unit;
	}
	
	public static String getUnitName() {
		switch (unit) {
			case Bar:
				return "mb";
				
			case Torr:
				return "mmHg";
				
			case Pascal:
				return "kPa";
				
			default:
				return "mb";
		}
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
    		// happens when reading reaches end of the file
    	}

		return data;
    }

}
