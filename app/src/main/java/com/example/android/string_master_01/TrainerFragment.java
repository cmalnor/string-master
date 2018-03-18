package com.example.android.string_master_01;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
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
    private TextView highScoreView;
    private PdUiDispatcher dispatcher;
    private PdService pdService = null;
    private android.os.Handler noteHandler = new Handler();
    private Random rand = new Random();
    private Context context;
    private MediaPlayer correctSound;
    private MediaPlayer timeoutSound;
    private SharedPreferences sharedPreferences;
    private String KEY_HIGH_SCORE;
    private int noteCounter = 0;
    private int counter = 0;
    private int score = 0;
    private TextView scoreView;
    private Runnable noteRunnable = new Runnable() {
        @Override
        public void run() {
            counter--;
            if (counter == 0){
                timeoutSound.start();
                countdownView.setText(Integer.toString(counter));
                stopGame();
            } else{
                if (noteCounter >= 2){
                    correctSound.start();
                    scorePoint();
                    scoreView.setText(Integer.toString(score));
                    setHighScore();
                    noteCounter = 0;
                    getRandomNote();
                }
                countdownView.setText(Integer.toString(counter));
                noteHandler.postDelayed(this, 1000);
            }
        }
    };

    private String TAG = "TrainerFragment";

    private void getRandomNote(){
        int nextNote = rand.nextInt(((MainActivity)context).getNumberOfFrets())+noteOffset;
        assignedNote.setText(notes[nextNote]);
        pitchView.setCenterPitch(nextNote+40);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.trainer_layout, container, false);
        context = getActivity();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        KEY_HIGH_SCORE = getString(R.string.com_example_string_master_SETTING_HIGH_SCORE);

        countdownView = (TextView) rootView.findViewById(R.id.countdown);
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);
        highScoreView = (TextView) rootView.findViewById(R.id.trainer_high_score);
        setHighScore();
        startStopButton = (Button) rootView.findViewById(R.id.start_stop_button);
        scoreView = (TextView) rootView.findViewById(R.id.trainer_score);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startStopButton.getText() == "Stop"){
                    stopGame();
                } else if (startStopButton.getText() == "Start"){
                    startGame();
                } else{
                    resetGame();
                }
            }
        });
        chosenString = (Spinner) rootView.findViewById(R.id.string_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.strings, android.R.layout.simple_spinner_dropdown_item);
        chosenString.setAdapter(adapter);
        chosenString.setOnItemSelectedListener(this);
        notes = ((MainActivity)context).getNOTES();
        pitchView = (TrainerPitchView)rootView.findViewById(R.id.trainer_pitch_view);
        pitchView.setCenterPitch(45);
        correctSound = MediaPlayer.create(context, R.raw.correct);
        timeoutSound = MediaPlayer.create(context, R.raw.out_of_time);

        resetGame();
        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(pos));
        switch(pos){
            case 0:
                noteOffset = ((MainActivity)context).getLowEOffset();
                break;
            case 1:
                noteOffset = ((MainActivity)context).getAOffset();
                break;
            case 2:
                noteOffset = ((MainActivity)context).getDOffset();
                break;
            case 3:
                noteOffset = ((MainActivity)context).getGOffset();
                break;
            case 4:
                noteOffset = ((MainActivity)context).getBOffset();
                break;
            case 5:
                noteOffset = ((MainActivity)context).getHighEOffset();
                break;
            default:
                noteOffset = ((MainActivity)context).getLowEOffset();
                break;
        }
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
                ((MainActivity)context).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        context.bindService(new Intent(context, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
        super.onStop();
        noteHandler.removeCallbacks(noteRunnable);
        context.unbindService(pdConnection);
    }

    private void startGame(){
        noteHandler.post(noteRunnable);
        startStopButton.setText("Stop");
        getRandomNote();
        counter += 1;
        pdService.startAudio();
    }

    private void stopGame(){
        noteHandler.removeCallbacks(noteRunnable);
        startStopButton.setText("Reset");
        pdService.stopAudio();
    }

    private void initPd() throws IOException{
        //Configure the audio glue
        int sampleRate = AudioParameters.suggestSampleRate();
        Log.i(TAG, "Sample Rate: " + sampleRate);
        pdService.initAudio(sampleRate, 1, 2, 50.0f);

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
        File dir = context.getFilesDir();
        IoUtils.extractZipResource(
                getResources().openRawResource(R.raw.tuner), dir, true);
        File patchFile = new File(dir, "tuner.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    private void scorePoint(){
        score += 1;
    }

    private void resetGame(){
        score = 0;
        counter = ((MainActivity)context).getGameLength();
        countdownView.setText(Integer.toString(counter));
        assignedNote.setText("Tap 'Start' to Begin");
        scoreView.setText(Integer.toString(score));
        startStopButton.setText("Start");
    }

    private void setHighScore(){
        //If score when game is stopped is a new high score for this game length, save it
        String key = KEY_HIGH_SCORE+((MainActivity)context).getGameLength();
        int highScore = sharedPreferences.getInt(key, 0);
        if(!sharedPreferences.contains(key) ||
                highScore < score){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, score);
            editor.apply();
            highScoreView.setText(getString(R.string.text_high_score, score));
        } else{
            highScoreView.setText(getString(R.string.text_high_score, highScore));
        }
    }
}
