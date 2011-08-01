package momenso.barometrum.gui;

import java.security.InvalidParameterException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class BlockView extends LabeledTextView {

	private enum Position { LEFT, TOP, RIGHT, BOTTOM };
	private enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT };
	private boolean top, left, right, bottom;
	private Rect rect;
	Paint paint;
	
	int cornerXRadius = 15;
	int cornerYRadius = 15;
	int lineWidth = 3;
	int backgroundColor = Color.rgb(30, 30, 30);
	int borderColor = Color.rgb(150, 150, 150);
	
	public BlockView(Context context) {
		super(context);
	}
	
	public BlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initializeParams(attrs);
	}
	
	public BlockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initializeParams(attrs);
	}
	
	protected void initializeParams(AttributeSet attrs) {
		super.initializeParams(attrs);
		
		String nameSpace = "http://schemas.android.com/apk/res/momenso.barometrum";
		
		this.top = attrs.getAttributeBooleanValue(nameSpace, "border_top", true);
		this.left = attrs.getAttributeBooleanValue(nameSpace, "border_left", true);
		this.right = attrs.getAttributeBooleanValue(nameSpace, "border_right", true);
		this.bottom = attrs.getAttributeBooleanValue(nameSpace, "border_bottom", true);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
				
		rect = new Rect();
		paint = new Paint();
		paint.setStrokeWidth(lineWidth);
		
		// define border limits
		getLocalVisibleRect(rect);
		rect.set(rect.left + lineWidth / 2, rect.top + lineWidth / 2, rect.right - lineWidth / 2 - 1, rect.bottom - lineWidth / 2 - 1);
		
		// fill inside the round border
		paint.setStyle(Style.FILL);
		paint.setColor(backgroundColor);
		RectF roundBorder = new RectF(rect);
		canvas.drawRoundRect(roundBorder, cornerXRadius, cornerYRadius, paint);
		
		// draw round border
		paint.setStyle(Style.STROKE);
		paint.setColor(borderColor);
		canvas.drawRoundRect(new RectF(rect), cornerXRadius, cornerYRadius, paint);
		
		// remove disabled borders
		paint.setStyle(Style.FILL_AND_STROKE);
		if (!top) {
			removeCorner(canvas, Corner.TOP_LEFT);
			removeCorner(canvas, Corner.TOP_RIGHT);
			
			removeBorderLine(canvas, Position.TOP);
			
			if (left)
				straightenBorder(canvas, Position.LEFT, Position.TOP);
			if (right)
				straightenBorder(canvas, Position.RIGHT, Position.TOP);
		}
		if (!left) {
			removeCorner(canvas, Corner.TOP_LEFT);
			removeCorner(canvas, Corner.BOTTOM_LEFT);
			
			removeBorderLine(canvas, Position.LEFT);
			
			if (top)
				straightenBorder(canvas, Position.TOP, Position.LEFT);
			if (bottom)
				straightenBorder(canvas, Position.BOTTOM, Position.LEFT);
		}
		if (!right) {
			removeCorner(canvas, Corner.TOP_RIGHT);
			removeCorner(canvas, Corner.BOTTOM_RIGHT);
			
			removeBorderLine(canvas, Position.RIGHT);
			
			if (top)
				straightenBorder(canvas, Position.TOP, Position.RIGHT);
			if (bottom)
				straightenBorder(canvas, Position.BOTTOM, Position.RIGHT);
		}
		if (!bottom) {
			removeCorner(canvas, Corner.BOTTOM_LEFT);
			removeCorner(canvas, Corner.BOTTOM_RIGHT);
			
			removeBorderLine(canvas, Position.BOTTOM);
			
			if (left)
				straightenBorder(canvas, Position.LEFT, Position.BOTTOM);
			if (right)
				straightenBorder(canvas, Position.RIGHT, Position.BOTTOM);
		}
		
		super.onDraw(canvas);
	}
	
	private void removeBorderLine(Canvas canvas, Position border) {
		
		if (canvas == null) {
			throw new InvalidParameterException();
		}
		
		paint.setColor(backgroundColor);
		
		switch (border) {
			case TOP:
				canvas.drawLine(rect.left + cornerXRadius, rect.top, rect.right - cornerXRadius, rect.top, paint);
				break;
				
			case BOTTOM:
				canvas.drawLine(rect.left + cornerXRadius, rect.bottom, rect.right - cornerXRadius, rect.bottom, paint);
				break;
				
			case LEFT:
				canvas.drawLine(rect.left, rect.top + cornerYRadius, rect.left, rect.bottom - cornerYRadius, paint);
				break;
				
			case RIGHT:
				canvas.drawLine(rect.right, rect.top + cornerYRadius, rect.right, rect.bottom - cornerYRadius, paint);
				break;
		}
	}
	
	private void removeCorner(Canvas canvas, Corner location) {
		
		if (canvas == null) {
			throw new InvalidParameterException();
		}
		
		paint.setColor(backgroundColor);
		
		switch (location) {
			case TOP_LEFT:
				canvas.drawRect(rect.left - lineWidth / 2, rect.top - lineWidth / 2, rect.left + cornerXRadius, rect.top + cornerYRadius, paint);
				break;
				
			case TOP_RIGHT:
				canvas.drawRect(rect.right - cornerXRadius, rect.top - 2, rect.right + lineWidth / 2, rect.top + cornerYRadius, paint);
				break;
				
			case BOTTOM_LEFT:
				canvas.drawRect(rect.left - lineWidth / 2, rect.bottom - cornerYRadius, rect.left + cornerXRadius, rect.bottom + lineWidth / 2, paint);
				break;
				
			case BOTTOM_RIGHT:
				canvas.drawRect(rect.right - cornerXRadius, rect.bottom - cornerYRadius, rect.right + lineWidth / 2, rect.bottom + lineWidth / 2, paint);
				break;
		}
	}
	
	private void straightenBorder(Canvas canvas, Position border, Position corner) {
		
		if (canvas == null) {
			throw new InvalidParameterException();
		}
		
		paint.setColor(borderColor);

		switch (border)
		{
			case TOP:
				switch (corner)
				{
					case LEFT:
						canvas.drawLine(rect.left - lineWidth, rect.top, rect.left + cornerXRadius + lineWidth, rect.top, paint);
						break;
						
					case RIGHT:
						canvas.drawLine(rect.right - cornerXRadius - 1, rect.top, rect.right + lineWidth, rect.top, paint);
						break;
						
					default:
						throw new InvalidParameterException();
				}
				break;
				
			case BOTTOM:
				switch (corner)
				{
					case LEFT:
						canvas.drawLine(rect.left - lineWidth, rect.bottom, rect.left + cornerXRadius + lineWidth, rect.bottom, paint);
						break;
						
					case RIGHT:
						canvas.drawLine(rect.right - cornerXRadius - 1, rect.bottom, rect.right + lineWidth, rect.bottom, paint);
						break;
						
					default:
						throw new InvalidParameterException();
				}
				break;
				
			case LEFT:
				switch (corner)
				{
					case TOP:
						canvas.drawLine(rect.left, rect.top - lineWidth, rect.left, rect.top + cornerYRadius + lineWidth, paint);
						break;
						
					case BOTTOM:
						canvas.drawLine(rect.left, rect.bottom - cornerYRadius - lineWidth, rect.left, rect.bottom + lineWidth, paint);
						break;
						
					default:
						throw new InvalidParameterException();
				}
				break;
				
			case RIGHT:
				switch (corner)
				{
					case TOP:
						canvas.drawLine(rect.right, rect.top - lineWidth, rect.right, rect.top + cornerYRadius + lineWidth, paint);
						break;
						
					case BOTTOM:
						canvas.drawLine(rect.right, rect.bottom - cornerYRadius - lineWidth, rect.right, rect.bottom + lineWidth, paint);
						break;
						
					default:
						throw new InvalidParameterException();
				}				
				break;
				
			default:
				throw new InvalidParameterException();
		}
	}
}
