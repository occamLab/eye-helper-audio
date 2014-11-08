package com.eyehelper.positionalaudiocvtesting.cv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sihrc on 11/8/14.
 */
public abstract class SIFTWrapper {

    static
    {
        try
        {
            // Load necessary libraries.
            System.loadLibrary("opencv_java");
            System.loadLibrary("nonfree");
            System.loadLibrary("nonfree_jni");
        }
        catch( UnsatisfiedLinkError e )
        {
            System.err.println("Native code library failed to load.\n" + e);
        }
    }
    public static native float[] runSIFT(int width, int height, short[] image, int[] size);

    public void run(Mat image) {
        int width = image.width();
        int height = image.height();

        // Allocated for SIFT return dimensions
        int[] size = new int [2];

        // Get short[] array of image
        final byte[] imageInBytes = new byte[(int)(image.total()) * image.channels()]; //FIXME - long -> int conversation may be unsafe
        image.get(0, 0, imageInBytes);

        short[] imageInShorts = new short[imageInBytes.length];

        for (int i = 0; i < imageInBytes.length; i++) {
            imageInShorts[i] = (short) (imageInBytes[i] & 0xFF);
        }

        // Run SIFT
        float[] result = runSIFT(width, height, imageInShorts, size);

        // Convert Shorts back into bytes
        for (int i = 0; i < imageInShorts.length; i++) {
            imageInBytes[i] = (byte) imageInShorts[i];
        }

        // Parse the KeyPoints and Descriptors from the return float array
        KeyPoint[] keyPoints = new KeyPoint[size[0]];
        Mat descriptors = new Mat(size[0], size[1], CvType.CV_32F);
        float[] temp = new float[size[1]];
        // Unpack results
        for (int i = 0; i < size[0]; i++) {
            KeyPoint point = new KeyPoint();
            point.pt = new Point((int) result[i * (2 + size[1])], (int) result[i * (2 + size[1]) + 1]);
            keyPoints[i] = point;
            for (int j = 0; j < size[1]; j++) {
                temp[j] = result[i * (2 + size[1]) + j + 1];
            }
            descriptors.put(i,0, temp);
        }

        // Do stuff with the keypoints and descriptors
        callback(keyPoints, descriptors);
    }

    public abstract void callback(KeyPoint[] keyPoints, Mat descriptors);
}
