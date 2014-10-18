package com.eyehelper.positionalaudiocvtesting;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.ContextThemeWrapper;

/**
 * Created by greenteawarrior on 10/18/14.
 * Contains methods for calculating sound position and playing sounds to provide info/feedback to the blind user.
 */
public class PositionalAudio {

    //Sound Variables
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int currentFile = R.raw.height0angle_85;

    private double distance = 100;
    private double angle = 0;
    private double height = 0;

    //Decide which sound file to play
    public int getSoundFile(ContextThemeWrapper contextThemeWrapper) {
        Log.i("Calculating which sound file", "height :" + height + ", angle :" + angle);

        // Floor the double and limit to 0 <= height <= 7
        int roundedHeight = Math.max(Math.min((int) Math.floor(height),7),0);
        // Round to the nearest multiple of 5 and limit to -85 <= angle <= 85
        int roundedAngle = Math.max(Math.min(5*(Math.round((int)(height/5))), 85), -85);
        // Chosen file based on height and angle -> index in resource array
        int chosenFile = roundedHeight * 16 + (roundedAngle + 5)/10 + 9;

        Log.i("Getting Sound File", "roundedHeight: " + roundedHeight + ", roundedAngle" + roundedAngle + ", chosen soundFile: " + chosenFile);
        return contextThemeWrapper.getResources().obtainTypedArray(R.array.sound_files).getResourceId(chosenFile, -1);
    }

    //Play a sound given the resource
    public void playSound(MyActivity myActivity) {
        //If a sound is currently playing, stop it.
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //Set up the media player
        mediaPlayer = MediaPlayer.create(myActivity.getApplicationContext(), currentFile);
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

    //Update text on the glass's display
    public void updateText(MyActivity myActivity) {
        //In order to update UI elements, we have to run on the UI thread
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Update the text views
                myActivity.distanceText.setText("Distance: " + String.format("%.1f", distance));
                myActivity.angleText.setText("Angle: " + String.format("%.1f", angle));
                myActivity.heightText.setText("Height: " + String.format("%.1f", height));

                myActivity.azimuthText.setText("Azimuth: " + String.format("%.2f", Math.toDegrees(myActivity.azimuth)));
                myActivity.pitchText.setText("Pitch: " + String.format("%.2f", myActivity.pitch));
                myActivity.rollText.setText("Roll: " + String.format("%.2f", Math.toDegrees(myActivity.roll)));
            }
        });

    }
}
