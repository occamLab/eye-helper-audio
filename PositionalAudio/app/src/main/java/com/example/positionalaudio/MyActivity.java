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
    int angle = -90;
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

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                angle = seekBar.getProgress() - 90;
                currentFile = getSoundFile();
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = currentFile;
                soundHandler.sendMessage(msg);

            }
        });

        SeekBar distanceSlider = (SeekBar) findViewById(R.id.distance);
        distanceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                distance = progress + 1;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar heightSlider = (SeekBar) findViewById(R.id.height);
        heightSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                height = seekBar.getProgress()/10;
                currentFile = getSoundFile();
                Message msg = Message.obtain();
                msg.what = 0;
                msg.arg1 = currentFile;
                soundHandler.sendMessage(msg);
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
        if (height < 1){
            if (angle <= -80) {
                angleFile = R.raw.height0angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height0angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height0angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height0angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height0angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height0angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height0angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height0angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height0angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height0angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height0angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height0angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height0angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height0angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height0angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height0angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height0angle75;
            } else {
                angleFile = R.raw.height0angle85;
            }
        } else if (height < 2){
            if (angle <= -80) {
                angleFile = R.raw.height1angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height1angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height1angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height1angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height1angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height1angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height1angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height1angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height1angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height1angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height1angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height1angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height1angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height1angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height1angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height1angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height1angle75;
            } else {
                angleFile = R.raw.height1angle85;
            }
        } else if (height < 3){
            if (angle <= -80) {
                angleFile = R.raw.height2angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height2angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height2angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height2angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height2angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height2angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height2angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height2angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height2angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height2angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height2angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height2angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height2angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height2angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height2angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height2angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height2angle75;
            } else {
                angleFile = R.raw.height2angle85;
            }
        } else if (height < 4){
            if (angle <= -80) {
                angleFile = R.raw.height3angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height3angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height3angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height3angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height3angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height3angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height3angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height3angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height3angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height3angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height3angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height3angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height3angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height3angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height3angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height3angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height3angle75;
            } else {
                angleFile = R.raw.height3angle85;
            }
        } else if (height < 5){
            if (angle <= -80) {
                angleFile = R.raw.height4angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height4angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height4angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height4angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height4angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height4angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height4angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height4angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height4angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height4angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height4angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height4angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height4angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height4angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height4angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height4angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height4angle75;
            } else {
                angleFile = R.raw.height4angle85;
            }
        } else if (height < 6){
            if (angle <= -80) {
                angleFile = R.raw.height5angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height5angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height5angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height5angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height5angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height5angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height5angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height5angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height5angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height5angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height5angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height5angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height5angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height5angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height5angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height5angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height5angle75;
            } else {
                angleFile = R.raw.height5angle85;
            }
        } else if (height < 7) {
            if (angle <= -80) {
                angleFile = R.raw.height6angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height6angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height6angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height6angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height6angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height6angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height6angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height6angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height6angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height6angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height6angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height6angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height6angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height6angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height6angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height6angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height6angle75;
            } else {
                angleFile = R.raw.height6angle85;
            }
        } else {
            if (angle <= -80) {
                angleFile = R.raw.height7angle_85;
            } else if (angle <= -70) {
                angleFile = R.raw.height7angle_75;
            } else if (angle <= -60) {
                angleFile = R.raw.height7angle_65;
            } else if (angle <= -50) {
                angleFile = R.raw.height7angle_55;
            } else if (angle <= -40) {
                angleFile = R.raw.height7angle_45;
            } else if (angle <= -30) {
                angleFile = R.raw.height7angle_35;
            } else if (angle <= -20) {
                angleFile = R.raw.height7angle_25;
            } else if (angle <= -10) {
                angleFile = R.raw.height7angle_15;
            } else if (angle <= 0) {
                angleFile = R.raw.height7angle_5;
            } else if (angle <= 10) {
                angleFile = R.raw.height7angle5;
            } else if (angle <= 20) {
                angleFile = R.raw.height7angle15;
            } else if (angle <= 30) {
                angleFile = R.raw.height7angle25;
            } else if (angle <= 40) {
                angleFile = R.raw.height7angle35;
            } else if (angle <= 50) {
                angleFile = R.raw.height7angle45;
            } else if (angle <= 60) {
                angleFile = R.raw.height7angle55;
            } else if (angle <= 70) {
                angleFile = R.raw.height7angle65;
            } else if (angle <= 80) {
                angleFile = R.raw.height7angle75;
            } else {
                angleFile = R.raw.height7angle85;
            }
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
        soundHandler.sendMessageDelayed(msg, distance*500L);

    }






}
