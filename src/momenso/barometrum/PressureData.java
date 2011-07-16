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
	
	public String getTendency()
    {
    	float average = (minValue + maxValue) / 2;
    	
    	int above = 0, same = 0, below = 0;
    	for (PressureDataPoint point : readingSamples) {
    		if (point.getValue() > average) above++;
    		if (point.getValue() == average) same++;
    		if (point.getValue() < average) below++;
    	}
    	
    	if (above > same && above > below) {
    		return "up";
    	} else if (below > same && below > above) {
    		return "down";
    	} else {
    		return "stable";
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
