package com.example.android.string_master_01;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Notes on a guitar using Scientific Pitch Notation (SPN)
    // MIDI notes 40 to 86
    final String[] NOTES = {
            "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2", "C3", "C#3",
            "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4",
            "A#4", "B4", "B#4", "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5",
            "G5", "G#5", "A5", "A#5", "B5", "C6", "C#6"};
    final int lowEOffset = 0;
    final int AOffset = 5;
    final int DOffset = 10;
    final int GOffset = 15;
    final int BOffset = 19;
    final int highEOffset = 24;
    private int numberOfFrets;
    private int gameLength;
    private boolean allowSwap;

    private final String TAG = "MainActivity";
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

        allowSwap = false;

        checkPermissions();

        if(allowSwap){
            if(savedInstanceState == null){
                swapFragment(TrainerFragment.class);
            }
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setGameLength(sharedPref.getInt(getString(R.string.com_example_string_master_SETTING_GAME_LENGTH), 2)*30);
        setNumberOfFrets(sharedPref.getInt(getString(R.string.com_example_string_master_SETTING_NUMBER_FRETS), 21)+1);


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
                fragmentClass = TunerFragment.class;
        }
        if(fragmentClass == TrainerFragment.class || fragmentClass == TunerFragment.class){
            checkPermissions();
            if(allowSwap){
                swapFragment(fragmentClass);
            }
        } else {
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
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    allowSwap = true;
                } else {
                    Toast toast = Toast.makeText(this, "Record Audio Permission Required!", Toast.LENGTH_SHORT);
                    toast.show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
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

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
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
}
