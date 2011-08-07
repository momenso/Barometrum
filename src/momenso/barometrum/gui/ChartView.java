package momenso.barometrum.gui;

import java.util.Date;
import java.util.List;

import momenso.barometrum.PressureDataPoint;
import momenso.barometrum.ReadingsData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class ChartView extends TextView {
	
	private ReadingsData data;
	
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
	
	public void updateData(ReadingsData data) {
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
		
		if (this.data == null)
			return;
		
		// get range for Y axis 
		float maximum = data.getMaximumValue().getRawValue();
		float minimum = data.getMinimumValue().getRawValue();
		
		// draw data columns
		List<PressureDataPoint> values = data.getHistory();
		paint.setTextSize((getTextSize() * 5) / 6);
		int columnWidth = ((rect.width() - 20) / values.size()) / 1;
		int xPos = rect.left + 10 + columnWidth / 2;
		for (PressureDataPoint bar : values) {
			Rect barRect = new Rect(xPos - 10, 
				convertY(bar.getRawValue(), minimum, maximum, rect.height() - 48) + 25, 
				xPos + 10, rect.bottom - 23);
			paint.setColor(Color.rgb(20, 180, 20));
			canvas.drawRect(barRect, paint);
			
			paint.setColor(Color.WHITE);
			Date date = new Date(bar.getTime());
			String label = String.format("%02d:%02d", date.getHours(), date.getMinutes());
			//String label = String.format("%dh", date.getHours());
			canvas.drawText(label, barRect.centerX(), rect.bottom - 7, paint);
			
			// display pressure value
			//String value = String.format("%.2f", Math.round(ReadingsData.getReadingValue(bar) * 100.0) / 100.0);
			//canvas.drawText(value, barRect.centerX(), barRect.top - 5, paint);
			
			xPos += columnWidth;
		}
	}
	
	private int convertY(float value, float min, float max, int height)
	{
		float factor = 1 - (value - min) / (max - min);
		int y = Math.round(factor * (float)height);
		//Log.v("GRAPH", 
		//	String.format("ConvertY(%.2f,%.2f,%.2f,%d) = %d ", value, min, max, height, y));
		
		return y;
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
}
