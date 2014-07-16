package com.example.positionalaudio;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;


public class MyActivity extends Activity {
    int angle = 0;
    int height = 0;
    int distance = 1;
    int currentFile;
    public Handler soundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){

            System.err.println("msg.what="+msg.what);
            System.err.println("msg.arg="+msg.arg1);

            if (msg.what == 0){
                playSound(msg.arg1);
            }else if  (msg.what == 1){
                if (msg.arg1 == currentFile){
                    playSound(msg.arg1);
                }
            }


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        SeekBar angleSlider = (SeekBar) findViewById(R.id.angle);

        angleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle = progress;
                currentFile = getSoundFile();
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = currentFile;
                soundHandler.sendMessage(msg);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar distanceSlider = (SeekBar) findViewById(R.id.distance);
        distanceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

    public int getSoundFile(){
        int angleFile;

        if (angle <= -80) {
            angleFile = R.raw.angle_85;
        } else if (angle <= -70) {
            angleFile = R.raw.angle_75;
        } else if (angle <= -60) {
            angleFile = R.raw.angle_65;
        } else if (angle <= -50) {
            angleFile = R.raw.angle_55;
        } else if (angle <= -40) {
            angleFile = R.raw.angle_45;
        } else if (angle <= -30) {
            angleFile = R.raw.angle_35;
        } else if (angle <= -20) {
            angleFile = R.raw.angle_25;
        } else if (angle <= -10) {
            angleFile = R.raw.angle_15;
        } else if (angle <= 0) {
            angleFile = R.raw.angle_5;
        } else if (angle <= 10) {
            angleFile = R.raw.angle5;
        } else if (angle <= 20) {
            angleFile = R.raw.angle15;
        } else if (angle <= 30) {
            angleFile = R.raw.angle25;
        } else if (angle <= 40) {
            angleFile = R.raw.angle35;
        } else if (angle <= 50) {
            angleFile = R.raw.angle45;
        } else if (angle <= 60) {
            angleFile = R.raw.angle55;
        } else if (angle <= 70) {
            angleFile = R.raw.angle65;
        } else if (angle <= 80) {
            angleFile = R.raw.angle75;
        } else {
            angleFile = R.raw.angle85;
        }

        return angleFile;
    }

    public void playSound(int fileResource){
        MediaPlayer mediaPlayer = MediaPlayer.create(this.getApplicationContext(), fileResource);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });

        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = fileResource;
        soundHandler.sendMessageDelayed(msg, distance*1000L);

    }






}
