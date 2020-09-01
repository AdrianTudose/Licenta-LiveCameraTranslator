package com.example.livecameratranslation.Boxes;

import org.opencv.core.Rect;

import static java.lang.Math.min;

public class WordBox {

    public String text;
    public Rect bb;
    public boolean valid;
    public String translated_text;
    public int firstPointIndex;

    public WordBox() {
        text = "";
        valid = false;
    }

    public void extend(PredictionBox predictionBox) {
        text += predictionBox.chr;
        if(bb == null) {
            bb = predictionBox.bb;
            firstPointIndex = predictionBox.index;
        } else {
            Rect new_rect = new Rect();
            new_rect.x = min(predictionBox.bb.x,bb.x);
            new_rect.y = min(predictionBox.bb.y,bb.y);
            new_rect.width = predictionBox.bb.x + predictionBox.bb.width - new_rect.x;
            new_rect.height = predictionBox.bb.y + predictionBox.bb.height - new_rect.y;
            bb = new_rect;
            valid = true;
        }
    }

    public void newWord() {
        text+=' ';
    }
}
