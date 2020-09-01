package com.example.livecameratranslation.OverlayWindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.livecameratranslation.Boxes.PredictionBox;
import com.example.livecameratranslation.Boxes.WordBox;

import org.opencv.core.Point;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class Overlay extends View{

    private final float REDUCE_IMAGE_BY_FACTOR = 16;

    private Paint textPaint;
    private Paint backgroundPaint;
    private ArrayList<PredictionBox> boundingPredictionBoxes;
    private ArrayList<WordBox> predictions;
    private Translator translator;
    private Mat currentFrame;
    private Tracker tracker;
    private Mat oldFrame;
    private boolean first_time;
    private ArrayList<Point> visiblePoints;
    private ArrayList<Point> invisiblePoints;
    private ArrayList<Point> invisiblePointsInitial;


    public Overlay(Context context, Translator translator) {
        super(context);

        boundingPredictionBoxes = new ArrayList<>();
        predictions = new ArrayList<>();
        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#f5f5f5"));
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#545d5c"));

        this.translator = translator;
        tracker = new Tracker();
        first_time = true;
        visiblePoints = new ArrayList<>();
        invisiblePoints = new ArrayList<>();
        invisiblePointsInitial = new ArrayList<>();
    }

    public void display(ArrayList<WordBox> predictions) {
        float x,y;
        this.predictions = new ArrayList<>();
        this.visiblePoints = new ArrayList<>();
        for (WordBox box : predictions) {
            if(box.valid)  {
                this.predictions.add(box);
                translator.translate(box);
                x = box.bb.x/REDUCE_IMAGE_BY_FACTOR + (float) (invisiblePoints.get(box.firstPointIndex).x - invisiblePointsInitial.get(box.firstPointIndex).x);
                y = box.bb.y/REDUCE_IMAGE_BY_FACTOR + (float) (invisiblePoints.get(box.firstPointIndex).y - invisiblePointsInitial.get(box.firstPointIndex).y);
                visiblePoints.add(new Point(x,y));
            }
        }
    }

    public void setPointsToFollow(ArrayList<PredictionBox> boxes) {
        this.invisiblePoints = new ArrayList<>();
        this.invisiblePointsInitial = new ArrayList<>();
        for (PredictionBox box : boxes) {
            invisiblePoints.add(new Point(box.bb.x/REDUCE_IMAGE_BY_FACTOR,box.bb.y/REDUCE_IMAGE_BY_FACTOR));
            invisiblePointsInitial.add(new Point(box.bb.x/REDUCE_IMAGE_BY_FACTOR,box.bb.y/REDUCE_IMAGE_BY_FACTOR));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(currentFrame != null) {
            if(first_time){
                oldFrame = currentFrame.clone();
                first_time =false;
            }
            visiblePoints = tracker.getNewPoints(oldFrame, currentFrame, visiblePoints);
            invisiblePoints = tracker.getNewPoints(oldFrame, currentFrame, invisiblePoints);
            if(predictions.size() == visiblePoints.size())
            for (int i = 0; i < visiblePoints.size(); i++) {
                predictions.get(i).bb.x = (int) visiblePoints.get(i).x;
                predictions.get(i).bb.y = (int) visiblePoints.get(i).y;
            }
        }

        for(WordBox box : predictions) {
            Paint letterPaint = new Paint(textPaint);
            letterPaint.setTextSize((int)(box.bb.height*0.8));
            letterPaint.setStrokeWidth(3);
            Paint boxPaint = new Paint(backgroundPaint);
            boxPaint.setStrokeWidth(3);
            canvas.drawRect(box.bb.x*REDUCE_IMAGE_BY_FACTOR,
                    box.bb.y*REDUCE_IMAGE_BY_FACTOR,
                    box.bb.x*REDUCE_IMAGE_BY_FACTOR+box.bb.width,
                    box.bb.y*REDUCE_IMAGE_BY_FACTOR+box.bb.height,
                    boxPaint);
            canvas.drawText(String.valueOf(box.translated_text),box.bb.x*REDUCE_IMAGE_BY_FACTOR,box.bb.y*REDUCE_IMAGE_BY_FACTOR+box.bb.height * 0.9f,letterPaint);
        }
        if(currentFrame!= null)
            oldFrame = currentFrame.clone();
        postInvalidate();
    }

    public void setFrame(Bitmap frame) {
        Mat mat = new Mat (frame.getWidth(), frame.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(frame, mat);

        Mat resizedImage = new Mat();
        Size sz = new Size(mat.width()/REDUCE_IMAGE_BY_FACTOR,mat.height()/REDUCE_IMAGE_BY_FACTOR);
        Imgproc.resize( mat, resizedImage, sz);

        currentFrame = new Mat (resizedImage.width(), resizedImage.height(), CvType.CV_8UC1);
        Imgproc.cvtColor(resizedImage, currentFrame, Imgproc.COLOR_BGR2GRAY);
    }
}
