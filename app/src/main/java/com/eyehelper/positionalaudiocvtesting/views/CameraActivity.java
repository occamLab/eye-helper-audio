package com.eyehelper.positionalaudiocvtesting.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.eyehelper.positionalaudiocvtesting.R;
import com.eyehelper.positionalaudiocvtesting.handler.AudioHandler;
import com.eyehelper.positionalaudiocvtesting.handler.ObjectTracker;
import com.eyehelper.positionalaudiocvtesting.handler.OrientationHandler;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    //Camera View
    @InjectView(R.id.camera_view)
    CameraBridgeViewBase cameraPreview;

    private double imageRows;
    private double imageCols;
    private int frames;
    private static final int FPS = 10;

    private ObjectTracker objectTracker;
    private AudioHandler positionalAudio;
    private OrientationHandler orientationHandler;


    //This is what we use to determine whether or not the app loaded successfully
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                //The app loaded successfully
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraPreview.enableView();
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
        setContentView(R.layout.activity_main);

        // Inject Views
        ButterKnife.inject(this);

        objectTracker = new ObjectTracker();
        positionalAudio = new AudioHandler(this, objectTracker.hypothesis.x, objectTracker.hypothesis.y);
        orientationHandler = new OrientationHandler(this);


        final GestureDetector gestureDetector = new GestureDetector(this, new TapGestureListener());
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        // why does this line of code happen twice?
        // Make this activity the listener for our camera view
        cameraPreview.setCvCameraViewListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //When the app is resumed, restart the camera asynchronously
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, loaderCallback);
        orientationHandler.resume();
        positionalAudio.startSoundThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        //When the app is paused, stop the camera and pause the music
        orientationHandler.pause();
        if (cameraPreview != null) {
            cameraPreview.disableView();
        }
    }


    //Right after the camera starts
    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    //when the camera view stops
    @Override
    public void onCameraViewStopped() {
    }

    //Every time we get a new camera frame
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat greyImage = inputFrame.gray();
        imageCols = greyImage.cols();
        imageRows = greyImage.rows();

        if (!objectTracker.hasTrainingImage()) {
            objectTracker.saveCurrentImage(greyImage);
        }

        if (!greyImage.empty() && frames == FPS) {
            Mat resized = new Mat();
            Imgproc.resize(greyImage, resized, new Size(), .3, .3, 1);
            objectTracker.matchObject(resized);
            frames = 0;
        } else {
            frames++;
        }

//        if (objectTracker.coordinates != null) {
//            Core.rectangle(greyImage, objectTracker.coordinates.first, objectTracker.coordinates.first, new Scalar(0, 0, 255), 0, 8, 0);
//        }
        return greyImage;
    }


    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        // TODO: Should TapGestureListener be a class within MyActivity or its own thing??
        // TODO: (futurImplement training image selection from the webapp
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.v(TAG, String.format("x: %f, y: %f", e.getX(), e.getY()));

            double xMargin = (cameraPreview.getWidth() - imageCols) / 2;
            double yMargin = (cameraPreview.getHeight() - imageRows) / 2;

            double x = e.getX() - xMargin;
            double y = e.getY() - yMargin;

            if (x < 0 || y < 0 || imageCols < x || imageRows < y) {
                Log.v(TAG, "Tapped outside the image");
                return true;
            }

            return objectTracker.saveTrainingImage(x, y);
        }
    }
}

