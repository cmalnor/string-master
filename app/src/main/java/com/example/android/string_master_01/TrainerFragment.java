package com.example.android.string_master_01;

import android.os.Bundle;
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

/**
 * Created by codymalnor on 1/14/18.
 */

public class TrainerFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private Button startButton;
    private Button stopButton;
    private TextView assignedNote;
    private Spinner chosenString;
    private String[] notes;

    private String TAG = "TrainerFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.trainer_layout, container, false);
        startButton = (Button) rootView.findViewById(R.id.start_button);
        assignedNote = (TextView) rootView.findViewById(R.id.assigned_note);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assignedNote.setText("ON");
            }
        });
        stopButton = (Button) rootView.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assignedNote.setText("OFF");
            }
        });
        chosenString = (Spinner) rootView.findViewById(R.id.string_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.strings, android.R.layout.simple_spinner_dropdown_item);
        chosenString.setAdapter(adapter);
        chosenString.setOnItemSelectedListener(this);

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(pos));
        switch(pos){
            case 0:
                notes = ((MainActivity)getActivity()).getLowENotes();
                break;
            case 1:
                notes = ((MainActivity)getActivity()).getANotes();
                break;
            case 2:
                notes = ((MainActivity)getActivity()).getDNotes();
                break;
            case 3:
                notes = ((MainActivity)getActivity()).getGNotes();
                break;
            case 4:
                notes = ((MainActivity)getActivity()).getBNotes();
                break;
            case 5:
                notes = ((MainActivity)getActivity()).getHighENotes();
                break;
            default:
                notes = ((MainActivity)getActivity()).getLowENotes();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }
}
