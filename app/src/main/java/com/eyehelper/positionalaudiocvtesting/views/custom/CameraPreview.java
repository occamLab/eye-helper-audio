package com.eyehelper.positionalaudiocvtesting.views.custom;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.eyehelper.positionalaudiocvtesting.R;
import com.eyehelper.positionalaudiocvtesting.Utils;

import java.io.IOException;
import java.util.List;

/**
 * Created by sihrc on 11/6/14.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    boolean opened;
    // Setting the Camera Size
    List<Camera.Size> mSupportedPreviewSizes;
    Camera.Size mPreviewSize;
    private SurfaceHolder mHolder;
    private WindowManager windowManager;
    private Camera mCamera;
    private Camera.Parameters parameters;
    Camera.PreviewCallback previewCallback;

    public CameraPreview(Context context) {
        super(context);
        init();
    }

    public void init() {
        if (!checkCameraHardware(getContext())) {
            Toast.makeText(getContext(), getResources().getText(R.string.no_camera_found), Toast.LENGTH_SHORT).show();
            return;
        } else if (opened) {
            return;
        }

        mCamera = getCameraInstance();
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        opened = true;


        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Check if this device has a camera
     */
    private static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            // Camera Parameters
            parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        } catch (IOException e) {
            Log.d("Camera Preview", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        if (windowManager != null) {
            int angle;
            switch (Utils.getScreenOrientation(windowManager)) {
                default:
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    angle = 90;
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    angle = 270;
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    angle = 0;
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    angle = 180;
                    break;
            }
            mCamera.setDisplayOrientation(angle);
        }

        // start preview with new settings
        try {
            if (previewCallback != null)
                mCamera.setPreviewCallback(previewCallback);
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            Log.d("Camera Preview", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        previewCallback = callback;
    }

    /**
     * Set Correct Aspect Ratio for the Camera Preview *
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    public void onDestroy() {
        if (mCamera != null) {
            opened = false;
            mCamera.release();
            mCamera = null;
        }
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }
}