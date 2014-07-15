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
    private SoundLooping soundLooper;
    public Handler soundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){

            System.err.println("Confirmation="+msg.arg1);
            playSound(msg.arg1);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        soundLooper = new SoundLooping(soundHandler);
        soundLooper.start();

        SeekBar angleSlider = (SeekBar) findViewById(R.id.angle);

        angleSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = seekBar.getProgress();
               soundLooper.getHandler().sendMessage(msg);

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
                Message msg = Message.obtain();
                msg.what = 1;
                msg.arg1 = seekBar.getProgress();
                soundLooper.getHandler().sendMessage(msg);
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

    public void playSound(int fileResource){
        MediaPlayer mediaPlayer = MediaPlayer.create(this.getApplicationContext(), fileResource);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }






}
