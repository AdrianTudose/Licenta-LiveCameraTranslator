package com.example.livecameratranslation.Boxes;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class PredictionBox {
    public Rect bb;
    public Point center;
    public char chr;
    public int index;
    public PredictionBox(Rect rect, int index) {
        bb = rect;
        center = new Point();
        center.x = bb.x + bb.width/2.0;
        center.y = bb.y + bb.height/2.0;
        this.index = index;
    }

    public void setChar(char c) {
        chr = c;
    }
}
