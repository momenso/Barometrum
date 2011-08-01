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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result + Float.floatToIntBits(value);
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PressureDataPoint)) {
			return false;
		}
		PressureDataPoint other = (PressureDataPoint) obj;
		if (time != other.time) {
			return false;
		}
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) {
			return false;
		}
		
		return true;
	}
	
	
	
}
