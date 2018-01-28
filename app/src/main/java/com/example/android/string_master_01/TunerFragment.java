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

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by codymalnor on 12/25/17.
 */

public class TunerFragment extends android.support.v4.app.Fragment{

    private static final String TAG = "GuitarTuner";

    private PdUiDispatcher dispatcher;
    private com.example.android.string_master_01.PitchView pitchView;
    private Spinner spinner;
    private TextView note;
    private PdService pdService = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.tuner_layout, container, false);
        initGui(rootView);

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
        //spinner = (Spinner) view.findViewById(R.id.string_spinner);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
        //        R.array.strings, android.R.layout.simple_spinner_dropdown_item);
        //spinner.setAdapter(adapter);
        //spinner.setOnItemSelectedListener(this);

    }

    private void initPd() throws IOException{
        //Configure the audio glue
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate, 1, 2, 50.0f);
        pdService.startAudio();

        //Create and install the dispatcher
        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                if (x > 0){
                    pitchView.setNewPitch(x);
                    findClosestString(x);
                    Log.i(TAG, "pitch: " + x);
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
        if (x < 42.5){
            pitchView.setCenterPitch(40);
            note.setText("E2");
        } else if (42.5 < x && x < 47.5){
            pitchView.setCenterPitch(45);
            note.setText("A2");
        } else if (47.5 < x && x < 52.5){
            pitchView.setCenterPitch(50);
            note.setText("D3");
        } else if (52.5 < x && x < 57.5){
            pitchView.setCenterPitch(55);
            note.setText("G3");
        } else if (57.5 < x && x < 61.5){
            pitchView.setCenterPitch(59);
            note.setText("B3");
        } else{
            pitchView.setCenterPitch(64);
            note.setText("E4");
        }
    }
}
