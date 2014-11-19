package com.eyehelper.positionalaudiocvtesting.handler;

import android.util.Log;
import android.util.Pair;

import com.eyehelper.positionalaudiocvtesting.Utils;
import com.eyehelper.positionalaudiocvtesting.cv.SIFTWrapper;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

public class ObjectTracker {
    private static final String TAG = "ObjectTracker";

    // Object Tracker's Guess
    public Point hypothesis = new Point(0, 0);
    public Pair<Point, Point> coordinates;
    boolean hasTrainingImage = false;
    // Current Image Shown on Camera
    private Mat currentImage;
    // Training Image
    private Mat trainingImageDescriptors;
    private Mat trainingImage;

    public void saveCurrentImage(Mat image) {
        currentImage = image;
    }

    public void matchObject(final Mat image) {
        this.currentImage = image;

        if (!hasTrainingImage()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                new SIFTWrapper() {
                    @Override
                    public void callback(KeyPoint[] keyPoints, Mat descriptors) {
                        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
                        List<MatOfDMatch> matchMatrices = new ArrayList<MatOfDMatch>();
//                        Log.i("DebugDebug", descriptors.toString());                        Log.i("DebugDebug", trainingImageDescriptors.toString());

                        matcher.knnMatch(descriptors, trainingImageDescriptors, matchMatrices, 2);

                        List<Point> goodMatches = new ArrayList<Point>();
                        for (MatOfDMatch matchMatrix : matchMatrices) {
                            // TODO: make sure we are getting the correct m and n
                            List<DMatch> matches = matchMatrix.toList();

                            DMatch m = matches.get(0);
                            DMatch n = matches.get(1);

                            if (m.distance < 0.75 * n.distance) {
                                goodMatches.add(new Point(keyPoints[m.trainIdx].pt.x, keyPoints[m.trainIdx].pt.y));
                            }
                        }

                        hypothesis = meanShift(goodMatches, 10);
                        Log.i("DebugDebug Hypothesis", hypothesis.x + ", " + hypothesis.y);
                    }
                }.run(currentImage);
            }
        }).start();
    }

    public boolean hasTrainingImage() {
        return hasTrainingImage;
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

    public boolean saveTrainingImage(double x, double y) {
        Log.v(TAG, String.format("x: %f, y: %f", x, y));

        // if we already have a training currentImage, let's start a new one
        if (hasTrainingImage()) {
            trainingImageDescriptors = null;
            coordinates = null;
        }

        if (coordinates == null) {
            // this is the first corner of the rectangle
            coordinates = new Pair<Point, Point>(new Point(x, y), new Point(0, 0));
            trainingImage = currentImage.clone();
            // TODO: freeze the frame until the training currentImage selection is complete
        } else {
            // this is the second corner of the rectangle
            coordinates.second.x = x;
            coordinates.second.y = y;

            double yMin = Math.min(coordinates.first.y, coordinates.second.y);
            double xMin = Math.min(coordinates.first.x, coordinates.second.x);

            double yMax = Math.max(coordinates.first.y, coordinates.second.y);
            double xMax = Math.max(coordinates.first.x, coordinates.second.x);

            coordinates = new Pair<Point, Point>(new Point(xMin, yMin), new Point(xMax, yMax));

            new SIFTWrapper() {
                @Override
                public void callback(KeyPoint[] points, Mat descriptors) {
                    Point point;
                    double[] rows = new double[points.length];
                    int ptr = 0;
                    for (int i = 0; i < points.length; i++) {
                        point = points[i].pt;
                        if ((point.x >= coordinates.first.x
                                || point.x >= coordinates.second.x)
                                && (point.y >= coordinates.first.y
                                || point.y >= coordinates.second.y)) {
                            rows[ptr] = i;
                            ptr++;
                        }
                    }
                    trainingImageDescriptors = new Mat(descriptors, new Range(rows));
                }
            }.run(trainingImage);

            hasTrainingImage = true;
        }
        return true;
    }
}
