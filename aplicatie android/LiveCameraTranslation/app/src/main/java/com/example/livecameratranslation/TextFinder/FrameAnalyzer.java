package com.example.livecameratranslation.TextFinder;

import android.graphics.Bitmap;

import com.example.livecameratranslation.Boxes.PredictionBox;
import com.example.livecameratranslation.Boxes.WordBox;
import com.example.livecameratranslation.MainActivity;
import com.example.livecameratranslation.OverlayWindow.Overlay;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


public class FrameAnalyzer extends Thread {

    private Mat current_image;
    private Overlay displayResults;

    private ImagePreprocessor preprocessor;
    private CharacterDetector detector;
    private PredictionClustering clusteringAlg;
    private NeuralNetwork classifier;
    private boolean running;

    public FrameAnalyzer(MainActivity activity, Overlay display) {
        displayResults = display;
        preprocessor = new ImagePreprocessor();
        detector = new CharacterDetector();
        clusteringAlg = new PredictionClustering();
        classifier = new NeuralNetwork(activity);
        running = true;
    }

    @Override
    public void run() {
        super.run();
        while(true) {
            if(current_image != null && running)
                predict(current_image);
        }
    }

    public void setFrame(Bitmap frame) {
        current_image = new Mat (frame.getWidth(), frame.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(frame, current_image);
    }

    private void predict(Mat image) {

        Mat grayImage = new Mat (image.width(), image.height(), CvType.CV_8UC1);
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        Bitmap bmp = Bitmap.createBitmap(grayImage.cols(), grayImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayImage, bmp);

        Mat processedImage = preprocessor.process(grayImage);

        ArrayList<PredictionBox> predictionBoxes = detector.detectLetters(processedImage,
                100,
                image.width() * image.height() * 0.5,
                0.2,
                2);

        displayResults.setPointsToFollow(predictionBoxes);

        ArrayList<PredictionBox> classifiedBoxes = classifier.clasifyAll(predictionBoxes,grayImage);

        ArrayList<WordBox> words = clusteringAlg.cluster(classifiedBoxes,
                -20,
                20,
                0.8,
                2);

        displayResults.display(words);
    }


    public void pauseAnalizer() {
        running = false;
    }

    public void resumeAnalizer() {
        running = true;
    }

}
