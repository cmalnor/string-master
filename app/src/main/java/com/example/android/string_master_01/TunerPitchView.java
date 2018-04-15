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

public class TunerPitchView extends View {

    private static final String TAG = "TunerPitchView";

    private float currentPitch;
    private Paint centerLinePaint;
    private Paint pitchLinePaint;
    private ValueAnimator needleAnimation = new ValueAnimator();
    private int width, height;
    private float centerPitch;

    public TunerPitchView(Context context) {
        super(context);
        init();
    }

    public TunerPitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TunerPitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        centerLinePaint = new Paint();
        centerLinePaint.setStrokeWidth(10.0f);
        centerLinePaint.setColor(Color.GREEN);

        pitchLinePaint = new Paint();
        pitchLinePaint.setStrokeWidth(5.0f);
        pitchLinePaint.setColor(Color.BLUE);

        needleAnimation.setDuration(300);
        needleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Log.d(TAG, "Current pitch value: " + getCurrentPitch());
                setTunerPitch((float) valueAnimator.getAnimatedValue());
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw midline
        float mid = width / 2;
        float startX = mid;
        canvas.drawLine(mid, 10, mid, this.height - 10, centerLinePaint);

        //Draw freq needle
        float dx = (currentPitch - getCenterPitch()) / 2;
        startX = mid + (dx * mid);

        canvas.drawLine(startX, 10, startX, this.height - 10, pitchLinePaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    //Sets current pitch on tuner
    public void setTunerPitch(float pitch) {
        this.currentPitch = pitch;
        invalidate();
    }

    //Sets current pitch read from mic
    public void setNewPitch(float newPitch) {

        //Stop needle animation where it is (if in progress) and begin moving to new value
        if (needleAnimation.isRunning()) {
            setTunerPitch((float) needleAnimation.getAnimatedValue());

            needleAnimation.end();
           // setCurrentPitch((float) needleAnimation.getAnimatedValue());
            Log.d(TAG, "ended animation");
        }
        needleAnimation = ValueAnimator.ofFloat(this.currentPitch, newPitch);
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

    //Sets note to tune to.
    //Input: MIDI VALUE of desired pitch
    public void setCenterPitch(float centerPitch) {
        this.centerPitch = centerPitch;
        invalidate();
    }

    // Return MIDI note value as a float for current reference pitch
    public float getCenterPitch() {
        return centerPitch;
    }
}
