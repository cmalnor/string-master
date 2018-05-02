package com.example.android.string_master_01;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by codymalnor on 3/20/18.
 */

public class ClearScoresDialogPreference extends DialogPreference {

    private static final String TAG = "ClearScoresPref";

    private int dialogLayoutResId;
    private SharedPreferences sharedPreferences;
    private String KEY_GAME_LENGTH;
    private String KEY_NUMBER_FRETS;
    private String KEY_SHARPS;
    private String KEY_FLATS;

    public ClearScoresDialogPreference(Context context){
        this(context, null);
    }

    public ClearScoresDialogPreference(Context context, AttributeSet  attrs) {
        this(context, attrs, 0);
    }

    public ClearScoresDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public ClearScoresDialogPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        KEY_NUMBER_FRETS = context.getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS);
        KEY_GAME_LENGTH = context.getString(R.string.com_example_string_master_SETTING_GAME_LENGTH);
        KEY_SHARPS = context.getString(R.string.com_example_string_master_SETTING_SHARPS);
        KEY_FLATS = context.getString(R.string.com_example_string_master_SETTING_FLATS);

        dialogLayoutResId = R.layout.clear_scores_dialog_pref;
    }

    @Override
    public int getDialogLayoutResource() {
        return dialogLayoutResId;
    }

    /**
     * Erase all high score data. Data is erased by getting non-score values, erasing all stored
     * values, and then putting non-score values back.
     */
    public void eraseScores() {
        Log.d(TAG, "eraseScores: Erasing scores!");
        boolean sharps = sharedPreferences.getBoolean(KEY_SHARPS, true);
        boolean flats = sharedPreferences.getBoolean(KEY_FLATS, true);
        int numberOfFrets = sharedPreferences.getInt(KEY_NUMBER_FRETS, 21);
        int gameLength = (sharedPreferences.getInt(KEY_GAME_LENGTH, 0));
        sharedPreferences.edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SHARPS, sharps);
        editor.putBoolean(KEY_FLATS, flats);
        editor.putInt(KEY_NUMBER_FRETS, numberOfFrets);
        editor.putInt(KEY_GAME_LENGTH, gameLength);
        editor.apply();
    }
}
