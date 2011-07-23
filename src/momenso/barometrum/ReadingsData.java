package momenso.barometrum;

import java.util.ArrayList;
import java.util.List;


public class ReadingsData {
	
	public enum PressureMode { BAROMETRIC, MSLP };
	
	private List<PressureDataPoint> readingSamples;
	private PressureDataPoint minValue = new PressureDataPoint(0, Float.MAX_VALUE);
	private PressureDataPoint maxValue = new PressureDataPoint(0, Float.MIN_VALUE);

	private float average = 0;
	private PressureMode mode = PressureMode.BAROMETRIC;
	private float currentElevation = 0;

	public ReadingsData() 
	{
		readingSamples = new ArrayList<PressureDataPoint>();		
	}
	
	private float estimateElevationAt(float pressure) {
		return (float)
			(1 - Math.pow(pressure * 100.0F / 101325.0F, 1.0F / 5.25588F)) / 0.0000225577F;
	}

	public void add(float pressureValue) 
	{
		if (currentElevation == 0 && readingSamples.size() == 0) {
			// estimate elevation based on pressure
			currentElevation = estimateElevationAt(pressureValue);
		}
		
		if (readingSamples.size() > 100) {
    		readingSamples.remove(0);
    	}
		
		updateMinMax();
		
    	PressureDataPoint newSample = 
    		new PressureDataPoint((System.currentTimeMillis()), pressureValue);
    	readingSamples.add(newSample);
	}
	
	public List<PressureDataPoint> get()
	{
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
		
		for (PressureDataPoint p : this.readingSamples) {
			// TODO: Implement PressureDataPoint compare
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
	
	public float getTrend()
    {
		// wait for a minimum reading samples
		if (readingSamples.size() < 10) {
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
}
