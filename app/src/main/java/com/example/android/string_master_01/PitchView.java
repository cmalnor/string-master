package com.example.android.string_master_01;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by codymalnor on 11/13/17.
 */

public class PitchView extends View {

    private static final String TAG = "GuitarTuner";

    private float centerPitch, currentPitch, newPitch;
    private int width, height;

    private final Paint paint = new Paint();
    private ValueAnimator needleAnimation = new ValueAnimator();


    public PitchView(Context context) {
        super(context);
        Log.d(TAG, "PitchView: 1");
        needleAnimation.setDuration(100);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setTunerPitch((float) valueAnimator.getAnimatedValue());
                Log.d(TAG, "Current pitch value: " + getCurrentPitch());
            }
        });
    }
    public PitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "PitchView: 2");
        needleAnimation.setDuration(100);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.d(TAG, "Current pitch value: ");

                setTunerPitch((float) valueAnimator.getAnimatedValue());
            }
        });
    }
    public PitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "PitchView: 3");
        needleAnimation.setDuration(100);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setTunerPitch((float) valueAnimator.getAnimatedValue());
                Log.d(TAG, "Current pitch value: " + getCurrentPitch());
            }
        });
    }

    //Sets note to tune to.
    //Input: MIDI VALUE of desired pitch
    public void setCenterPitch(float centerPitch) {
        this.centerPitch = centerPitch;
        invalidate();
    }

    //Sets current pitch on tuner
    public void setTunerPitch(float pitch) {
        this.currentPitch = pitch;
        invalidate();
    }

    //Sets current pitch read from mic
    public void setNewPitch(float newPitch){
        this.newPitch = newPitch;

        //Stop needle animation where it is (if in progress) and begin moving to new value
        if (needleAnimation.isRunning()){
            setTunerPitch((float) needleAnimation.getAnimatedValue());

            needleAnimation.end();
           // setCurrentPitch((float) needleAnimation.getAnimatedValue());
            Log.d(TAG, "ended animation");
        }
        needleAnimation = ValueAnimator.ofFloat(this.currentPitch, this.newPitch);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setTunerPitch((float) valueAnimator.getAnimatedValue());
                Log.d(TAG, "Current pitch value: " + getCurrentPitch());

            }
        });
        needleAnimation.start();
        Log.d(TAG, "setNewPitch: started animation");

        invalidate();
    }

    public float getCenterPitch(){
        return centerPitch;
    }

    public float getCurrentPitch(){
        return currentPitch;
    }

    public float getNewPitch(){
        return newPitch;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }
/*
    @Override
    protected void onDraw(Canvas canvas) {
        float halfWidth = width / 2;
        paint.setStrokeWidth(6.0f);
        paint.setColor(Color.GREEN);
        canvas.drawLine(halfWidth, 0, halfWidth, height, paint);
        float dx = (currentPitch - centerPitch) / 2;
        if (-5 < dx && dx < 5) {
            paint.setStrokeWidth(2.0f);
            paint.setColor(Color.BLUE);
        } else {
            paint.setStrokeWidth(8.0f);
            paint.setColor(Color.RED);
            dx = (dx < 0) ? -5 : 5;
        }
        double phi = (dx / 5) * Math.PI / 4;
        canvas.drawLine(halfWidth, height, halfWidth + (float) Math.sin(phi) * height * 0.9f,
                height - (float) Math.cos(phi) * height * 0.9f, paint);
    }*/
    @Override
    protected void onDraw(Canvas canvas){

        int span = 1;

        //Draw midline
        float mid = this.width / 2;
        float startX = mid;
        paint.setStrokeWidth(10.0f);
        paint.setColor(Color.GREEN);
        canvas.drawLine(mid, 10, mid, this.height - 10, this.paint);

        //Draw freq needle
        float dx = (this.currentPitch - this.centerPitch);

        this.paint.setStrokeWidth(5.0f);
        this.paint.setColor(Color.BLUE);

        startX = mid + (dx * (mid/span));
        canvas.drawLine(startX, 10, startX, this.height - 10, this.paint);
    }
}
