package com.example.positionalaudiocvtesting;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

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


public class MyActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    //The Tag for the logcat
    private static final String  TAG = "OCVSample::Activity";
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
    double distance = -1;



    //The camera view
    private CameraBridgeViewBase mOpenCvCameraView;
    //This is what we use to determine whether or not the app loaded successfully
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                //The app loaded successfully
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(MyActivity.this);
                } break;
                //Otherwise it didn't
                default:
                {
                    super.onManagerConnected(status);
                } break;
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
    }

    @Override
    public void onPause() {
        //When the app is paused, stop the camera
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        //When the app is resumed, restart the camera asynchronously
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        //When the app is destroyed, stop the camera
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        //We are tracking a color
        mIsColorSelected = true;
        //Tracking the color black
        mDetector.setHsvColor(new Scalar(130,25,55,0));
    }

    public void onCameraViewStopped() {
        //When the camera view stops, release the camera
        mRgba.release();
    }

    //Every time we get a new camera frame
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //the image matrix is the input frame converted to RGBa
        mRgba = inputFrame.rgba();
        Log.e(TAG, "Image Size" + mRgba.size());
                //if we are tracking a color
        if (mIsColorSelected) {
            //process the color
            mDetector.process(mRgba);
            //get the contours from that color
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
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
                double tempDistance = (focal * objWidth * imgHeight) / (largestContourRect.width * sensorHeight);


                if (Math.abs(tempDistance-distance) > 100){
                    distance = tempDistance;
                }

                Log.e(TAG, "Distance: " + distance);
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
}

