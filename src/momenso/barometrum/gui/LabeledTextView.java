package momenso.barometrum.gui;

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

public class LabeledTextView extends TextView {

	private Paint paint;
	private String text;
	private String label;
	private String unit;
	private int labelWidth;
	
	public LabeledTextView(Context context) {
		super(context);
		initializeState();
	}
	
	public LabeledTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeState();
		initializeParams(attrs);
	}
	
	public LabeledTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeState();
		initializeParams(attrs);
	}
	
	private void initializeState() {
		paint = new Paint();
	}
	
	protected void initializeParams(AttributeSet attrs) {
		String nameSpace = "http://schemas.android.com/apk/res/momenso.barometrum";
		String androidNameSpace = "http://schemas.android.com/apk/res/android";
		
		if ((this.label = attrs.getAttributeValue(nameSpace, "label")) == null)
			label = "label";
		if ((this.text = attrs.getAttributeValue(androidNameSpace, "text")) == null)
			text = "0.00";
		if ((this.unit = attrs.getAttributeValue(nameSpace, "unit")) == null)
			unit = "unit";
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		
		float originalTextSize = getTextSize();
		paint.setTypeface(getTypeface());
		paint.setAntiAlias(true);

		Rect bounds = new Rect();
		
		try {
			if (this.label != null) {
				paint.setTextSize(originalTextSize);
				//paint.getTextBounds(this.label, 0, this.label.length(), bounds);
				paint.getTextBounds("X", 0, 1, bounds);
				bounds.offset(5, rect.centerY());
				int width = this.labelWidth > 0 ? labelWidth : this.label.length(); 
				bounds.set(bounds.left + 5, bounds.top - 5, bounds.right * width + 15, bounds.bottom + 6);
				
				// fill tag background
				paint.setStyle(Style.FILL);
				paint.setColor(Color.rgb(83, 83, 0));
				canvas.drawRoundRect(new RectF(bounds), 3, 3, paint);
				
				// draw tag border
				paint.setStyle(Style.STROKE);
				paint.setColor(Color.rgb(183, 183, 0));
				canvas.drawRoundRect(new RectF(bounds), 3, 3, paint);
				
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.CENTER);
				canvas.drawText(this.label, bounds.centerX(), bounds.bottom - 5, paint);
			}
			
			int right = 3 + bounds.right;
			
			if (this.text != null) {
				paint.setTextSize(originalTextSize + 3);
				int left = right + 3;
				right += paint.measureText(this.text) + 10;
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(this.text, rect.left + left, rect.centerY(), paint);
			}
			if (this.unit != null) { 
				paint.setTextSize(originalTextSize - 3);
				paint.getTextBounds(this.unit, 0, this.unit.length(), bounds);
				bounds.offset(right, rect.centerY());
				bounds.set(bounds.left, bounds.top - 5, bounds.right + 10, bounds.bottom + 6);
				
				// fill tag background
				paint.setStyle(Style.FILL);
				paint.setColor(Color.rgb(0, 183, 0));
				canvas.drawRoundRect(new RectF(bounds), 3, 3, paint);
				
				// draw tag border
				paint.setStyle(Style.STROKE);
				paint.setColor(Color.rgb(0, 253, 0));
				canvas.drawRoundRect(new RectF(bounds), 3, 3, paint);
	
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(this.unit, bounds.left + 5, rect.centerY(), paint);
			}
		} catch (UnsupportedOperationException e) {
			// handle displaying component in the designer
			paint.setColor(Color.GRAY);
			canvas.drawRect(rect, paint);
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.CENTER);
			canvas.drawText("LabeledTextView", rect.centerX(), rect.centerY(), paint);
		}
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public void setLabelWidth(int width) {
		this.labelWidth = width;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	    //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	    
	    this.setMeasuredDimension(parentWidth, (int)(getTextSize() + 15));
	}
	
}
