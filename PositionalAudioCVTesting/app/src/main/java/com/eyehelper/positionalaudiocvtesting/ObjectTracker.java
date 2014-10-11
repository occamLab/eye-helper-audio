package com.eyehelper.positionalaudiocvtesting;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ObjectTracker {

    public Pair<Point, Point> coordinates;
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
        Mat testMat = image.clone();

        final int width = image.width();
        final int height = image.height();

//        Log.i("MatrixSize", width + " width, " + height + " height");

        final byte[] imageInBytes = new byte[(int)(image.total()) * image.channels()]; //FIXME - long -> int conversation may be unsafe
        image.get(0, 0, imageInBytes);
        Log.i("DebugDebug Previous ", Arrays.toString(imageInBytes));

        final int[] outputImgArray = new int[width * height];

        SIFTImpl.runSIFT(width, height, imageInBytes, outputImgArray);
//        Log.i("DebugDebug After", Arrays.toString(outputImgArray));
        Log.i("DebugDebug After ", Arrays.toString(imageInBytes));


//        //Bitmap contains circles drawn on for keypoints
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bmp.setPixels(outputImgArray, 0/* offset */, width /* stride */, 0, 0, width, height);

//        Mat testMat = new Mat(width, height, CvType.CV_32S);
//        testMat.convertTo(testMat, CvType.CV_32S);
        testMat.put(0, 0, imageInBytes);
        testMat.convertTo(testMat, CvType.CV_8UC4);
        Log.i("DebugDebug dump", testMat.dump());
        return testMat.t();

//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                SIFTImpl.runSIFT(width, height, imageInBytes, outputImgArray);
//                Log.i("DebugDebug", Arrays.toString(outputImgArray));
//
//                //Bitmap contains circles drawn on for keypoints
//                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                bmp.setPixels(outputImgArray, 0/* offset */, width /* stride */, 0, 0, width, height);
//                return null;
//            }
//        }.execute();



//        if (!hasTrainingImage()) {
//            return;
//        }
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
//
//        meanShift(goodMatches, 10);
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

            //  Finding the radius:
            List<Double> norm_weights = new ArrayList<Double>();
            for (int i = 0; i < x_weights.size(); i++) {
                norm_weights.add(Utils.norm(new Point(x_weights.get(i), y_weights.get(i))));
            }

            double avg_weight = Utils.sum(norm_weights) / norm_weights.size();

            // TODO: get standard deviation using java
            // double std_weight = Math.std(norm_weights);

            //  Threshold based on standard deviations (to account for different kp density scenarios)
            threshold = avg_weight; // - .25*std_weight
            List<Double> inliers = new ArrayList<Double>();

            //  Radius corresponds to the farthest-away keypoints are in the threshold from center of mass (x,y)
            for (int index = 0; index < norm_weights.size(); index++) {
                if (norm_weights.get(index) > threshold) {
                    Point inliearCoordinates = new Point(keypoints.get(index).x - x, keypoints.get(index).y - y);
                    inliers.add(Utils.norm(inliearCoordinates));
                }
            }

            // TODO: why do we need radius?
            double radius = Collections.max(inliers);
        }
        return hypothesis;
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
