package com.example.positionalaudiocvtesting;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;


public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //The Tag for the logcat
    private static final String TAG = "OCVSample::Activity";
    //Are we tracking a color
    private boolean mIsColorSelected = false;
    //The matrix of the image in rgba
    private Mat mRgba;
    //the color blob rgba (4 values)
    private Scalar mBlobColorRgba;
    //The HSV value of the color we are tracking
    private Scalar mBlobColorHsv;
    //The color blob detector
    private ColorBlobDetector mDetector;
    //The color spectrum of the image
    private Mat mSpectrum;
    //The size of the spectrum
    private Size SPECTRUM_SIZE;
    //The color of the contours
    private Scalar CONTOUR_COLOR;

    //Camera Parameters
    double focal = 2.8;
    int objWidth = 127;
    int imgHeight = 512;
    int sensorHeight = 4;

    //Position Variables
    double distance = 100;
    double angle = 0;
    double height = 0;

    //Sound Variables
    MediaPlayer mediaPlayer = new MediaPlayer();
    long lastPlayTime = System.nanoTime();
    int currentFile = R.raw.height0angle_85;
    boolean soundChanged = true;


    //Text Views
    TextView angleText;
    TextView heightText;
    TextView distanceText;

    //Initialize the Message Handler
    public Handler soundHandler = new Handler() {

        //When the media player gets a message
        @Override
        public void handleMessage(Message msg) {

            //We only care about the message if it's telling us to play the most current file
            if (msg.arg1 == currentFile) {
                //If this is a message letting us know that the most current file has changed
                if (msg.what == 0) {
                    Log.e(TAG, "Time" + (System.nanoTime() - lastPlayTime));
                    //If the time since we last played a sound is less than 0.5 seconds try again later
                    if ((System.nanoTime() - lastPlayTime) < 500000000L) {
                        Log.e(TAG, "Playing");
                        //Send a message that has been delayed
                        Message msgDel = Message.obtain();
                        //Msg.what = 0 means that this is to play a changed sound file
                        msgDel.what = 0;
                        msgDel.arg1 = currentFile;
                        //Delay sending
                        soundHandler.sendMessageDelayed(msgDel, 100);
                    //If the time since the last sound is more than 0.5s play the sound now and update last played time
                    } else {
                        playSound(currentFile);
                        soundChanged = false;
                        lastPlayTime = System.nanoTime();
                    }


                    //The message is from wanting to repeat the last sound played
                } else if (msg.what == 1 && !soundChanged) {
                    playSound(currentFile);
                    lastPlayTime = System.nanoTime();
                }
            }
        }

    };

    //The camera view
    private CameraBridgeViewBase mOpenCvCameraView;
    //This is what we use to determine whether or not the app loaded successfully
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                //The app loaded successfully
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(MyActivity.this);
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

    //On the constructor for this activity
    public MyActivity() {
        Log.i(TAG, "Instantiated new " + ((Object) this).getClass());
    }

    //When the activity is created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        //Set up the layout to fill the whole screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        //Set the camera to appear on the whole screen
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        //Make this class, which extends CameraVeiwListener the listener
        mOpenCvCameraView.setCvCameraViewListener(this);

        //Display the angle, height, and distance on the sceen on the glass
        angleText = (TextView) findViewById(R.id.textViewA);
        heightText = (TextView) findViewById(R.id.textViewH);
        distanceText = (TextView) findViewById(R.id.textViewD);

    }

    @Override
    public void onPause() {
        //When the app is paused, stop the camera and pause the music
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        mediaPlayer.release();

    }

    @Override
    public void onResume() {
        //When the app is resumed, restart the camera asynchronously
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        //When the app is destroyed, stop the camera and the sounds
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        mediaPlayer.release();
    }

    //When a user swipes down to quit, finish the app
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            finish();
        }
        return false;
    }

    //Right after the camera starts
    public void onCameraViewStarted(int width, int height) {
        //Declare the image matrix to be a matrix of the height and width of the image
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        //Make a new detector
        mDetector = new ColorBlobDetector();
        //Declare the spectrum, but don't put anything into it yet
        mSpectrum = new Mat();
        //the Rgba is 255
        mBlobColorRgba = new Scalar(255);
        //The HSV is 255
        mBlobColorHsv = new Scalar(255);
        //The spectrum is 200 x 64
        SPECTRUM_SIZE = new Size(200, 64);
        //The contour color, declared as red
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
        //We are tracking a color
        mIsColorSelected = true;
        //Tracking the color black
        mDetector.setHsvColor(new Scalar(130, 25, 55, 0));
    }

    //when the camera view stops
    public void onCameraViewStopped() {
        //When the camera view stops, release the camera
        mRgba.release();
    }

    //Every time we get a new camera frame
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //the image matrix is the input frame converted to RGBa
        mRgba = inputFrame.rgba();
        //Log.e(TAG, "Size " + mRgba.size());
        //if we are tracking a color
        if (mIsColorSelected) {
            //process the color
            mDetector.process(mRgba);
            //get the contours from that color
            List<MatOfPoint> contours = mDetector.getContours();
            //Log.e(TAG, "Contours count: " + contours.size());
            //Draw the contours
            //Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            //For each set of contours, draw a rectangle
            if (contours.size() > 0) {
                Rect largestContourRect = Imgproc.boundingRect(contours.get(0));

                for (int i = 0; i < contours.size(); i++) {
                    Rect bounding = Imgproc.boundingRect(contours.get(i));
                    if (bounding.area() > largestContourRect.area()) {
                        largestContourRect = bounding;
                    }
                }

                Point p1 = new Point(largestContourRect.x, largestContourRect.y);

                Point p2 = new Point(largestContourRect.x + largestContourRect.width, largestContourRect.y + largestContourRect.height);

                Core.rectangle(mRgba, p1, p2, CONTOUR_COLOR, 1);
                //Log.e(TAG, "Width: " + largestContourRect.width + " Height: " + largestContourRect.height);
                //Log.e(TAG, "X: " + largestContourRect.x + " Y: " + largestContourRect.y);
                //distance in mm


                double tempDistance = (focal * objWidth * imgHeight) / (largestContourRect.width * sensorHeight);
                //Smoothing the distance signal
                double smoothing = 10.0;
                distance += ((tempDistance / 10) - distance) / smoothing;

                //Calculating angle
                angle = (76 * (largestContourRect.x + largestContourRect.width / 2) / 512.0) - 38;

                double elivAngle = (62 * (largestContourRect.y + largestContourRect.height / 2) / 288.0) - 31;

                //Assuming that the average person is 175 cm
                //height = 175 - distance*Math.sin(Math.toRadians(elivAngle));

                //get the current sound file based on angle and height
                int tempCurrentFile = getSoundFile();

                //if this isn't the sound file we are playing
                if (tempCurrentFile != currentFile) {
                    //Send a message to play a different file
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.arg1 = tempCurrentFile;
                    //set currentFile
                    currentFile = tempCurrentFile;
                    soundChanged = true;
                    soundHandler.sendMessage(msg);
                }

//                Log.e(TAG, "Distance: " + distance);
//                Log.e(TAG, "Angle: " + angle);
//                Log.e(TAG, "Height: " + height);

                // Update Distance, Height, and Angle
                updateText();


            }
            //Define the color label
            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            //set the color label to the blob's average RGBa
            colorLabel.setTo(mBlobColorRgba);
            //Update the spectrum label
            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        //Return the RGBa for the image
        return mRgba;
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
    public void playSound(int fileResource) {
        //If a sound is currently playing, stop it.
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //Set up the media player
        mediaPlayer = MediaPlayer.create(this.getApplicationContext(), fileResource);
        //Start playing once prepared
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        //Listen for completion
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Release the media player on completion
                mp.release();
            }
        });

        //Send a message to repeat the sound file some time later
        Message msg = Message.obtain();
        //Msg.what = 1 means that this is to repeat a sound file
        msg.what = 1;
        msg.arg1 = fileResource;
        //Delay sending based on the distance from the object
        soundHandler.sendMessageDelayed(msg, 800);

    }
}

