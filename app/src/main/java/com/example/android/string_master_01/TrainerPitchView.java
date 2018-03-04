package com.example.android.string_master_01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by codymalnor on 2/12/18.
 */

public class TrainerPitchView extends View {

    private Paint centerPaint;
    private Paint leftPaint;
    private Paint rightPaint;
    private int padding;
    Rect leftRect;
    Rect rightRect;
    Rect centerRect;
    private int width, height;
    private float centerPitch;


    final String TAG = "trainerPitchView";

    public TrainerPitchView(Context context){
        super(context);
        init();
    }

    public TrainerPitchView(Context context, AttributeSet attrs){
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
        padding = 16;
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
        Log.d(TAG, "init: height: " + width);

    }
    
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
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawRect(leftRect, leftPaint);
        canvas.drawRect(rightRect, rightPaint);
        canvas.drawRect(centerRect, centerPaint);
        Log.d(TAG, "onDraw");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        init();
    }

    //Sets note to tune to.
    //Input: MIDI VALUE of desired pitch
    public void setCenterPitch(float centerPitch) {
        this.centerPitch = centerPitch;
        invalidate();
    }

    // Return MIDI note value as a float for current reference pitch
    public float getCenterPitch(){
        return centerPitch;
    }
}
