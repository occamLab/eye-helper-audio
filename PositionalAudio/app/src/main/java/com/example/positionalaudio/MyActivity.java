package com.example.positionalaudio;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
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

    public void playSound(View v){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.dovecooing);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    public byte[] readSoundFile(View v) throws IOException {
        InputStream in = this.getResources().openRawResource(R.raw.dovecooing);
        BufferedInputStream bis = new BufferedInputStream(in, 8000);
// Create a DataInputStream to read the audio data from the saved file
        DataInputStream dis = new DataInputStream(bis);

        byte[] music = new byte[in.available()];
        int i = 0; // Read the file into the "music" array
        while (dis.available() > 0) {
            // dis.read(music[i]); // This assignment does not reverse the order
            music[i]=dis.readByte();
            i++;
        }

        dis.close();

        return music;
    }

    public byte[] delayBasedOnPosition(byte[] soundFile, int[] posSound, int samplingRate){
    // The location of the camera (and the right ear) is at 0,0
        double headWidth = 0.15;
        int [] soundPos = {1, 45};
        int speedOfSound = 343;
        byte [] leftSoundFile;
        byte [] rightSoundFile;


    // Given a distance from the camera and a angle to the camera, what is the distance from the left ear?
        double distanceToLeftEar = Math.sqrt(Math.pow(soundPos[0]*Math.cos(Math.toRadians(soundPos[1])),2) + Math.pow(soundPos[0]*(Math.sin(Math.toRadians(soundPos[1])))-headWidth,2));
        double byteDifference = (soundPos[0] - distanceToLeftEar)/speedOfSound*samplingRate;
        int numBytes = (int) byteDifference;
        byte[] byteDelay = new byte[numBytes];


        if (byteDifference > 0) {
            //delay on the left side
            leftSoundFile =

        } else {
            //delay on the right side
        }


    }
}
