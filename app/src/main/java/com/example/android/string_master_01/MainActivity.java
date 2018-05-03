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

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION_TRAINER = 200;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION_TUNER = 201;
    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "com.example.string_master.ANDROID";

    // Notes on a guitar using Scientific Pitch Notation (SPN)
    // MIDI notes 40 to 86
    private static final String[] NOTES = {
            "E2", "F2", "F#2/Gb2", "G2", "G#2/Ab2", "A2", "A#2/Bb2", "B2", "C3", "C#3/Db3",
            "D3", "D#3/Eb3", "E3", "F3", "F#3/Gb3", "G3", "G#3/Ab3", "A3", "A#3/Bb3", "B3",
            "C4", "C#4/Db4", "D4", "D#4/Eb4", "E4", "F4", "F#4/Gb4", "G4", "G#4/Ab4", "A4",
            "A#4/Bb4", "B4", "B#4/Cb5", "C5", "C#5/Db5", "D5", "D#5/Eb5", "E5", "F5", "F#5/Gb5",
            "G5", "G#5/Ab5", "A5", "A#5/Bb5", "B5", "C6", "C#6/Db6"};
    private static final String[] NOTE_LETTERS = {"C", "D", "E", "F", "G", "A", "B"};
    private static final int LOW_E_OFFSET = 0;
    private static final int A_OFFSET = 5;
    private static final int D_OFFSET = 10;
    private static final int G_OFFSET = 15;
    private static final int B_OFFSET = 19;
    private static final int HIGH_E_OFFSET = 24;
    private static final int BASE_MIDI_NOTE = 40;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private NotificationCompat.Builder mBuilder;
    private boolean sharps;
    private boolean flats;
    private int numberOfFrets;
    private int gameLength;

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

        if (savedInstanceState == null) {
            checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TRAINER);
        }

        //Set preference values with previously saved values (if exists)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        gameLength = (sharedPref
                .getInt(getString(R.string.com_example_string_master_SETTING_GAME_LENGTH),
                        0)+1)*30;
        numberOfFrets = sharedPref
                .getInt(getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS),
                        21)+1;
        sharps = sharedPref
                .getBoolean(getString(R.string.com_example_string_master_SETTING_SHARPS),
                        true);
        flats = sharedPref
                .getBoolean(getString(R.string.com_example_string_master_SETTING_FLATS),
                        true);

        //Setup notification channel if running Oreo+
        setupNotificationChannel();

        //Setup builder, default appearance, and action for notification
        PendingIntent pendingNotificationIntent = setupNotification(getString(R.string.app_name),
                getString(R.string.notification_text));

        //Start repeating notification to remind the user to train daily
        repeatNotification(1000, pendingNotificationIntent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.
        // ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Setup navigation drawer onNavigationItemSelectedListener to check the selected item
     * and try to load the associated fragment.
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectDrawerItem(item);
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize fragment associated with selected nav drawer item. Will swap fragment if no
     * permissions check is needed, otherwise will call permissions check which will handle swap.
     *
     * @param item  nav drawer item that was selected by user
     */
    public void selectDrawerItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_trainer:
                checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TRAINER);
                break;
            case R.id.nav_tuner:
                checkPermissions(REQUEST_RECORD_AUDIO_PERMISSION_TUNER);
                break;
            case R.id.nav_settings:
                swapFragment(SettingsFragment.class);
                break;
        }

        //Close the navigation drawer
        mDrawer.closeDrawers();
    }

    /**
     * Determines if appropriate permissions have been granted before switching fragment. If
     * they have note been granted, requests access from user, and fragment switch is handled by
     * onRequestPermissionsResult based on response. If permissions have been granted, swaps
     * fragment.
     *
     * @param requestCode constant used to identify result of permissions request
     */
    private void checkPermissions(int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    requestCode);
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION_TUNER) {
            swapFragment(TunerFragment.class);
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION_TRAINER) {
            swapFragment(TrainerFragment.class);
        }
    }

    /**
     * Determines what to do based on user response to permissions request. Called when user
     * responds to permissions request. If permission granted, swaps to fragment which requested
     * the permission access. If permission denied, provides a toast to describe to the user why
     * the feature has not loaded.
     *
     * @param requestCode constant used to identify the feature of the app which originated the
     *                    permissions request
     * @param permissions
     * @param grantResults
     */
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
                    Toast toast = Toast.makeText(this,
                            "Record Audio Permission Required!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return;
            }
            case REQUEST_RECORD_AUDIO_PERMISSION_TUNER: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    swapFragment(TunerFragment.class);
                } else {
                    Toast toast = Toast.makeText(this,
                            "Record Audio Permission Required!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return;
            }
        }
    }

    /**
     * Creates an instance of fragmentClass and replaces the current fragment with it. Also sets the
     * action bar title and navigation bar selected item.
     *
     * @param fragmentClass fragment class which will be instantiated and swapped to
     */
    public void swapFragment(Class fragmentClass) {
        android.support.v4.app.Fragment fragment = null;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        try {
            fragment = (android.support.v4.app.Fragment)fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        MenuItem navItem = null;
        switch (fragment.getClass().getSimpleName()) {
            case "TrainerFragment":
                navItem = nvDrawer.getMenu().getItem(1);
                break;
            case "TunerFragment":
                navItem = nvDrawer.getMenu().getItem(0);
                break;
            case "SettingsFragment":
                navItem = nvDrawer.getMenu().getItem(2);
                break;
        }
        if (navItem != null) {
            //Highlight the selected item has been done by NavigationView
            navItem.setChecked(true);

            //Set action bar title
            setTitle(navItem.getTitle());
        }
    }

    /**
     * Convert a note to a MIDI number by using the known MIDI number of the string the note was
     * played on.
     *
     * @param note the letter note which was played followed by the octave, ex. "A2"
     * @param stringNotes map of notes available on the string to their MIDI values
     * @return MIDI note value of played note
     */
    public int getMIDINote(String note, Map<String, Integer> stringNotes) {
        Log.d(TAG, "getMIDINote: " + (BASE_MIDI_NOTE + stringNotes.get(note)));
        return BASE_MIDI_NOTE + stringNotes.get(note);
    }


    /**
     * Generate a map of the available notes to their MIDI values for a trainer instance based on
     * settings and string selected.
     *
     * @param string the string which is being tested, ex. "E"
     * @param octave the octave number of the string being tested, ex. 2
     * @param stringOffset the base MIDI offset value for the selected string
     * @return LinkedHashMap of available notes and their MIDI values for trainer
     */
    private Map<String, Integer> generateNotes(String string, int octave, int stringOffset) {
        Map<String, Integer> output = new LinkedHashMap<>();
        int offset = -1;
        for (int i = 0; i < NOTE_LETTERS.length; i++) {
            if (NOTE_LETTERS[i].equals(string)) {
                offset = i;
                break;
            }
        }
        if (offset == -1) {
            return null;
        }
        for (int i = 0; i < numberOfFrets; i++) {
            output.put(NOTE_LETTERS[offset] + octave, i + stringOffset);
            if (!NOTE_LETTERS[offset].equals("E") && !NOTE_LETTERS[offset].equals("B")) {
                //Number of frets counts a flat/sharp pair as one note
                i++;
                if (i < numberOfFrets) {
                    if (sharps) {
                        output.put(NOTE_LETTERS[offset] + "#" + octave, i + stringOffset);
                    }
                    if (flats) {
                        output.put(NOTE_LETTERS[offset+1] + "b" + octave, i + stringOffset);
                    }
                }
            }
            if (offset == NOTE_LETTERS.length-1) {
                offset = 0;
                octave++;
            } else {
                offset++;
            }
        }
        for (String name: output.keySet()) {
            String value = output.get(name).toString();
            Log.d(TAG, "generateNotes: Note: " + name + " offset: " + value);
        }
        return output;
    }

    /**
     * Setup repeating notification with given interval.
     *
     * @param interval Time, in milliseconds, to repeat notification display
     * @param pendingNotificationIntent PendingIntent which contains the built notification to be
     *                                  displayed
     */
    private void repeatNotification(long interval, PendingIntent pendingNotificationIntent) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        try {
            alarmManager.cancel(pendingNotificationIntent);
        } catch (NullPointerException e) {
            Log.d(TAG, "repeatNotification: No pending intents to cancel");
        }
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                //SystemClock.elapsedRealtime(),
                interval,
                pendingNotificationIntent);
    }

    /**
     * Setup notification channel if running Oreo+. Only on API 26+ because the NotificationChannel
     * class is new and not in the support library.
     */
    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    /**
     * Setup a PendingIntent with a built notification attached that uses the text parameters
     * provided.
     *
     * @param title Title of the created notification
     * @param text  Body text of the created notification
     * @return
     */
    private PendingIntent setupNotification(String title, String text) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setAction("android.intent.action.MAIN");
        mainActivityIntent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingMainActivityIntent = PendingIntent.getActivity(
                this,
                0,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_guitar_acoustic)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingMainActivityIntent)
                .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.notificationInstance, mBuilder.build());
        notificationIntent.putExtra(NotificationPublisher.notificationId, 1);
        return PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public Map<String, Integer> getLowENotes() {
        return generateNotes("E", 2, LOW_E_OFFSET);
    }

    public Map<String, Integer> getANotes() {
        return generateNotes("A", 2, A_OFFSET);
    }

    public Map<String, Integer> getDNotes() {
        return generateNotes("D", 3, D_OFFSET);
    }

    public Map<String, Integer> getGNotes() {
        return generateNotes("G", 3, G_OFFSET);
    }

    public Map<String, Integer> getBNotes() {
        return generateNotes("B", 3, B_OFFSET);
    }

    public Map<String, Integer> getHighENotes() {
        return generateNotes("E", 4, HIGH_E_OFFSET);
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
        return LOW_E_OFFSET;
    }

    public int getAOffset() {
        return A_OFFSET;
    }

    public int getDOffset() {
        return D_OFFSET;
    }

    public int getGOffset() {
        return G_OFFSET;
    }

    public int getBOffset() {
        return B_OFFSET;
    }

    public int getHighEOffset() {
        return HIGH_E_OFFSET;
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
