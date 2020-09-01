package com.example.livecameratranslation.TextFinder;

import com.example.livecameratranslation.Boxes.PredictionBox;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;

public class CharacterDetector {

    public ArrayList<PredictionBox> detectLetters(Mat binaryImage, double minArea, double maxArea, double minBoxProportion, double maxBoxProportion) {
        ArrayList<PredictionBox> predictionBoxes = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binaryImage,contours,hierarchy,RETR_TREE, CHAIN_APPROX_SIMPLE);

        int index = 0;
        for(MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if(minArea <= area && area <= maxArea) {
                Rect rect = Imgproc.boundingRect(contour);
                if(minBoxProportion <= (double)rect.width/rect.height && (double)rect.width/rect.height <= maxBoxProportion){
                    predictionBoxes.add(new PredictionBox(rect,index));
                    index ++;
                }
            }
        }
        return predictionBoxes;
    }
}
