package com.example.android.string_master_01;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

/**
 * Created by codymalnor on 3/21/18.
 */

public class ClearScoresDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static ClearScoresDialogFragmentCompat newInstance(String key) {
        final ClearScoresDialogFragmentCompat fragment = new ClearScoresDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof ClearScoresDialogPreference) {
                ClearScoresDialogPreference clearScoresPreference =
                        ((ClearScoresDialogPreference) preference);
                clearScoresPreference.eraseScores();
            }
        }
    }
}
