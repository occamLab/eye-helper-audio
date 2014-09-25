package com.eyehelper.positionalaudiocvtesting;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
    private CameraBridgeViewBase openCvCameraView;

    //Text Views
    private TextView angleText;
    private TextView heightText;
    private TextView distanceText;
    private TextView azimuthText;
    private TextView pitchText;
    private TextView rollText;

    //Sensor Data
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float azimuth = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;
    private float[] gravity;
    private float[] geomagnetic;

    private Thread soundThread;
    private volatile boolean soundRunning;

    private Pair<Point, Point> coordinates;
    private MatOfKeyPoint trainingImageKeypoints;
    private Mat trainingImageDescriptors;

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

        //Set the camera to appear on the whole screen
        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        //Make this class, which extends CameraVeiwListener the listener
        openCvCameraView.setCvCameraViewListener(this);
        final GestureDetector gestureDetector = new GestureDetector(this, new TapDetector());
        openCvCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        //Display the angle, height, and distance on the screen on the glass
        angleText = (TextView) findViewById(R.id.textViewA);
        heightText = (TextView) findViewById(R.id.textViewH);
        distanceText = (TextView) findViewById(R.id.textViewD);
        azimuthText = (TextView) findViewById(R.id.textViewAzimuth);
        pitchText = (TextView) findViewById(R.id.textViewPitch);
        rollText = (TextView) findViewById(R.id.textViewRoll);

        //Sensors! (To get the head tilt information)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    class TapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.v(TAG, String.format("x: %f, y: %f", e.getX(), e.getY()));
            return super.onSingleTapUp(e);
        }
    }

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
        soundThread = new Thread(new Runnable() {
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

    private Mat getTrainingImage(Pair<Point, Point> coordinates) {
        // TODO: get the training image based on coordinates
        return new Mat();
    }

    private void setTrainingArea(Pair<Point, Point> coordinates) {
        Mat trainingImage = getTrainingImage(coordinates);
        this.trainingImageKeypoints = detectKeypoints(trainingImage);
        this.trainingImageDescriptors = computeDescriptors(trainingImage, trainingImageKeypoints);
        this.coordinates = coordinates;
    }
    
    //Every time we get a new camera frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (coordinates != null) {
            matchObject(inputFrame.gray());
        }
        return inputFrame.rgba();
    }

    private void matchObject(Mat image) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        MatOfKeyPoint keypoints = detectKeypoints(image);
        KeyPoint[] keypointsArray = keypoints.toArray();
        Mat descriptors = computeDescriptors(image, keypoints);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        List<MatOfDMatch> matchMatrices = new ArrayList<MatOfDMatch>();
        matcher.knnMatch(trainingImageDescriptors, descriptors, matchMatrices, 2);

        // Filter out the outliers
        List<Point> goodMatches = new ArrayList<Point>();
        for (MatOfDMatch matchMatrix : matchMatrices) {
            // TODO: make sure we are getting the correct m and n
            List<DMatch> matches = matchMatrix.toList();

            DMatch m = matches.get(0);
            DMatch n = matches.get(1);

            if (m.distance < 0.75 * n.distance) {
                double x = keypointsArray[m.trainIdx].pt.x;
                double y = keypointsArray[m.trainIdx].pt.y;
                goodMatches.add(new Point(x, y));
            }
        }

        meanShift(goodMatches, 10);
    }

    private Point meanShift(List<Point> keypoints, double threshold) {
        Point hypothesis = getCenter();

        // need more than one keypoint to update hypothesis
        if (keypoints.size() <= 1) {
            return hypothesis;
        }

        // assigns a value to the weighting constant -> based on
        // experimental results on cropped cookie_00274
        double c = 0.0001;

        // arbitrarily set diff high to go through loop at least once
        double diff = 1000;

        while (diff > threshold) {

            // sets up lists of weights and weights*position
            // TODO: should these be doubles or ints or floats?
            List<Double> x_weights = new ArrayList<Double>();
            List<Double> y_weights = new ArrayList<Double>();
            List<Double> weighted_x = new ArrayList<Double>();
            List<Double> weighted_y = new ArrayList<Double>();

            // Creates a list of weighted points, where points near the
            // hypothesis have a larger weight

            Point last_guess = hypothesis;

            for (Point keypoint : keypoints) {
                double x_val = Math.exp(-c * Math.pow((keypoint.x - last_guess.x), 2));
                x_weights.add(x_val);
                weighted_x.add(x_val * keypoint.x);
                double y_val = Math.exp(-c * Math.pow((keypoint.y - last_guess.y), 2));
                y_weights.add(y_val);
                weighted_y.add(y_val * keypoint.y);
            }

            // TODO: should x, y be floats, ints, or doubles?
            // finds 'center of mass' of the points to determine new center
            double x = sum(weighted_x) / sum(x_weights);
            double y = sum(weighted_y) / sum(y_weights);

            // update hypothesis
            hypothesis = new Point(x, y);

            // difference between the current and last guess
            diff = Math.sqrt(Math.pow((last_guess.x - x), 2) + Math.pow((last_guess.y - y), 2));

            //  Finding the radius:
            List<Double> norm_weights = new ArrayList<Double>();
            for (int i = 0; i < x_weights.size(); i++) {
                norm_weights.add(norm(new Point(x_weights.get(i), y_weights.get(i))));
            }

            double avg_weight = sum(norm_weights) / norm_weights.size();

            // TODO: get standard deviation using java
            // double std_weight = Math.std(norm_weights);

            //  Threshold based on standard deviations (to account for different kp density scenarios)
            threshold = avg_weight; // - .25*std_weight
            List<Double> inliers = new ArrayList<Double>();

            //  Radius corresponds to the farthest-away keypoints are in the threshold from center of mass (x,y)
            for (int index = 0; index < norm_weights.size(); index++) {
                if (norm_weights.get(index) > threshold) {
                    Point inliearCoordinates = new Point(keypoints.get(index).x - x, keypoints.get(index).y - y);
                    inliers.add(norm(inliearCoordinates));
                }
            }

            // TODO: why do we need radius?
            double radius = Collections.max(inliers);
        }
        return hypothesis;
    }

    private double norm(Point point) {
        return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
    }

    private double sum(List<Double> list) {
        double sum = 0;
        for (double number : list) {
            sum += number;
        }
        return sum;
    }

    private Point getCenter() {
        double x = (coordinates.first.x + coordinates.second.x) / 2;
        double y = (coordinates.first.y + coordinates.second.y) / 2;
        return new Point(x, y);
    }

    //Convert a scalar HSV to RGBa
    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
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
        int angleFile;

        //If the height is less than one, chose a sound file based on angle. Angles are in the middle of angle ranges of 10 degrees
        //For Example angles 1-10 -> angle5
        if (height < 1) {
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
            //If the height is less than 2, choose a sound file corresponding to height 1 and some angle
        } else if (height < 2) {
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
            //If the height is less than 3, choose a sound file corresponding to height 2 and the angle
        } else if (height < 3) {
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
            //If the height is less than 4, choose a sound file corresponding to height 3 and the angle
        } else if (height < 4) {
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
            //If the height is less than 5, choose a sound file corresponding to height 4 and the angle
        } else if (height < 5) {
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

            //If the height is less than 6, choose a sound file corresponding to height 5 and the angle
        } else if (height < 6) {
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
            //If the height is less than 7, choose a sound file corresponding to height 6 and the angle
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
            //Otherwise, choose a sound file corresponding to height 7 and the angle.
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

        //Return which file to play
        return angleFile;
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

    private MatOfKeyPoint detectKeypoints(Mat img) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(img, keypoints);
        return keypoints;
    }

    private Mat computeDescriptors(Mat img, MatOfKeyPoint keypoints) {
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        Mat descriptors = new Mat();
        descriptor.compute(img, keypoints, descriptors);
        return descriptors;
    }
}

