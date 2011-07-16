package momenso.barometrum;

import java.util.ArrayList;

public class PressureData {
	private ArrayList<PressureDataPoint> readingSamples;
	private long eventTime;
	private float minValue = Float.MAX_VALUE;
	private float maxValue = Float.MIN_VALUE;

	public PressureData() {
		readingSamples = new ArrayList<PressureDataPoint>();
		eventTime = System.currentTimeMillis();
	}

	public void add(float pressureValue) {
		
		if (readingSamples.size() > 100) {
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
	
	public Number[] get()
	{
		PressureDataPoint[] values = readingSamples.toArray(new PressureDataPoint[0]);
    	Number[] data = new Number[values.length];
    	
    	int i = 0;
    	for (PressureDataPoint m : values) {
    		data[i++] = m.getValue();
    	}
    	
    	return data;
	}
	
	public String getTrend()
    {
		// wait for a minimum reading samples
		if (readingSamples.size() < 80) {
			return "...";
		}
		
		// calculates the slope of the trend line
    	float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
		float y = 0;
    	for (PressureDataPoint point : readingSamples) {
    		float x = point.getValue();
    		sumX += x;
    		sumY += y;
    		sumXY += x*y;
    		sumX2 += x*x;
    		y++;
    	}
    	
    	float slope = (sumXY - sumX * sumY / y) / (sumX2 - (sumX * sumX) / y); 
    	
    	// assigns a trend
    	if (slope > 3.5) {
    		return "Up";
    	} else if (slope < -3.5) {
    		return "Down";
    	} else {
    		return "Stable";
    	}
    }
	
	public float getMinimum()
	{
		return minValue;
	}
	
	public float getMaximum()
	{
		return maxValue;
	}
	
}
