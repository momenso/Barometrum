package momenso.barometrum.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

	public CustomTextView(Context context) {
		super(context);
	}
	
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super (context, attrs, defStyle);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(Color.rgb(30, 30, 30));
		paint.setStrokeWidth(3);

		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		rect.set(rect.left + 3, rect.top + 3, rect.right - 3, rect.bottom - 3); 
		canvas.drawRoundRect(new RectF(rect), 15, 15, paint);
		
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.rgb(150, 150, 150));
		canvas.drawRoundRect(new RectF(rect), 15, 15, paint);
		
		super.onDraw(canvas);
	}
	
}
