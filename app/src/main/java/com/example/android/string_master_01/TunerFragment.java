package com.example.android.string_master_01;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

public class TunerFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "TunerFragment";

    private PdUiDispatcher dispatcher;
    private com.example.android.string_master_01.TunerPitchView pitchView;
    private TextView note;
    private PdService pdService = null;
    private Context context;
    private String[] notes = null;

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tuner_layout, container, false);
        initGui(rootView);
        context = getActivity();

        notes = ((MainActivity) context).getNOTES();

        //Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        context.bindService(new Intent(getActivity(), PdService.class),
                pdConnection,
                BIND_AUTO_CREATE);
        ((MainActivity) context).getWindow()
                .addFlags(WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStop() {
        super.onStop();
        context.unbindService(pdConnection);
        ((MainActivity) context).getWindow()
                .clearFlags(WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initGui(View view) {
        pitchView = (TunerPitchView) view.findViewById(R.id.pitch_view);
        pitchView.setCenterPitch(45);

        note = (TextView) view.findViewById(R.id.detected_note);
        note.setText("A");
    }

    /**
     * Initialize audio receiving through PD and add listener to handle received data. Listener
     * receives MIDI note as float through PD patch and updates tuner with new value.
     *
     * @throws IOException
     */
    private void initPd() throws IOException {
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
                if (x > 30) {
                    findClosestString(x);
                    Log.i(TAG, "pitch: " + x);
                    pitchView.setNewTunerPitch(x);
                }
            }
        });
    }

    /**
     * Load specified PD patch by extracting and opening raw resource.
     *
     * @throws IOException
     */
    private void loadPatch() throws IOException {
        File dir = getActivity().getFilesDir();
        IoUtils.extractZipResource(
                getResources().openRawResource(R.raw.tuner), dir, true);
        File patchFile = new File(dir, "tuner.pd");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    /**
     * Locate the closest legitimate guitar note to the current pitch and then draw the new center
     * note and user note.
     *
     * @param userPitch MIDI value of pitch read from PD patch
     */
    public void findClosestString(float userPitch) {
        float midiNote = Math.round(userPitch);
        int noteOffset = (int) midiNote - 40;
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
