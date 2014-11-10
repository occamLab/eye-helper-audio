package com.eyehelper.positionalaudiocvtesting.handler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by sihrc on 11/10/14.
 */
public class OrientationHandler implements SensorEventListener {
    public float azimuth = 0.0f;
    public float pitch = 0.0f;
    public float roll = 0.0f;
    Context context;
    SensorManager manager;
    //Sensors
    Sensor accelerometer, magnetometer;
    //Listener registered
    boolean registered;
    //Values
    private float[] gravity;
    private float[] geomagnetic;

    public OrientationHandler(Context context) {
        this.context = context;
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void init() {
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        resume();
    }

    public void resume() {
        if (!registered) {
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            registered = true;
        }
    }

    public void pause() {
        if (registered) {
            manager.unregisterListener(this);
            registered = false;
        }
    }


    //When the reading of a sensor changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
            }
        }
    }

    //When the accuracy of a sensor changes
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
