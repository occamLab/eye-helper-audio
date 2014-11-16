package com.eyehelper.positionalaudiocvtesting.views;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.eyehelper.positionalaudiocvtesting.R;
import com.eyehelper.positionalaudiocvtesting.Utils;
import com.eyehelper.positionalaudiocvtesting.handler.AudioHandler;
import com.eyehelper.positionalaudiocvtesting.handler.ObjectTracker;
import com.eyehelper.positionalaudiocvtesting.handler.OrientationHandler;
import com.eyehelper.positionalaudiocvtesting.views.custom.CameraPreview;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class CameraActivity extends Activity {
    private static final String TAG = "OCVSample::Activity";

    //Camera View
    @InjectView(R.id.camera_view)
    CameraPreview cameraPreview;

    private double imageRows;
    private double imageCols;
    private int frames;
    private static final int FPS = 30;

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
                    cameraPreview.init();
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

    public CameraActivity() {
    }


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

        cameraPreview.setWindowManager(getWindowManager());
        cameraPreview.init();

        final GestureDetector gestureDetector = new GestureDetector(this, new TapGestureListener());
        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        // Get Preview Frame Callbacks
        cameraPreview.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                if (bytes == null || bytes.length == 0) {
                    return;
                }

                Camera.Size size = camera.getParameters().getPreviewSize();
                Mat greyImage = new Mat(size.height, size.width, CvType.CV_8UC1);
                greyImage.put(0, 0, bytes);

                if (!objectTracker.hasTrainingImage()) {
                    imageCols = greyImage.cols();
                    imageRows = greyImage.rows();
                    objectTracker.saveCurrentImage(greyImage);
                }

                if (frames == FPS) {
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
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //When the app is resumed, restart the camera asynchronously
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, loaderCallback);
        orientationHandler.resume();
//        positionalAudio.startSoundThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        //When the app is paused, stop the camera and pause the music
        orientationHandler.pause();
        if (cameraPreview != null) {
            cameraPreview.onDestroy();
        }
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

