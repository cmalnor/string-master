package com.example.android.string_master_01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by codymalnor on 2/12/18.
 */

public class trainerPitchView extends PitchView {

    private Paint centerPaint;
    private Paint sidePaint;
    private Paint leftPaint;
    private Paint rightPaint;
    private int padding;
    Rect leftRect;
    Rect rightRect;
    Rect centerRect;
    private int width, height;

    final String TAG = "trainerPitchView";

    public trainerPitchView(Context context){
        super(context);
        init();
    }

    public trainerPitchView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        leftPaint = new Paint();
        leftPaint.setStyle(Paint.Style.FILL);
        leftPaint.setColor(getResources().getColor(R.color.sidePaintOff));
        centerPaint = new Paint();
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(getResources().getColor(R.color.centerPaintOff));
        rightPaint = new Paint();
        rightPaint.setStyle(Paint.Style.FILL);
        rightPaint.setColor(getResources().getColor(R.color.sidePaintOff));
        padding = 50;
        leftRect = new Rect(
                padding,
                padding,
                width/2-width/10,
                height-padding);
        rightRect = new Rect(
                width/2+width/10,
                padding,
                width-padding,
                height-padding);
        centerRect = new Rect(
                width/2-width/10,
                padding,
                width/2+width/10,
                height-padding);
        Log.d(TAG, "init: height: " + height);

    }

    @Override
    public void setNewPitch(float newPitch){
        if (newPitch > getCenterPitch()-1 && newPitch < getCenterPitch()+1){
            rightPaint.setColor(getResources().getColor(R.color.sidePaintOff));
            centerPaint.setColor(getResources().getColor(R.color.centerPaintOn));
            leftPaint.setColor(getResources().getColor(R.color.sidePaintOff));
        } else if (newPitch < getCenterPitch()-1){
            rightPaint.setColor(getResources().getColor(R.color.sidePaintOff));
            centerPaint.setColor(getResources().getColor(R.color.centerPaintOff));
            leftPaint.setColor(getResources().getColor(R.color.sidePaintOn));
        } else {
            rightPaint.setColor(getResources().getColor(R.color.sidePaintOn));
            centerPaint.setColor(getResources().getColor(R.color.centerPaintOff));
            leftPaint.setColor(getResources().getColor(R.color.sidePaintOff));
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        init();
        Log.d(TAG, "onSizeChanged: ");
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawRect(leftRect, leftPaint);
        canvas.drawRect(rightRect, rightPaint);
        canvas.drawRect(centerRect, centerPaint);
        Log.d(TAG, "onDraw");
    }
}
