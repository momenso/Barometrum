package momenso.barometrum;

import java.util.ArrayList;
import java.util.List;

public class ReadingsData {
	private List<PressureDataPoint> readingSamples;
	private long eventTime;
	private float minValue = Float.MAX_VALUE;
	private float maxValue = Float.MIN_VALUE;
	private float average = 0;

	public ReadingsData() {
		readingSamples = new ArrayList<PressureDataPoint>();
		eventTime = System.currentTimeMillis();
	}

	public void add(float pressureValue) {
		
		if (readingSamples.size() > 500) {
    		readingSamples.remove(0);
    	}
		
		if (pressureValue > maxValue)
        	maxValue = pressureValue;
        if (pressureValue < minValue)
        	minValue = pressureValue;
    	
    	PressureDataPoint newSample = 
    		new PressureDataPoint((System.currentTimeMillis() - eventTime), pressureValue);
    	readingSamples.add(newSample);
	}
	
	public List<PressureDataPoint> get()
	{
		return readingSamples;
	}
	
	public List<Number> getPressure() {
		
		List<Number> data = new ArrayList<Number>();
		for (PressureDataPoint m : readingSamples) {
			data.add(m.getValue());
		}

    	return data;
	}
	
	public void set(List<PressureDataPoint> data) {
		this.readingSamples = data;
	}
	
	public float getTrend()
    {
		// wait for a minimum reading samples
		if (readingSamples.size() < 10) {
			return 0;
		}
		
		// calculates the slope of the trend line
    	float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
		float y = 0;
    	for (PressureDataPoint point : readingSamples) {
    		float x = point.getValue();
    		sumX += x;
    		sumY += y;
    		sumXY += x * y;
    		sumX2 += x * x;
    		y++;
    	}

    	average = sumX / y; //readingSamples.size();
    	
    	float slope = (sumXY - sumX * sumY / y) / (sumX2 - (sumX * sumX) / y); 
    	
    	return slope;
    }
	
	public float getMinimum()
	{
		return minValue;
	}
	
	public float getMaximum()
	{
		return maxValue;
	}
	
	public float getAverage() {
		return average;
	}
}
