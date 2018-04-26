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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by codymalnor on 1/14/18.
 */

public class TrainerFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "TrainerFragment";

    private Button startStopButton;
    private Spinner chosenString;
    private Map<String, Integer> notes;
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
    private MediaPlayer tickSound;
    private SharedPreferences sharedPreferences;
    private String KEY_HIGH_SCORE;
    private int selectedString;
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
                countdownView.setText(timeConvert(counter));
                stopGame();
            } else{
                tickSound.start();
                countdownView.setText(timeConvert(counter));
                noteHandler.postDelayed(this, 1000);
            }
        }
    };
    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            pdService = ((PdService.PdBinder) service).getService();
            try {
                initPd();
                loadPatch();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                ((MainActivity) context).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.trainer_layout, container, false);
        context = getActivity();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        KEY_HIGH_SCORE = getString(R.string.com_example_string_master_SETTING_HIGH_SCORE);

        countdownView = (TextView) rootView.findViewById(R.id.countdown);
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);

        highScoreView = (TextView) rootView.findViewById(R.id.trainer_high_score);
        scoreView = (TextView) rootView.findViewById(R.id.trainer_score);
        setHighScore();

        startStopButton = (Button) rootView.findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startStopButton.getText() == getString(R.string.trainer_button_stop)) {
                    stopGame();
                } else if (startStopButton.getText() == getString(R.string.trainer_button_start)) {
                    startGame();
                } else {
                    resetGame();
                }
            }
        });

        //Setup chosen string Spinner with string names
        chosenString = (Spinner) rootView.findViewById(R.id.string_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.strings, android.R.layout.simple_spinner_dropdown_item);
        chosenString.setAdapter(adapter);
        chosenString.setOnItemSelectedListener(this);

        notes = ((MainActivity) context).getLowENotes();

        pitchView = (TrainerPitchView) rootView.findViewById(R.id.trainer_pitch_view);
        pitchView.setCenterPitch(45);

        correctSound = MediaPlayer.create(context, R.raw.correct);
        timeoutSound = MediaPlayer.create(context, R.raw.out_of_time);
        tickSound = MediaPlayer.create(context, R.raw.clock_tick);

        resetGame();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        context.bindService(new Intent(context, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        noteHandler.removeCallbacks(noteRunnable);
        context.unbindService(pdConnection);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        if (correctSound != null) {
            correctSound.release();
        }
        if (timeoutSound != null) {
            timeoutSound.release();
        }
        if (tickSound != null)  {
            tickSound.release();
        }
    }

    /**
     *
     *
     * @throws IOException
     */
    private void initPd() throws IOException {
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
                Log.i(TAG, "pitch: " + x);
                if (noteCounter > 1) {
                    correctSound.start();
                    scorePoint();
                    scoreView.setText(Integer.toString(score));
                    setHighScore();
                    noteCounter = 0;
                    getRandomNote();
                } else if (x < pitchView.getCenterPitch()+0.5
                        && x > pitchView.getCenterPitch()-0.5) {
                    noteCounter++;
                } else if (x > 0) {
                    noteCounter = 0;
                }
                pitchView.setNewPitch(x);
                //Log.d(TAG, "receiveFloat: " + noteCounter);
            }
        });
    }

    private void loadPatch() throws IOException {
        File dir = context.getFilesDir();
        IoUtils.extractZipResource(
                getResources().openRawResource(R.raw.tuner), dir, true);
        File patchFile = new File(dir, "tuner.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    private void getRandomNote() {
        int nextNoteOffset = rand.nextInt(notes.size());
        String nextNote = notes.keySet().toArray()[nextNoteOffset].toString();
        assignedNote.setText(nextNote);
        pitchView.setCenterPitch(((MainActivity) context).getMIDINote(nextNote, notes));
    }

    private void startGame() {
        noteHandler.post(noteRunnable);
        startStopButton.setText(getString(R.string.trainer_button_stop));
        getRandomNote();
        counter += 1;
        pdService.startAudio();
        chosenString.setEnabled(false);
        ((MainActivity) context).getWindow()
                .addFlags(WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopGame() {
        noteHandler.removeCallbacks(noteRunnable);
        startStopButton.setText(getString(R.string.trainer_button_reset));
        pitchView.setNewPitch(-1);
        pdService.stopAudio();
        ((MainActivity) context).getWindow()
                .clearFlags(WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void resetGame() {
        score = 0;
        counter = ((MainActivity) context).getGameLength();
        countdownView.setText(timeConvert(counter));
        assignedNote.setText(getString(R.string.trainer_text_begin_instruction));
        scoreView.setText(Integer.toString(score));
        startStopButton.setText(R.string.trainer_button_start);
        chosenString.setEnabled(true);
    }

    /**
     * If current score is greater than high score, or if no high score exists, save score as high
     * score. If not high score do nothing.
     */
    private void setHighScore() {
        String key = KEY_HIGH_SCORE+
                ((MainActivity) context).getNumberOfFrets()+
                '_'+
                selectedString+
                '_'+
                ((MainActivity) context).getGameLength();
        Log.d(TAG, "setHighScore: "+key);
        int highScore = sharedPreferences.getInt(key, 0);
        if (!sharedPreferences.contains(key)
                || highScore < score) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, score);
            editor.apply();
            highScoreView.setText(getString(R.string.text_high_score, score));
        } else {
            highScoreView.setText(getString(R.string.text_high_score, highScore));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(pos));
        selectedString = pos;
        setHighScore();
        switch (pos) {
            case 0:
                notes = ((MainActivity) context).getLowENotes();
                break;
            case 1:
                notes = ((MainActivity) context).getANotes();
                break;
            case 2:
                notes = ((MainActivity) context).getDNotes();
                break;
            case 3:
                notes = ((MainActivity) context).getGNotes();
                break;
            case 4:
                notes = ((MainActivity) context).getBNotes();
                break;
            case 5:
                notes = ((MainActivity) context).getHighENotes();
                break;
            default:
                notes = ((MainActivity) context).getLowENotes();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String timeConvert(int seconds){
        long min = TimeUnit.SECONDS.toMinutes(seconds);
        long sec = seconds - TimeUnit.MINUTES.toSeconds(min);
        return String.format("%01d:%02d", min, sec);
    }

    private void scorePoint() {
        score += 1;
    }
}
