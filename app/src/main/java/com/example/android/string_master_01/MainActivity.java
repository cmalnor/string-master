package com.example.android.string_master_01;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION_TRAINER = 200;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_TUNER = 201;

    // Notes on a guitar using Scientific Pitch Notation (SPN)
    // MIDI notes 40 to 86
    private final String[] NOTES = {
            "E2", "F2", "F#2/Gb2", "G2", "G#2/Ab2", "A2", "A#2/Bb2", "B2", "C3", "C#3/Db3",
            "D3", "D#3/Eb3", "E3", "F3", "F#3/Gb3", "G3", "G#3/Ab3", "A3", "A#3/Bb3", "B3",
            "C4", "C#4/Db4", "D4", "D#4/Eb4", "E4", "F4", "F#4/Gb4", "G4", "G#4/Ab4", "A4",
            "A#4/Bb4", "B4", "B#4/Cb5", "C5", "C#5/Db5", "D5", "D#5/Eb5", "E5", "F5", "F#5/Gb5",
            "G5", "G#5/Ab5", "A5", "A#5/Bb5", "B5", "C6", "C#6/Db6"};
    private final String[] noteLetters = {"C", "D", "E", "F", "G", "A", "B"};
    private boolean sharps;
    private boolean flats;

    private final int lowEOffset = 0;
    private final int AOffset = 5;
    private final int DOffset = 10;
    private final int GOffset = 15;
    private final int BOffset = 19;
    private final int highEOffset = 24;
    private int numberOfFrets;
    private int gameLength;
    private final int baseMIDINote = 40;

    private final String TAG = "MainActivity";
    private final String CHANNEL_ID = "com.example.string_master.ANDROID";

    private NotificationCompat.Builder mBuilder;

    /**
    Convert a note to a MIDI number by using the known MIDI number of the string the note was
    played on
     **/
    public int getMIDINote(String note, ArrayList<String> stringNotes){
        int offset = 0;
        switch(stringNotes.get(0)){
            case "E2":
                offset = lowEOffset;
                break;
            case "A2":
                offset = AOffset;
                break;
            case "D3":
                offset = DOffset;
                break;
            case "G3":
                offset = GOffset;
                break;
            case "B3":
                offset = BOffset;
                break;
            case "E4":
                offset = highEOffset;
                break;
        }
        Log.d(TAG, "getMIDINote: " + (baseMIDINote + offset + stringNotes.indexOf(note)));
        return baseMIDINote + offset + stringNotes.indexOf(note);
    }

    private ArrayList<String> generateNotes(String string, int octave){
        ArrayList<String> output = new ArrayList<>();
        int offset = -1;
        for(int i = 0; i < noteLetters.length; i++){
            if(noteLetters[i] == string){
                offset = i;
                break;
            }
        }
        if (offset == -1){
            return null;
        }
        for(int i = 0; i < numberOfFrets; i++){
            output.add(noteLetters[offset] + octave);
            if(noteLetters[offset] != "E" && noteLetters[offset] != "B"){
                //Number of frets counts a flat/sharp pair as one note
                i++;
                if(i < numberOfFrets){
                    if(sharps){
                        output.add(noteLetters[offset] + "#" + octave);
                    }
                    if(flats){
                        output.add(noteLetters[offset+1] + "b" + octave);
                    }
                }
            }
            if(offset == noteLetters.length-1){
                offset = 0;
                octave++;
            } else {
                offset++;
            }
        }
        Log.d(TAG, "generateNotes: Notelist: " + Arrays.toString(output.toArray()));
        return output;
    }

    public ArrayList<String> getLowENotes(){
        return generateNotes("E", 2);
    }

    public ArrayList<String> getANotes(){
        return generateNotes("A", 2);
    }

    public ArrayList<String> getDNotes(){
        return generateNotes("D", 3);
    }

    public ArrayList<String> getGNotes(){
        return generateNotes("G", 3);
    }

    public ArrayList<String> getBNotes(){
        return generateNotes("B", 3);
    }

    public ArrayList<String> getHighENotes(){
        return generateNotes("E", 4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        setupDrawerContent(nvDrawer);

        if(savedInstanceState == null){
            checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TRAINER);
        }

        //Set preference values with previously saved values (if exists)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        gameLength = (sharedPref.getInt(getString(R.string.com_example_string_master_SETTING_GAME_LENGTH), 0)+1)*30;
        numberOfFrets = sharedPref.getInt(getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS), 21)+1;
        sharps = sharedPref.getBoolean(getString(R.string.com_example_string_master_SETTING_SHARPS), true);
        flats = sharedPref.getBoolean(getString(R.string.com_example_string_master_SETTING_FLATS), true);

        //Setup notification channel if running Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        //Setup builder, default appearance, and action for notification
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(
                this,
                0,
                mainActivityIntent,
                0);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_guitar_acoustic)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingMainActivityIntent)
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, mBuilder.build());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        PendingIntent pendingNotificationIntent = PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Setup daily reminder notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingNotificationIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                //SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                //6000,
                pendingNotificationIntent);


    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );
    }

    public void selectDrawerItem(MenuItem item){
        Class fragmentClass;
        switch(item.getItemId()){
            case R.id.nav_trainer:
                fragmentClass = TrainerFragment.class;
                break;
            case R.id.nav_tuner:
                fragmentClass = TunerFragment.class;
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                fragmentClass = TrainerFragment.class;
        }
        if(fragmentClass == TrainerFragment.class){
            checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TRAINER);
        } else if (fragmentClass == TunerFragment.class){
            checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TUNER);
        } else{
            swapFragment(fragmentClass);
        }

        //Highlight the selected item has been done by NavigationView
        item.setChecked(true);

        //Set action bar title
        setTitle(item.getTitle());

        //Close the navigation drawer
        mDrawer.closeDrawers();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION_TRAINER: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    swapFragment(TrainerFragment.class);
                } else {
                    Toast toast = Toast.makeText(this, "Record Audio Permission Required!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return;
            }
            case REQUEST_RECORD_AUDIO_PERMISSION_TUNER: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    swapFragment(TunerFragment.class);
                } else {
                    Toast toast = Toast.makeText(this, "Record Audio Permission Required!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissions(int requestCode){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    requestCode);
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION_TUNER){
            swapFragment(TunerFragment.class);
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION_TRAINER) {
            swapFragment(TrainerFragment.class);
        }
    }

    public void swapFragment(Class fragmentClass){
        android.support.v4.app.Fragment fragment = null;
        try{
            fragment = (android.support.v4.app.Fragment) fragmentClass.newInstance();
        } catch (Exception e){
            e.printStackTrace();
        }

        //Insert the fragment by replacing any existing fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

    }

    public int getGameLength() {
        return gameLength;
    }

    public void setGameLength(int gameLength) {
        this.gameLength = gameLength;
    }

    public int getNumberOfFrets() {
        return numberOfFrets;
    }

    public void setNumberOfFrets(int numberOfFrets) {
        this.numberOfFrets = numberOfFrets;
    }

    public int getLowEOffset() {
        return lowEOffset;
    }

    public int getAOffset() {
        return AOffset;
    }

    public int getDOffset() {
        return DOffset;
    }

    public int getGOffset() {
        return GOffset;
    }

    public int getBOffset() {
        return BOffset;
    }

    public int getHighEOffset() {
        return highEOffset;
    }

    public String[] getNOTES() {
        return NOTES;
    }

    public boolean isSharps() {
        return sharps;
    }

    public void setSharps(boolean sharps) {
        this.sharps = sharps;
    }

    public boolean isFlats() {
        return flats;
    }

    public void setFlats(boolean flats) {
        this.flats = flats;
    }
}
