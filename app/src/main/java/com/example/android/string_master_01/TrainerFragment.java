package com.example.android.string_master_01;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

/**
 * Created by codymalnor on 1/14/18.
 */

public class TrainerFragment extends Fragment {

    // Notes on a guitar using Scientific Pitch Notation (SPN)
    // MIDI notes 40 to
    final String[] NOTES = {
            "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "C3", "C#3",
            "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4",
            "A#4", "B4", "B#4", "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5",
            "G5", "G#5", "A5", "A#5", "B5", "C6", "C#6"};
    final String[] lowENotes = Arrays.copyOfRange(NOTES, 0, 22);
    final String[] ANotes = Arrays.copyOfRange(NOTES, 5, 27);
    final String[] DNotes = Arrays.copyOfRange(NOTES, 10, 32);
    final String[] GNotes = Arrays.copyOfRange(NOTES, 15, 37);
    final String[] BNotes = Arrays.copyOfRange(NOTES, 19, 37);
    final String[] highENotes = Arrays.copyOfRange(NOTES, 24, 46);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.trainer_layout, container, false);
    }
}
