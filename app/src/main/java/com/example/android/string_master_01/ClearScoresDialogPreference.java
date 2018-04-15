package com.example.android.string_master_01;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by codymalnor on 3/20/18.
 */

public class ClearScoresDialogPreference extends DialogPreference {

    private static final String TAG = "ClearScoresPref";

    private int dialogLayoutResId = R.layout.clear_scores_dialog_pref;

    public ClearScoresDialogPreference(Context context){
        this(context, null);
    }

    public ClearScoresDialogPreference(Context context, AttributeSet  attrs) {
        this(context, attrs, 0);
    }

    public ClearScoresDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public  ClearScoresDialogPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getDialogLayoutResource() {
        return dialogLayoutResId;
    }

    public void eraseScores() {
        Log.d(TAG, "eraseScores: Erasing scores!");
    }
}
