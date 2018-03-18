package com.example.android.string_master_01;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by codymalnor on 2/20/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "StringMaster/Settings";
    private android.support.v7.preference.Preference fretsSeekBar;
    private android.support.v7.preference.Preference timeSeekBar;
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        fretsSeekBar = findPreference(KEY_NUMBER_FRETS);
        timeSeekBar = findPreference(KEY_GAME_LENGTH);

        numberOfNotes = ((MainActivity)getActivity()).getNOTES().length;

        KEY_GAME_LENGTH = getString(R.string.com_example_string_master_SETTING_GAME_LENGTH);
        KEY_NUMBER_FRETS = getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS);
        KEY_HIGH_SCORE = getString(R.string.com_example_string_master_SETTING_HIGH_SCORE);

        context = getActivity();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        Log.d(TAG, "onSharedPreferenceChanged: ");
        if(key.equals(KEY_GAME_LENGTH)){

            //length of game in increments of 30s: 0-300s
            gameLength = sharedPreferences.getInt(KEY_GAME_LENGTH, 2)*30;
            ((MainActivity)context).setGameLength(gameLength);
        } else if (key.equals(KEY_NUMBER_FRETS)){

            //number of frets tested: 1-22
            numberOfFrets = sharedPreferences.getInt(KEY_NUMBER_FRETS, 21)+1;
            ((MainActivity)context).setNumberOfFrets(numberOfFrets);
        } else if (key.equals(KEY_HIGH_SCORE+gameLength)){

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
}
