package com.example.drawsomethingfun.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class DrawLine extends LinearLayout {

	private Paint mPaint = null;

	public DrawLine(Context context, Paint paint) {
		super(context);
		mPaint = new Paint();
	}

	public DrawLine(Context context, AttributeSet attr) {
		super(context, attr);
		mPaint = new Paint();
	}

	@SuppressLint("NewApi")
	public DrawLine(Context context, AttributeSet attr, int style) {
		super(context, attr, style);
		mPaint = new Paint();
	}

	public void onPaintChanged(Paint paint) {
		mPaint = paint;
		invalidate();
	}

	public void onDraw(Canvas canvas) {
		float width = (float) this.getWidth();
		float height = (float) this.getHeight() / 2;
		canvas.drawLine(0, height, width, height, mPaint);
	}

	public void setPaint(Paint paint) {
		mPaint = paint;
	}

	public Paint getPaint() {
		return mPaint;
	}
}
