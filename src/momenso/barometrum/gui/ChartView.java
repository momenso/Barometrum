package momenso.barometrum.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import momenso.barometrum.PressureDataPoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class ChartView extends TextView {
	
	private List<PressureDataPoint> data = new ArrayList<PressureDataPoint>();
	
	public ChartView(Context context) {
		super(context);
	}
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//initializeParams(attrs);
	}
	
	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		//initializeParams(attrs);
	}
	
	/*protected void initializeParams(AttributeSet attrs) {
		super.initializeParams(attrs);
		
		String nameSpace = "http://schemas.android.com/apk/res/momenso.barometrum";
		
		this.top = attrs.getAttributeBooleanValue(nameSpace, "border_top", true);
		this.left = attrs.getAttributeBooleanValue(nameSpace, "border_left", true);
		this.right = attrs.getAttributeBooleanValue(nameSpace, "border_right", true);
		this.bottom = attrs.getAttributeBooleanValue(nameSpace, "border_bottom", true);
	}*/
	
	public void updateData(List<PressureDataPoint> data) {
		this.data = data;
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		
		Paint paint = new Paint();
		paint.setTypeface(getTypeface());
		paint.setAntiAlias(true);
		
		RectF borderRect = new RectF();
		borderRect.set(rect.left + 1, rect.top + 1, rect.right - 2, rect.bottom - 2);
		paint.setStyle(Style.FILL);
		paint.setColor(Color.rgb(30, 30, 30));
		canvas.drawRoundRect(borderRect, 15, 15, paint);
		
		paint.setColor(Color.rgb(150, 150, 150));		
		paint.setStrokeWidth(3);		
		paint.setStyle(Style.STROKE);
		canvas.drawRoundRect(borderRect, 15, 15, paint);
		paint.setStrokeWidth(1);
		
		// print graph title
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("Pressure History", rect.centerX(), 20, paint);
		
		// draw axis
		paint.setColor(Color.WHITE);
		canvas.drawLine(rect.left + 10, rect.bottom - 23, rect.right - 10, rect.bottom - 23, paint);
		//canvas.drawLine(rect.left + 10, rect.top + 10, rect.left + 10, rect.bottom - 10, paint);
		
		if (this.data == null || this.data.size() == 0)
			return;
		
		// get range for Y axis 
		float maximum = Float.MIN_VALUE;
		float minimum = Float.MAX_VALUE;
		for (PressureDataPoint bar : this.data) {
			maximum = Math.max(bar.getValue(), maximum);
			minimum = Math.min(bar.getValue(), minimum);
		}
		
		// draw data columns
		paint.setTextSize(getTextSize() / 2);
		int columnWidth = ((rect.width() - 20) / this.data.size()) / 1;
		int xPos = rect.left + 10 + columnWidth / 2;
		for (PressureDataPoint bar : data) {
			Rect barRect = new Rect(xPos - 10, 
					convertY(bar.getValue(), minimum, maximum, rect.height() - 60) + 30, 
					xPos + 10, rect.bottom - 23);
			paint.setColor(Color.rgb(20, 180, 20));
			canvas.drawRect(barRect, paint);
			
			paint.setColor(Color.WHITE);
			Date date = new Date(bar.getTime());
			String label = String.format("%02d:%02d", date.getHours(), date.getMinutes());
			
			canvas.drawText(label, barRect.centerX(), rect.bottom - 5, paint);
			
			xPos += columnWidth;
		}
	}
	
	private int convertY(float value, float min, float max, int height)
	{
		float factor = 1 - (value - min) / (max - min);
		return Math.round(factor * (float)height);
	}
	/*
	private PressureDataPoint[] sample = {
			new PressureDataPoint(3600000L*0, 1010.0F),
			new PressureDataPoint(3600000L*1, 1014.0F),
			new PressureDataPoint(3600000L*2, 1016.0F),
			new PressureDataPoint(3600000L*3, 1010.0F),
			new PressureDataPoint(3600000L*4, 1008.0F),
			new PressureDataPoint(3600000L*5, 1010.0F),
			new PressureDataPoint(3600000L*6, 1010.0F),
			new PressureDataPoint(3600000L*7, 1010.0F),
			new PressureDataPoint(3600000L*8, 1010.0F),
			new PressureDataPoint(3600000L*9, 1010.0F),
			new PressureDataPoint(3600000L*10, 1020.0F),
			new PressureDataPoint(3600000L*11, 1025.0F),
			new PressureDataPoint(3600000L*12, 1010.0F),
			new PressureDataPoint(3600000L*13, 1010.0F),
			new PressureDataPoint(3600000L*14, 1010.0F),
			new PressureDataPoint(3600000L*15, 1010.0F),
			new PressureDataPoint(3600000L*16, 1010.0F),
			new PressureDataPoint(3600000L*17, 1010.0F),
			new PressureDataPoint(3600000L*18, 1010.0F),
			new PressureDataPoint(3600000L*19, 1010.0F),
			new PressureDataPoint(3600000L*20, 1010.0F),
			new PressureDataPoint(3600000L*21, 1010.0F),
			new PressureDataPoint(3600000L*22, 1010.0F),
			new PressureDataPoint(3600000L*23, 1012.0F)
	};
	*/
	/*
	private PressureDataGroup[] sample = {
			new PressureDataGroup(3600000L*0, 3600000L*0, 1000.0F, 1010.0F, 1000.0F, 1010.0F),
			new PressureDataGroup(3600000L*1, 3600000L*2, 1005.0F, 1010.0F, 1010.0F, 1010.0F),
			new PressureDataGroup(3600000L*2, 3600000L*3, 1008.0F, 1017.0F, 1010.0F, 1017.0F),
			new PressureDataGroup(3600000L*3, 3600000L*4, 1008.0F, 1016.0F, 1017.0F, 1015.0F),
			new PressureDataGroup(3600000L*4, 3600000L*5, 1000.0F, 1010.0F, 1015.0F, 1010.0F),
	};
	
	class PressureDataGroup {
		
		private float minimum;
		private float maximum;
		private float open;
		private float close;
		private long start;
		private long end;
		
		public PressureDataGroup(long start, long end, float minimum, float maximum, float open, float close) {
			setStart(start);
			setEnd(end);
			setMinimum(minimum);
			setMaximum(maximum);
			setOpen(open);
			setClose(close);
		}
		
		public float getMinimum() {
			return minimum;
		}

		public void setMinimum(float minimum) {
			this.minimum = minimum;
		}

		public float getMaximum() {
			return maximum;
		}

		public void setMaximum(float maximum) {
			this.maximum = maximum;
		}

		public float getOpen() {
			return open;
		}

		public void setOpen(float open) {
			this.open = open;
		}

		public float getClose() {
			return close;
		}

		public void setClose(float close) {
			this.close = close;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getStart() {
			return start;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public long getEnd() {
			return end;
		}
		
	}*/
}
