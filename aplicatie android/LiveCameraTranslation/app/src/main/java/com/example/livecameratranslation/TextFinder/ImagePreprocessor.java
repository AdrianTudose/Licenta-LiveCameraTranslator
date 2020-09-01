package com.example.livecameratranslation.TextFinder;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

class ImagePreprocessor {

    Mat process(Mat image) {

        Mat filteredImage = new Mat();
        Size s = new Size(3,3);
        Imgproc.GaussianBlur(image, filteredImage, s, 2);


        Mat binaryImage = new Mat();
        Imgproc.adaptiveThreshold(filteredImage,binaryImage,255,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,11,2);

        return binaryImage;
    }
}
