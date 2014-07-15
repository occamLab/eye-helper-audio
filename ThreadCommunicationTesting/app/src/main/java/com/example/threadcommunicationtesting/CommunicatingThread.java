package com.example.threadcommunicationtesting;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Sophia on 7/14/14.
 */
public class CommunicatingThread extends Thread {
    private Handler activityHandler;
    private Handler threadHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){

            System.err.println("Message"+msg.arg1);
            Message msgUI = new Message();
            msgUI.arg1 = 1;

            activityHandler.sendMessage(msgUI);

        }
    };

    public CommunicatingThread (Handler parentHandler){
        activityHandler = parentHandler;
    }

    public Handler getHandler(){
        return threadHandler;
    }

    @Override
    public void run(){

    while (true){

        System.err.println("running");


    }

    }
}
