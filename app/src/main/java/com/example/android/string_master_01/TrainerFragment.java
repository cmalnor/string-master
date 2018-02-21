package com.example.android.string_master_01;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by codymalnor on 1/14/18.
 */

public class TrainerFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private TrainerPitchView testPitch;
    private Button startStopButton;
    private Spinner chosenString;
    private String[] notes;
    private int noteOffset;
    private TrainerPitchView pitchView;
    private TextView assignedNote;
    private TextView countdownView;
    private PdUiDispatcher dispatcher;
    private PdService pdService = null;
    private int noteCounter = 0;
    private android.os.Handler noteHandler = new Handler();
    private Random rand = new Random();
    private int counter = 0;
    private Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            counter--;
            if (counter == -1){
                timeoutSound.start();
                resetTimer();
            } else if (noteCounter >= 2){
                correctSound.start();
                resetTimer();
            }
            countdownView.setText(Integer.toString(counter));
            noteHandler.postDelayed(this, 1000);
        }
    };
    private MediaPlayer correctSound;
    private MediaPlayer timeoutSound;


    private String TAG = "TrainerFragment";

    private void resetTimer(){
        counter = 5;
        int nextNote = rand.nextInt(22)+noteOffset;
        assignedNote.setText(notes[nextNote]);
        pitchView.setCenterPitch(nextNote+40);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.trainer_layout, container, false);
        countdownView = (TextView) rootView.findViewById(R.id.countdown);
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);
        startStopButton = (Button) rootView.findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gameInstance.toggleGame();
                if (startStopButton.getText() == "Stop"){
                    noteHandler.removeCallbacks(noteRunnable);
                    startStopButton.setText("Start");
                    pdService.stopAudio();
                } else{
                    noteHandler.post(noteRunnable);
                    startStopButton.setText("Stop");
                    int nextNote = rand.nextInt(22)+noteOffset;
                    assignedNote.setText(notes[nextNote]);
                    pitchView.setCenterPitch(nextNote+40);
                    counter = 6;
                    pdService.startAudio();
                }
            }
        });
        chosenString = (Spinner) rootView.findViewById(R.id.string_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.strings, android.R.layout.simple_spinner_dropdown_item);
        chosenString.setAdapter(adapter);
        chosenString.setOnItemSelectedListener(this);
        notes = ((MainActivity)getActivity()).getNOTES();
        pitchView = (TrainerPitchView)rootView.findViewById(R.id.trainer_pitch_view);
        pitchView.setCenterPitch(45);
        correctSound = MediaPlayer.create(getActivity(), R.raw.correct);
        timeoutSound = MediaPlayer.create(getActivity(), R.raw.out_of_time);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(pos));
        switch(pos){
            case 0:
                noteOffset = ((MainActivity)getActivity()).getLowEOffset();
                break;
            case 1:
                noteOffset = ((MainActivity)getActivity()).getAOffset();
                break;
            case 2:
                noteOffset = ((MainActivity)getActivity()).getDOffset();
                break;
            case 3:
                noteOffset = ((MainActivity)getActivity()).getGOffset();
                break;
            case 4:
                noteOffset = ((MainActivity)getActivity()).getBOffset();
                break;
            case 5:
                noteOffset = ((MainActivity)getActivity()).getHighEOffset();
                break;
            default:
                noteOffset = ((MainActivity)getActivity()).getLowEOffset();
                break;
        }
        //gameInstance.setNotes(notes);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
            try{
                initPd();
                loadPatch();
            } catch (IOException e){
                Log.e(TAG, e.toString());
                getActivity().finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
        super.onStop();
        getActivity().unbindService(pdConnection);
    }

    private void initPd() throws IOException{
        //Configure the audio glue
        int sampleRate = AudioParameters.suggestSampleRate();
        Log.i(TAG, "Sample Rate: " + sampleRate);
        pdService.initAudio(sampleRate, 1, 2, 50.0f);
        //pdService.startAudio();

        //Create and install the dispatcher
        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                if (x > 40){
                    //Log.i(TAG, "pitch: " + x);
                    if (x < pitchView.getCenterPitch()+1 && x > pitchView.getCenterPitch()-1){
                        noteCounter++;
                    } else {
                        noteCounter = 0;
                    }
                    pitchView.setNewPitch(x);
                }
            }
        });
    }

    private void loadPatch() throws IOException{
        File dir = getActivity().getFilesDir();
        IoUtils.extractZipResource(
                getResources().openRawResource(R.raw.tuner), dir, true);
        File patchFile = new File(dir, "tuner.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }
}
