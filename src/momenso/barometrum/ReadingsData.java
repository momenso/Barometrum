package momenso.barometrum;

import java.util.ArrayList;
import java.util.List;


public class ReadingsData {
	
	private List<PressureDataPoint> readingSamples;
	private PressureDataPoint minValue = new PressureDataPoint(0, Float.MAX_VALUE);
	private PressureDataPoint maxValue = new PressureDataPoint(0, Float.MIN_VALUE);
	private float average = 0;

	public ReadingsData() 
	{
		readingSamples = new ArrayList<PressureDataPoint>();
	}

	public void add(float pressureValue) 
	{
		if (readingSamples.size() > 500) {
    		readingSamples.remove(0);
    	}
		
		updateMinMax();
		
    	PressureDataPoint newSample = 
    		new PressureDataPoint((System.currentTimeMillis()/* - eventTime*/), pressureValue);
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
			data.add(m.getValue());
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
    		((last.getTime() - mark.getTime()) / 1000);
    }
	
	public float getMinimum()
	{
		return minValue.getValue();
	}
	
	public float getMaximum()
	{
		return maxValue.getValue();
	}
	
	public float getAverage() 
	{
		return average;
	}

	public void clear() {
		this.readingSamples.clear();
	}
}
