package com.example.livecameratranslation.OverlayWindow;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;

public class Tracker {

    ArrayList<Point> getNewPoints(Mat image1, Mat image2, ArrayList<Point> points) {
        MatOfPoint intial = new MatOfPoint();
        intial.fromList(points);

        Imgproc.goodFeaturesToTrack(image1, intial, 500, 0.01, 10);

        MatOfPoint2f new_point = new MatOfPoint2f();
        MatOfPoint2f currentPoints = new MatOfPoint2f();
        if(points.size()>0) {
            currentPoints.fromList(points);
            MatOfByte status = new MatOfByte();
            MatOfFloat error = new MatOfFloat();
            Video.calcOpticalFlowPyrLK(image1, image2, // 2 consecutive images
                    currentPoints, // input point position in first image
                    new_point, // output point postion in the second image
                    status,    // tracking success
                    error);
        }
        return new ArrayList<Point> (new_point.toList());
    }
}
