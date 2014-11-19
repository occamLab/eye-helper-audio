package com.eyehelper.positionalaudiocvtesting;

import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.opencv.core.Point;

import java.util.List;

public class Utils {
    public static double norm(Point point) {
        return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
    }

    public static double sum(List<Double> list) {
        double sum = 0;
        for (double number : list) {
            sum += number;
        }
        return sum;
    }

    public static Point getCenter(Point first, Point second) {
        double x = (first.x + second.x) / 2;
        double y = (first.y + second.y) / 2;
        return new Point(x, y);
    }

    public static int getScreenOrientation(WindowManager manager) {
        int rotation = manager.getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        int original;

        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            original = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            original = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        // if the device's natural orientation is portrait:

        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = original;
                break;
            case Surface.ROTATION_90:
                orientation = 1 - original;
                break;
            case Surface.ROTATION_180:
                orientation = 8 + original;
                break;
            case Surface.ROTATION_270:
                orientation = 9 - original;
                break;
            default:
                Log.e("Orientation Handler", "Unknown screen orientation. Defaulting to " +
                        "portrait.");
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
        }

        return orientation;
    }

    static public void decodeYUV(int[] outGrey,  byte[] fg) {
        for (int pixPtr = 0; pixPtr < outGrey.length; pixPtr++) {
            outGrey[pixPtr] = fg[pixPtr] & 255;
        }
    }
}
