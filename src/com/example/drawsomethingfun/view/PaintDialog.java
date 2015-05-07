package com.example.drawsomethingfun.view;



import com.example.drawsomethingfun.R;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class PaintDialog extends AlertDialog {

	private SeekBar mSeekBar;
	private TextView mTextSeek;
	private Paint mPaint;
	private Paint mPaintTemp;
	private MaskFilter mBlur;
	private MaskFilter mEmboss;
	private Button mButtonColor;
	private DrawLine mDrawLine;

	private int mColor;
	private int mPenWidth = 1;

	private OnPaintChangedListener mListener;

	public interface OnPaintChangedListener {
		public void onPaintChanged(Paint paint);
	}

	public void setOnPaintChangedListener(OnPaintChangedListener listener) {
		mListener = listener;
	}

	public PaintDialog(Context context) {
		super(context);
	}

	public PaintDialog(Context context, int theme) {
		super(context, theme);
	}

	@SuppressLint("InflateParams")
	public void initDialog(Context context, Paint paint) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("set paint");
		View paintView = getLayoutInflater().inflate(R.layout.set_paint, null);
		builder.setView(paintView);

		mPaintTemp = new Paint(paint);
		mPaint = paint;
		mBlur = new BlurMaskFilter(13, BlurMaskFilter.Blur.NORMAL);
		mEmboss = new EmbossMaskFilter(new float[] { 1.0f, 1.0f, 1.0f }, 0.4f,
				6, 3.5f);
		mSeekBar = (SeekBar) paintView.findViewById(R.id.seekbarPenWidth);
		mTextSeek = (TextView) paintView.findViewById(R.id.textPenWidth);
		mButtonColor = (Button) paintView.findViewById(R.id.btnColor);
		mDrawLine = (DrawLine) paintView.findViewById(R.id.lineShow);
		mPenWidth = (int) mPaint.getStrokeWidth();
		mColor = mPaint.getColor();

		initSeekBar(mSeekBar, mPenWidth);
		initButton(mButtonColor);
		initDrawLine(mDrawLine);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mPaint = mPaintTemp;
						if (mPaint != null) {
							mListener.onPaintChanged(mPaint);
						}
					}
				});
		
		builder.create().show();
	}

	private void initSeekBar(SeekBar seekBar, int width) {
		seekBar.setOnSeekBarChangeListener(new SeekBarListener());
		seekBar.setProgress(width);
	}

	private void initButton(Button button) {
		button.setBackgroundColor(mPaint.getColor());
		button.setOnClickListener(new ColorClickListener());
	}

	private void initDrawLine(DrawLine line) {
		line.setPaint(mPaint);
	}

	public class SeekBarListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			mSeekBar.setProgress(progress);
			mPaint.setStrokeWidth(progress);
			mPenWidth = progress;
			mTextSeek.setText("画笔宽度:" + progress);

			if (mListener != null) {
				mListener.onPaintChanged(mPaint);
				mDrawLine.onPaintChanged(mPaint);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}
	}

	public class ColorClickListener implements View.OnClickListener,
			ColorPickerDialog.OnColorChangedListener {
		public void onClick(View view) {
			boolean bAlphaSliderEnabled = false;
			boolean bHexValueEnabled = false;
			//Bundle state = null;
			ColorPickerDialog dialog = new ColorPickerDialog(getContext(),
					Color.BLACK);
			dialog.setOnColorChangedListener(this);
			if (bAlphaSliderEnabled) {
				dialog.setAlphaSliderVisible(true);
			}
			if (bHexValueEnabled) {
				dialog.setHexValueEnabled(true);
			}
			dialog.show();
		}

		public void onColorChanged(int color) {
			mPaint.setColor(color);
			mButtonColor.setBackgroundColor(color);
			mColor = color;
			if (mListener != null) {
				mListener.onPaintChanged(mPaint);
				mDrawLine.onPaintChanged(mPaint);
			}
		}
	}

	public void setPaint(Paint paint) {
		mPaint = paint;
	}

	public Paint getPaint() {
		return mPaint;
	}

}
