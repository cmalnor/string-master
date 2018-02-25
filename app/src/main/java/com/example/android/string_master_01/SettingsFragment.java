package com.example.android.string_master_01;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by codymalnor on 2/20/18.
 */

public class SettingsFragment extends android.support.v4.app.Fragment{

    private SeekBar fretsSeekBar;
    private TextView fretsText;
    private int numberOfNotes;
    private int numberOfFrets;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.settings_layout, container, false);
        fretsSeekBar = (SeekBar) rootView.findViewById(R.id.frets_seekbar);
        fretsText = (TextView) rootView.findViewById(R.id.setting_frets_text);
        numberOfNotes = ((MainActivity)getActivity()).getNOTES().length;
        fretsSeekBar.setMax(21);

        //numberOfFrets is for SeekBar, so 0 is included
        fretsText.setText(getContext().getString(R.string.setting_number_frets,
                ((MainActivity)getActivity()).getNumberOfFrets()));
        fretsSeekBar.setProgress(((MainActivity)getActivity()).getNumberOfFrets()-1);
        fretsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ((MainActivity)getActivity()).setNumberOfFrets(i+1);
                fretsText.setText(getContext().getString(R.string.setting_number_frets, i+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return rootView;
    }
}
