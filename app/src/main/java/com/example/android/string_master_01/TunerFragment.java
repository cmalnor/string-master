package com.example.android.string_master_01;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by codymalnor on 12/25/17.
 */

public class TunerFragment extends android.support.v4.app.Fragment{

    private static final String TAG = "GuitarTuner";

    private PdUiDispatcher dispatcher;
    private com.example.android.string_master_01.PitchView pitchView;
    private TextView note;
    private PdService pdService = null;

    private String[] notes = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.tuner_layout, container, false);
        initGui(rootView);
        notes = ((MainActivity)getActivity()).getNOTES();
        //Inflate the layout for this fragment
        return rootView;
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
            //Will never be called...
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

    private void initGui(View view){
        pitchView = (PitchView) view.findViewById(R.id.pitch_view);
        pitchView.setCenterPitch(45);

        note = (TextView) view.findViewById(R.id.detected_note);
        note.setText("A");
    }

    private void initPd() throws IOException{
        //Configure the audio glue
        int sampleRate = AudioParameters.suggestSampleRate();
        Log.i(TAG, "Sample Rate: " + sampleRate);
        pdService.initAudio(sampleRate, 1, 2, 50.0f);
        pdService.startAudio();

        //Create and install the dispatcher
        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                if (x > 30){
                    findClosestString(x);
                    //Log.i(TAG, "pitch: " + x);
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

    public void findClosestString(float x){
        float midiNote = Math.round(x);
        int noteOffset = (int)midiNote - 40;
        if (pitchView.getCenterPitch() != midiNote) {
            if (midiNote < 40) {
                pitchView.setCenterPitch(40);
                note.setText(notes[0]);
            } else if (midiNote > 86) {
                pitchView.setCenterPitch(86);
                note.setText(notes[46]);
            } else {
                pitchView.setCenterPitch(midiNote);
                note.setText(notes[noteOffset]);
            }
        }
    }
}
