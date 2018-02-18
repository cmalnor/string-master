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

    private Paint centerLinePaint;
    private Paint pitchLinePaint;
    private ValueAnimator needleAnimation = new ValueAnimator();


    public PitchView(Context context) {
        super(context);
        init();
    }

    public PitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
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
                //Log.d(TAG, "Current pitch value: " + getCurrentPitch());

            }
        });
        needleAnimation.start();
        Log.d(TAG, "setNewPitch: started animation");

        invalidate();
    }

    // Return MIDI note value as a float for current reference pitch
    public float getCenterPitch(){
        return centerPitch;
    }

    // Return MIDI note value as a float for current played pitch
    public float getCurrentPitch(){
        return currentPitch;
    }

    public float getNewPitch(){
        return newPitch;
    }

    public Paint getCenterLinePaint(){
        return centerLinePaint;
    }

    public Paint getPitchLinePaint(){
        return pitchLinePaint;
    }

    public int getViewHeight(){
        return height;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void init(){
        centerLinePaint = new Paint();
        centerLinePaint.setStrokeWidth(10.0f);
        centerLinePaint.setColor(Color.GREEN);

        pitchLinePaint = new Paint();
        pitchLinePaint.setStrokeWidth(5.0f);
        pitchLinePaint.setColor(Color.BLUE);

        needleAnimation.setDuration(100);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Log.d(TAG, "Current pitch value: " + getCurrentPitch());
                setTunerPitch((float) valueAnimator.getAnimatedValue());
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //Draw midline
        float mid = this.width / 2;
        float startX = mid;
        canvas.drawLine(mid, 10, mid, this.height - 10, centerLinePaint);

        //Draw freq needle
        float dx = (this.currentPitch - this.centerPitch);
        startX = mid + (dx * mid);
        canvas.drawLine(startX, 10, startX, this.height - 10, pitchLinePaint);

    }
}
