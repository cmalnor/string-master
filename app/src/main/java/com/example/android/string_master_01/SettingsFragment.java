package com.example.android.string_master_01;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SeekBarPreference;

/**
 * Created by codymalnor on 2/20/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    private int numberOfFrets;
    private SharedPreferences sharedPreferences;
    private int gameLength;
    private String KEY_GAME_LENGTH;
    private String KEY_NUMBER_FRETS;
    private String KEY_SHARPS;
    private String KEY_FLATS;
    private Context context;
    private android.support.v7.preference.SeekBarPreference fretsSeekBar;
    private android.support.v7.preference.SeekBarPreference timeSeekBar;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        KEY_NUMBER_FRETS = getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS);
        KEY_GAME_LENGTH = getString(R.string.com_example_string_master_SETTING_GAME_LENGTH);
        KEY_SHARPS = getString(R.string.com_example_string_master_SETTING_SHARPS);
        KEY_FLATS = getString(R.string.com_example_string_master_SETTING_FLATS);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        fretsSeekBar = (SeekBarPreference) findPreference(KEY_NUMBER_FRETS);
        numberOfFrets = sharedPreferences.getInt(KEY_NUMBER_FRETS, 21)+1;
        fretsSeekBar.setTitle(getString(R.string.text_number_frets, numberOfFrets));

        timeSeekBar = (SeekBarPreference) findPreference(KEY_GAME_LENGTH);
        gameLength = (sharedPreferences.getInt(KEY_GAME_LENGTH, 0)+1)*30;
        timeSeekBar.setTitle(getString(R.string.text_game_length, gameLength));

        context = getActivity();
    }

    /**
     * When a shared preference is changed, update the corresponding views and activity variables.
     *
     * @param sharedPreferences
     * @param key stored reference name for changed preference data
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_GAME_LENGTH)) {

            //length of game in increments of 30s: 0-240s
            gameLength = (sharedPreferences.getInt(KEY_GAME_LENGTH, 0)+1)*30;
            ((MainActivity) context).setGameLength(gameLength);
            timeSeekBar.setTitle(getString(R.string.text_game_length, gameLength));
        } else if (key.equals(KEY_NUMBER_FRETS)) {

            //number of frets tested: 1-23
            numberOfFrets = sharedPreferences.getInt(KEY_NUMBER_FRETS, 21)+1;
            ((MainActivity) context).setNumberOfFrets(numberOfFrets);
            fretsSeekBar.setTitle(getString(R.string.text_number_frets, numberOfFrets));
        } else if (key.equals(KEY_SHARPS)) {

            //include sharps in note list
            boolean sharps = sharedPreferences.getBoolean(KEY_SHARPS, true);
            ((MainActivity) context).setSharps(sharps);
        } else if (key.equals(KEY_FLATS)) {

            //include flats in note list
            boolean flats = sharedPreferences.getBoolean(KEY_FLATS, true);
            ((MainActivity) context).setFlats(flats);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * If a ClearScoresDialogPreference tries to display a dialog, specify
     * ClearScoresDialogFragmentCompat. Otherwise call super and display normal dialog.
     *
     * @param preference Preference trying to display a dialog
     */
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ClearScoresDialogPreference) {
            dialogFragment = ClearScoresDialogFragmentCompat.newInstance(preference.getKey());
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(),
                    "android.support.v7.preference" + ".PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
