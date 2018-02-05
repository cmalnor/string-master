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
    private android.os.Handler noteHandler = new Handler();
    private Random rand = new Random();
    private Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            assignedNote.setText(notes[rand.nextInt(notes.length)]);
            noteHandler.postDelayed(this, 1000);
        }
    };

    public GameInstance(String[] notesList, View rootView){
        notes = notesList;
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);
    }

    public void start(){
        noteHandler.post(noteRunnable);
    }

    public void stop(){
        noteHandler.removeCallbacks(noteRunnable);
    }

    public void setNotes(String[] notesList){
        notes = notesList;
    }
}
