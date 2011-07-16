package momenso.barometrum;

public class PressureDataPoint {
	private float value;
	private long time;
	
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
}
