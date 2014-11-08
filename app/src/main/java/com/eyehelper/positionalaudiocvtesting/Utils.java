package com.eyehelper.positionalaudiocvtesting;

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


}
