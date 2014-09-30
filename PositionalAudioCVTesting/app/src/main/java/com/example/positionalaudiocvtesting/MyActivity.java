package com.example.positionalaudiocvtesting;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {
    //The Tag for the logcat
    private static final String TAG = "OCVSample::Activity";
    //Are we tracking a color
    private boolean isColorSelected = false;
    //The matrix of the image in rgba
    private Mat rgba;
    //the color blob rgba (4 values)
    private Scalar blobColorRgba;
    //The color blob detector
    private ColorBlobDetector detector;
    //The color spectrum of the image
    private Mat spectrum;
    //The color of the contours
    private final Scalar CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

    //Camera Parameters
    private double focal = 2.8;
    private int objWidth = 127;
    private int imgHeight = 512;
    private int sensorHeight = 4;

    //Position Variables
    private double distance = 100;
    private double angle = 0;
    private double height = 0;

    //Sound Variables
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int currentFile = R.raw.height0angle_85;

    //Camera View
    @InjectView(R.id.camera_view)
    CameraBridgeViewBase openCvCameraView;

    //Text Views
    @InjectView(R.id.text_view_angle)
    TextView angleText;
    @InjectView(R.id.text_view_height)
    TextView heightText;
    @InjectView(R.id.text_view_distance)
    TextView distanceText;
    @InjectView(R.id.text_view_azimuth)
    TextView azimuthText;
    @InjectView(R.id.text_view_pitch)
    TextView pitchText;
    @InjectView(R.id.text_view_roll)
    TextView rollText;

    //Sensor Data
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float azimuth = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    private float[] gravity;
    private float[] geomagnetic;

    private volatile boolean soundRunning;

    //This is what we use to determine whether or not the app loaded successfully
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                //The app loaded successfully
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    openCvCameraView.enableView();
                    //openCvCameraView.setOnTouchListener(MyActivity.this);
                }
                break;
                //Otherwise it didn't
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //When the activity is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.color_blob_detection_surface_view);
        ButterKnife.inject(this);

        // Sensors! (To get the head tilt information)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Make this activity the listener for our camera view
        openCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        //When the app is paused, stop the camera and pause the music
        sensorManager.unregisterListener(this);
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
        soundRunning = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        //When the app is resumed, restart the camera asynchronously
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, loaderCallback);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        startSoundThread();
        super.onResume();
    }

    private void startSoundThread() {
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

    //When a user swipes down to quit, finish the app
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return false;
    }

    //Right after the camera starts
    @Override
    public void onCameraViewStarted(int width, int height) {
        //Declare the image matrix to be a matrix of the height and width of the image
        rgba = new Mat(height, width, CvType.CV_8UC4);
        //Make a new detector
        detector = new ColorBlobDetector();
        //Declare the spectrum, but don't put anything into it yet
        spectrum = new Mat();
        //the Rgba is 255
        blobColorRgba = new Scalar(255);
        //We are tracking a color
        isColorSelected = true;
        //Tracking the color black
        detector.setHsvColor(new Scalar(130, 25, 55, 0));
    }

    //when the camera view stops
    @Override
    public void onCameraViewStopped() {
        //When the camera view stops, release the camera
        rgba.release();
    }

    //Every time we get a new camera frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //the image matrix is the input frame converted to RGBa
        rgba = inputFrame.rgba();
        //Log.e(TAG, "Size " + rgba.size());
        //if we are tracking a color
        if (isColorSelected) {
            //process the color
            detector.process(rgba);
            //get the contours from that color
            List<MatOfPoint> contours = detector.getContours();
            //Log.e(TAG, "Contours count: " + contours.size());
            //Draw the contours
            //Imgproc.drawContours(rgba, contours, -1, CONTOUR_COLOR);

            //For each set of contours, draw a rectangle
            if (!contours.isEmpty()) {
                Rect largestContourRect = Imgproc.boundingRect(contours.get(0));

                for (MatOfPoint contour : contours) {
                    Rect bounding = Imgproc.boundingRect(contour);
                    if (bounding.area() > largestContourRect.area()) {
                        largestContourRect = bounding;
                    }
                }

                Point p1 = new Point(largestContourRect.x, largestContourRect.y);

                Point p2 = new Point(largestContourRect.x + largestContourRect.width, largestContourRect.y + largestContourRect.height);

                Core.rectangle(rgba, p1, p2, CONTOUR_COLOR, 1);
                //Log.e(TAG, "Width: " + largestContourRect.width + " Height: " + largestContourRect.height);
                //Log.e(TAG, "X: " + largestContourRect.x + " Y: " + largestContourRect.y);
                //distance in mm


                double tempDistance = (focal * objWidth * imgHeight) / (largestContourRect.width * sensorHeight);
                //Smoothing the distance signal
                double smoothing = 10.0;
                distance += ((tempDistance / 10) - distance) / smoothing;

                //Calculating angle
                //angle = (76 * (largestContourRect.x + largestContourRect.width / 2) / 512.0) - 38;
                angle = -((180 * (largestContourRect.x + largestContourRect.width / 2) / 512.0) - 90);


                double elivAngle = (62 * (largestContourRect.y + largestContourRect.height / 2) / 288.0) - 31;

                //Assuming that the average person is 175 cm
                //height = 175 - distance**Math.sin(Math.toRadians(elivAngle));
                height = (3 * (1 - ((largestContourRect.y + largestContourRect.height / 2) / 288.0))) + 2;
                //get the current sound file based on angle and height
                currentFile = getSoundFile();

//                Log.e(TAG, "Distance: " + distance);
//                Log.e(TAG, "Angle: " + angle);
//                Log.e(TAG, "Height: " + height);

                // Update Distance, Height, and Angle
                updateText();
            }
            //Define the color label
            Mat colorLabel = rgba.submat(4, 68, 4, 68);
            //set the color label to the blob's average RGBa
            colorLabel.setTo(blobColorRgba);
            //Update the spectrum label
            Mat spectrumLabel = rgba.submat(4, 4 + spectrum.rows(), 70, 70 + spectrum.cols());
            spectrum.copyTo(spectrumLabel);
        }

        //Return the RGBa for the image
        return rgba;
    }

    //Update text on the glass's display
    public void updateText() {
        //In order to update UI elements, we have to run on the UI thread
        MyActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Update the text views
                distanceText.setText("Distance: " + String.format("%.1f", distance));
                angleText.setText("Angle: " + String.format("%.1f", angle));
                heightText.setText("Height: " + String.format("%.1f", height));

                azimuthText.setText("Azimuth: " + String.format("%.2f", Math.toDegrees(azimuth)));
                pitchText.setText("Pitch: " + String.format("%.2f", pitch));
                rollText.setText("Roll: " + String.format("%.2f", Math.toDegrees(roll)));
            }
        });

    }

    //Decide which sound file to play
    public int getSoundFile() {
        Log.i("Calculating which sound file", "height :" + height + ", angle :" + angle);

        // Floor the double and limit to 0 <= height <= 7
        int roundedHeight = Math.max(Math.min((int) Math.floor(height),7),0);
        // Round to the nearest multiple of 5 and limit to -85 <= angle <= 85
        int roundedAngle = Math.max(Math.min(5*(Math.round((int)(height/5))), 85), -85);
        // Chosen file based on height and angle -> index in resource array
        int chosenFile = roundedHeight * 16 + (roundedAngle + 5)/10 + 9;

        Log.i("Getting Sound File", "roundedHeight: " + roundedHeight + ", roundedAngle" + roundedAngle + ", chosen soundFile: " + chosenFile);
        return getResources().obtainTypedArray(R.array.sound_files).getResourceId(chosenFile, -1);
    }

    //Play a sound given the resource
    public void playSound() {
        //If a sound is currently playing, stop it.
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //Set up the media player
        mediaPlayer = MediaPlayer.create(this.getApplicationContext(), currentFile);
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

    //When the accuracy of a sensor changes
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
                azimuth = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
            }
        }
    }
}

