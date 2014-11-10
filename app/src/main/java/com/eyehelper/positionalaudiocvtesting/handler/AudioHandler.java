package com.eyehelper.positionalaudiocvtesting.handler;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.eyehelper.positionalaudiocvtesting.R;

/**
 * Created by greenteawarrior on 10/18/14.
 * Contains methods for calculating sound position and playing sounds to provide info/feedback to the blind user.
 */
public class AudioHandler {
    Context context;
    boolean soundRunning;

    //Sound Variables
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //Camera Parameters
    final static private double FOCAL = 2.8;
    final static private int OBJ_WIDTH = 127;
    final static private int IMG_HEIGHT = 512;
    final static private int SENSOR_HEIGHT = 4;

    private double x, y;

    public AudioHandler(Context context, double x, double y) {
        this.context = context;
        this.x = x;
        this.y = y;
    }

    public void startSoundThread() {
        soundRunning = true;
        Thread soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (soundRunning) {
                    try {
                        Thread.sleep(500);
                        playSound();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        soundThread.start();
    }

    //Play a sound given the resource
    private void playSound() {
        //If a sound is currently playing, stop it.
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //Set up the media player
        mediaPlayer = MediaPlayer.create(context, getSoundFile(0, 0));
        mediaPlayer.start();

        //Listen for completion
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Release the media player on completion
                mp.release();
            }
        });
    }

    //Decide which sound file to play
    private int getSoundFile(float height, float angle) {
        Log.i("Calculating which sound file", "height :" + height + ", angle :" + angle);

        // Floor the double and limit to 0 <= height <= 7
        int roundedHeight = Math.max(Math.min((int) Math.floor(height), 7), 0);
        // Round to the nearest multiple of 5 and limit to -85 <= angle <= 85
        int roundedAngle = Math.max(Math.min(5 * (Math.round((int) (height / 5))), 85), -85);
        // Chosen file based on height and angle -> index in resource array
        int chosenFile = roundedHeight * 16 + (roundedAngle + 5) / 10 + 9;

        Log.i("Getting Sound File", "roundedHeight: " + roundedHeight + ", roundedAngle" + roundedAngle + ", chosen soundFile: " + chosenFile);
        return context.getResources().obtainTypedArray(R.array.sound_files).getResourceId(chosenFile, -1);
    }


//    //Update text on the glass's display
//    public void updateText(final MyActivity myActivity) {
//        //In order to update UI elements, we have to run on the UI thread
//        myActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //Update the text views
//                myActivity.distanceText.setText("Distance: " + String.format("%.1f", distance));
//                myActivity.angleText.setText("Angle: " + String.format("%.1f", angle));
//                myActivity.heightText.setText("Height: " + String.format("%.1f", height));
//
//                myActivity.azimuthText.setText("Azimuth: " + String.format("%.2f", Math.toDegrees(myActivity.azimuth)));
//                myActivity.pitchText.setText("Pitch: " + String.format("%.2f", myActivity.pitch));
//                myActivity.rollText.setText("Roll: " + String.format("%.2f", Math.toDegrees(myActivity.roll)));
//            }
//        });
//    }
}
