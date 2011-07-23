package momenso.barometrum;

import java.io.Serializable;

public class PressureDataPoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3959936631531969908L;
	private float value;
	private long time;
	
	public PressureDataPoint() {
		this.setValue(0);
		this.setTime(0);
	}
	
	public PressureDataPoint(long time, float value) {
		this.setValue(value);
		this.setTime(time);
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}
	
	public void reset() {
		this.time = 0;
		this.value = 0;
	}
	
}
