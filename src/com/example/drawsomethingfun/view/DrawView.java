package com.example.drawsomethingfun.view;

import com.example.drawsomethingfun.utils.NetworkManager;
import com.protobuftest.protobuf.GameProbuf.Game;
import com.protobuftest.protobuf.GameProbuf.Game.MsgType;
import com.protobuftest.protobuf.GameProbuf.Game.Point;
import com.protobuftest.protobuf.GameProbuf.Game.Point.PointType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
/**
 * 
 * @author yanzhi
 *
 */

@SuppressLint({ "ClickableViewAccessibility", "DrawAllocation" })
public class DrawView extends View {
    float preX;
    float preY;
    private Path path;
    public Paint paint = null;
    int VIEW_WIDTH = 320;
    int VIEW_HEIGHT = 480;
    Bitmap cacheBitmap = null;
    Canvas cacheCanvas = null;
 
    public DrawView(Context context, AttributeSet set) {
        super(context, set);
        init();
    }
    
    boolean drawable = false;
    
    public void setDrawable(boolean drawable)
    {
    	this.drawable = drawable;
    }
    
    public boolean getDrawable()
    {
    	return drawable;
    }
    public void init()
    {
    	DisplayMetrics metrics = new DisplayMetrics();  
    	WindowManager wm = (WindowManager) this.getContext().getSystemService("window");  
        wm.getDefaultDisplay().getMetrics(metrics);  
        VIEW_WIDTH = metrics.widthPixels;  
        VIEW_HEIGHT = metrics.heightPixels;  
    	cacheBitmap = Bitmap.createBitmap(VIEW_WIDTH,VIEW_HEIGHT,
                Config.ARGB_8888);
        cacheCanvas = new Canvas();
        
        path = new Path();
        cacheCanvas.setBitmap(cacheBitmap);
 
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setDither(true);
    }
    
    public void receiceDraw(Point point)
    {
    	//*屏幕宽高解决分辨率问题
    	float x = point.getX()*VIEW_WIDTH;
    	float y = point.getY()*VIEW_HEIGHT;
    	PointType type = point.getType();
    	switch(type.getNumber())
    	{
    	case PointType.ACTION_DOWN_VALUE:
    		path.moveTo(x, y);
            preX = x;
            preY = y;
    		break;
    	case PointType.ACTION_MOVE_VALUE:
    		path.quadTo(preX, preY, x, y);
            preX = x;
            preY = y;
    		break;
    	case PointType.ACTION_UP_VALUE:
    		cacheCanvas.drawPath(path, paint);
            path.reset();
    		break;
    	}
    	invalidate();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(drawable == false)
    		return false;
    	
        float x = event.getX();
        float y = event.getY();
        Point p = null;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	p = Point.newBuilder().setX(x/VIEW_WIDTH).setY(y/VIEW_HEIGHT).setType(PointType.ACTION_DOWN).build();
            path.moveTo(x, y);
            preX = x;
            preY = y;
            break;
        case MotionEvent.ACTION_MOVE:
        	p = Point.newBuilder().setX(x/VIEW_WIDTH).setY(y/VIEW_HEIGHT).setType(PointType.ACTION_MOVE).build();
        	
            path.quadTo(preX, preY, x, y);
            preX = x;
            preY = y;
            break;
        case MotionEvent.ACTION_UP:
        	p = Point.newBuilder().setX(x/VIEW_WIDTH).setY(y/VIEW_HEIGHT).setType(PointType.ACTION_UP).build();
        	
            cacheCanvas.drawPath(path, paint);
            path.reset();
            break;
        }
        Game game = Game.newBuilder().setPoint(p).setType(MsgType.POINT).build();
        NetworkManager.sendMessage(game.toByteArray());
        invalidate();
        return true;
    }
 
    public void clear()
    {
    	path.reset();
    	cacheBitmap = Bitmap.createBitmap(VIEW_WIDTH,VIEW_HEIGHT,
                Config.ARGB_8888);
    	cacheCanvas.setBitmap(cacheBitmap);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint bmpPaint = new Paint();
        canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);
        canvas.drawPath(path, paint);
    }
 
}