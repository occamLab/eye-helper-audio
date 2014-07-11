package com.example.positionalaudio;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;


public class MyActivity extends Activity {
    Boolean seekbarPressed = false;
    Boolean buttonStart = false;
    int seekBarProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        SeekBar angleSlider = (SeekBar) findViewById(R.id.seekBar);

        angleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Find the angle which is closest to progress between
                // TODO Auto-generated method stub


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarPressed = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                seekbarPressed = false;
                seekBarProgress = seekBar.getProgress();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startLoop(View view){
        buttonStart = !buttonStart;
        long lastPlayTime = System.nanoTime();
        int distance = 100;
        float waitTime = distance * 1000000000;
        while(!seekbarPressed && buttonStart){
            int angleVal = seekBarProgress - 90;
            int angleFile;


            if (angleVal <= -80) {
                angleFile = R.raw.angle_85;
            } else if (angleVal <= -70) {
                angleFile = R.raw.angle_75;
            } else if (angleVal <= -60) {
                angleFile = R.raw.angle_65;
            } else if (angleVal <= -50) {
                angleFile = R.raw.angle_55;
            } else if (angleVal <= -40) {
                angleFile = R.raw.angle_45;
            } else if (angleVal <= -30) {
                angleFile = R.raw.angle_35;
            } else if (angleVal <= -20) {
                angleFile = R.raw.angle_25;
            } else if (angleVal <= -10) {
                angleFile = R.raw.angle_15;
            } else if (angleVal <= 0) {
                angleFile = R.raw.angle_5;
            } else if (angleVal <= 10) {
                angleFile = R.raw.angle5;
            } else if (angleVal <= 20) {
                angleFile = R.raw.angle15;
            } else if (angleVal <= 30) {
                angleFile = R.raw.angle25;
            } else if (angleVal <= 40) {
                angleFile = R.raw.angle35;
            } else if (angleVal <= 50) {
                angleFile = R.raw.angle45;
            } else if (angleVal <= 60) {
                angleFile = R.raw.angle55;
            } else if (angleVal <= 70) {
                angleFile = R.raw.angle65;
            } else if (angleVal <= 80) {
                angleFile = R.raw.angle75;
            } else {
                angleFile = R.raw.angle85;
            }

            long elapsedTime =  System.nanoTime() - lastPlayTime;
            if (elapsedTime > waitTime){
                playSound(angleFile);
                lastPlayTime = System.nanoTime();
            }


        }
    }

    public void playSound(int fileResource){

        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), fileResource);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }



}
