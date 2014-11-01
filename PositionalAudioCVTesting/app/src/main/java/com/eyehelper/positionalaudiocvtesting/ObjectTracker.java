package com.eyehelper.positionalaudiocvtesting;

import android.util.Log;
import android.util.Pair;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectTracker {

    public Pair<Point, Point> coordinates;
    public Point hypothesis;
    private Mat trainingImageDescriptors;
    private static final String TAG = "OCVSample::ObjectTracker";
    private Mat image;
    private Mat trainingImage;

    private void setTrainingArea(Pair<Point, Point> coordinates) {
        MatOfKeyPoint trainingImageKeypoints = detectKeypoints(trainingImage);
        this.trainingImageDescriptors = computeDescriptors(trainingImage, trainingImageKeypoints);
    }

    public Mat matchObject(Mat image) {
        this.image = image;

        if (!hasTrainingImage()) {
            return image;
        }
        Mat testMat = image.clone();

        final int width = image.width();
        final int height = image.height();
        int[] size = new int [2];

        final byte[] imageInBytes = new byte[(int)(image.total()) * image.channels()]; //FIXME - long -> int conversation may be unsafe
        image.get(0, 0, imageInBytes);

        short[] imageInShorts = new short[imageInBytes.length];

        for (int i = 0; i < imageInBytes.length; i++) {
            imageInShorts[i] = (short) (imageInBytes[i] & 0xFF);
        }

        float[] result = SIFTImpl.runSIFT(width, height, imageInShorts, size);

        for (int i = 0; i < imageInShorts.length; i++) {
            imageInBytes[i] = (byte) imageInShorts[i];
        }

        int[] keypoints_x = new int[size[0]];
        int[] keypoints_y = new int[size[0]];
        float[][] descriptors = new float[size[0]][size[1]];

        // Unpack results
        for (int i = 0; i < size[0]; i++) {
            keypoints_x[i] = (int) result[i * (2 + size[1])];
            keypoints_y[i] = (int) result[i * (2 + size[1]) + 1];
            for (int j = 0; j < size[1]; j++) {
                Log.i("DebugDebug", result[i*(2+size[1]) + j +1] + " float");
                descriptors[i][j] = result[i * (2 + size[1]) + j + 1];
            }
        }

        Log.i("DebugDebug Unpacked X", Arrays.toString(keypoints_x));
        Log.i("DebugDebug Unpacked Y", Arrays.toString(keypoints_y));
        Log.i("DebugDebug Unpacked Descriptors", Arrays.toString(descriptors));

        // Yay it works now!
        testMat.put(0, 0, imageInBytes);
        testMat.convertTo(testMat, 0);
        return testMat;


//
//        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
//        MatOfKeyPoint keypoints = detectKeypoints(image);
//        KeyPoint[] keypointsArray = keypoints.toArray();
//        Mat descriptors = computeDescriptors(image, keypoints);
//        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
//        List<MatOfDMatch> matchMatrices = new ArrayList<MatOfDMatch>();
//        matcher.knnMatch(trainingImageDescriptors, descriptors, matchMatrices, 2);
//
//        // Filter out the outliers
//        List<Point> goodMatches = new ArrayList<Point>();
//        for (MatOfDMatch matchMatrix : matchMatrices) {
//            // TODO: make sure we are getting the correct m and n
//            List<DMatch> matches = matchMatrix.toList();
//
//            DMatch m = matches.get(0);
//            DMatch n = matches.get(1);
//
//            if (m.distance < 0.75 * n.distance) {
//                double x = keypointsArray[m.trainIdx].pt.x;
//                double y = keypointsArray[m.trainIdx].pt.y;
//                goodMatches.add(new Point(x, y));
//            }
//        }

//        return image;
//
//        this.hypothesis = meanShift(goodMatches, 10);
    }

    private Point meanShift(List<Point> keypoints, double threshold) {
        Point hypothesis = Utils.getCenter(coordinates.first, coordinates.second);

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
            double x = Utils.sum(weighted_x) / Utils.sum(x_weights);
            double y = Utils.sum(weighted_y) / Utils.sum(y_weights);

            // update hypothesis
            hypothesis = new Point(x, y);

            // difference between the current and last guess
            diff = Math.sqrt(Math.pow((last_guess.x - x), 2) + Math.pow((last_guess.y - y), 2));
        }
        return hypothesis;
    }

    private MatOfKeyPoint detectKeypoints(Mat img) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(img, keypoints);
        return keypoints;
    }

    private Mat computeDescriptors(Mat img, MatOfKeyPoint keypoints) {
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        Mat descriptors = new Mat();
        descriptor.compute(img, keypoints, descriptors);
        return descriptors;
    }

    private boolean hasTrainingImage() {
        return trainingImageDescriptors != null;
    }

    public boolean onSingleTapUp(double x, double y) {
        Log.v(TAG, String.format("x: %f, y: %f", x, y));

        // if we already have a training image, let's start a new one
        if (hasTrainingImage()) {
            trainingImageDescriptors = null;
            coordinates = null;
        }

        if (coordinates == null) {
            // this is the first corner of the rectangle
            coordinates = new Pair<Point, Point>(new Point(x, y), new Point(0,0));
            saveImage();
            // TODO: freeze the frame until the training image selection is complete
        } else {
            // this is the second corner of the rectangle
            coordinates.second.x = x;
            coordinates.second.y = y;

            double yMin = Math.min(coordinates.first.y, coordinates.second.y);
            double xMin = Math.min(coordinates.first.x, coordinates.second.x);

            double yMax = Math.max(coordinates.first.y, coordinates.second.y);
            double xMax = Math.max(coordinates.first.x, coordinates.second.x);

            coordinates = new Pair<Point, Point>(new Point(xMin, yMin), new Point(xMax, yMax));

            setTrainingArea(coordinates);
        }
        return true;
    }


    private void saveImage() {
        this.trainingImage = this.image;
    }
}
