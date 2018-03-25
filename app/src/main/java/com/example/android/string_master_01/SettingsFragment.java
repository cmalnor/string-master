package com.example.android.string_master_01;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;
import android.util.Log;

/**
 * Created by codymalnor on 2/20/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "StringMaster/Settings";
    private android.support.v7.preference.SeekBarPreference fretsSeekBar;
    private android.support.v7.preference.SeekBarPreference timeSeekBar;
    private android.support.v7.preference.Preference clearScoresDialog;
    private int numberOfNotes;
    private int numberOfFrets;
    private SharedPreferences sharedPreferences;
    private int gameLength;
    private String KEY_GAME_LENGTH;
    private String KEY_NUMBER_FRETS;
    private String KEY_HIGH_SCORE;
    private Context context;

    @Override
    public void onCreatePreferences(Bundle bundle, String s){
        addPreferencesFromResource(R.xml.preferences);
        KEY_NUMBER_FRETS = getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS);
        KEY_GAME_LENGTH = getString(R.string.com_example_string_master_SETTING_GAME_LENGTH);
        KEY_HIGH_SCORE = getString(R.string.com_example_string_master_SETTING_CLEAR_SCORES);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        fretsSeekBar = (SeekBarPreference)findPreference(KEY_NUMBER_FRETS);
        timeSeekBar = (SeekBarPreference)findPreference(KEY_GAME_LENGTH);

        numberOfNotes = ((MainActivity)getActivity()).getNOTES().length;


        context = getActivity();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        Log.d(TAG, "onSharedPreferenceChanged: ");
        if(key.equals(KEY_GAME_LENGTH)){

            //length of game in increments of 30s: 0-300s
            gameLength = sharedPreferences.getInt(KEY_GAME_LENGTH, 30);
            ((MainActivity)context).setGameLength(gameLength);
        } else if (key.equals(KEY_NUMBER_FRETS)){

            //number of frets tested: 1-22
            numberOfFrets = sharedPreferences.getInt(KEY_NUMBER_FRETS, 21)+1;
            ((MainActivity)context).setNumberOfFrets(numberOfFrets);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference){
        DialogFragment dialogFragment = null;
        if(preference instanceof ClearScoresDialogPreference){
            dialogFragment = ClearScoresDialogFragmentCompat.newInstance(preference.getKey());
        }
        if(dialogFragment != null){
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" + ".PreferenceFragment.DIALOG");
        } else{
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
