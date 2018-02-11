package com.example.android.string_master_01;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by codymalnor on 2/3/18.
 */

public class GameInstance{

    private String[] notes;
    private TextView assignedNote;
    private TextView countdownView;
    private TextView startStopButton;
    private android.os.Handler noteHandler = new Handler();
    private Random rand = new Random();
    private int counter = 0;
    private Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            counter--;
            if (counter == -1){
                counter = 5;
                assignedNote.setText(notes[rand.nextInt(22)]);
            }
            countdownView.setText(Integer.toString(counter));
            noteHandler.postDelayed(this, 1000);
        }
    };

    public GameInstance(String[] notesList, View rootView){
        countdownView = (TextView) rootView.findViewById(R.id.countdown);
        startStopButton = (TextView) rootView.findViewById(R.id.start_stop_button);
        notes = notesList;
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);
    }

    public void toggleGame(){
        if (startStopButton.getText() == "Stop"){
            noteHandler.removeCallbacks(noteRunnable);
            startStopButton.setText("Start");
        } else{
            noteHandler.post(noteRunnable);
            startStopButton.setText("Stop");
        }
    }

    public void setNotes(String[] notesList){
        notes = notesList;
    }
}
