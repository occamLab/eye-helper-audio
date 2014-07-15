package com.example.positionalaudio;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by Sophia on 7/11/14.
 */
public class SoundLooping extends Thread {
    private Handler activityHandler;
    private Handler threadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

//            System.err.println("Message"+msg.arg1);
//            Message msgUI = new Message();
//            msgUI.arg1 = 1;
//
//            activityHandler.sendMessage(msgUI);
//        }

            if (msg.what == 0){
                prevSeekBarProgress = seekBarProgress;
                seekBarProgress = msg.arg1;
            } else if (msg.what == 1){
                if (msg.arg1 ==0){
                    distance = 1;
                    waitTime = 100;

                }else{
                    distance = msg.arg1;
                    waitTime = distance * 100;
                }

            }

            System.err.println("skb="+seekBarProgress);
            System.err.println("dist="+distance);

            Message msgUI = new Message();
            msgUI.arg1 = 1;

            activityHandler.sendMessage(msgUI);
        }
    };
    int prevSeekBarProgress = 0;
    int seekBarProgress = 0;
    int distance = 1;
    long waitTime = distance * 100;


    public SoundLooping (Handler parentHandler){

        activityHandler = parentHandler;
    }

    public Handler getHandler(){
        return threadHandler;
    }

    @Override
    public void run(){

        long lastPlayTime = SystemClock.elapsedRealtime();


        int angleVal = seekBarProgress - 90;
        int angleFile;
        long elapsedTime;

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

        while (true){
            if (prevSeekBarProgress != seekBarProgress){
                angleVal = seekBarProgress - 90;

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
            }

            elapsedTime = SystemClock.elapsedRealtime() - lastPlayTime;

            if (elapsedTime > 6000){
                lastPlayTime = SystemClock.elapsedRealtime();
                Message msg = Message.obtain();
                msg.what = 2;
                msg.arg1 = angleFile;
                activityHandler.sendMessage(msg);
            }


        }


    }


}
