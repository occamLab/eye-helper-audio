package com.eyehelper.positionalaudiocvtesting;

import android.app.Application;

/**
 * Created by sihrc on 10/3/14.
 */
public class EyeHelper extends Application {
    public static EyeHelper app;

    final public static String serverAddress = "";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
