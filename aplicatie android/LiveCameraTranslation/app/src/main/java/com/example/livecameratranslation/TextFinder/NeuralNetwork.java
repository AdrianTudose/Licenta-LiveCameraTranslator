package com.example.livecameratranslation.TextFinder;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.widget.Toast;

import com.example.livecameratranslation.Boxes.PredictionBox;
import com.example.livecameratranslation.MainActivity;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class NeuralNetwork {

    private Interpreter clasifier;
    private static final String MODEL_PATH = "model.tflite";

    private static final float LETTER_TRESHOLD = 0.5f;

    public NeuralNetwork(MainActivity activity) {
        try {
            clasifier = new Interpreter(loadModelFile(activity));
        } catch (IOException e) {
            Toast.makeText(activity,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return  fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public ArrayList<PredictionBox> clasifyAll(ArrayList<PredictionBox> boxes, Mat grayImage) {
        ArrayList<PredictionBox> classified = new ArrayList<>();
        for(PredictionBox box : boxes) {
            char c = clasify(box.bb,grayImage);
            if(c != '_') {
                classified.add(box);
                box.setChar(c);
            }
        }
        return classified;
    }

    public char clasify(Rect rect, Mat image) {
        if(rect.x - rect.width * 0.1 >= 0) rect.x -= rect.width * 0.1;
        if(rect.y - rect.height * 0.1 >= 0) rect.y -= rect.height * 0.1;
        if(rect.x + rect.width + rect.width * 0.2 <= image.width()) rect.width += rect.width * 0.2;
        if(rect.y + rect.height + rect.height * 0.2 <= image.height()) rect.height += rect.height * 0.2;
        Mat croppedImage = new Mat(image, rect);
        Mat resizedImage = new Mat();
        Size sz = new Size(32,32);
        Imgproc.resize( croppedImage, resizedImage, sz);


        float[][][][] input = convertBitmapToFloatArr(resizedImage);

        float[][] output = new float[1][63];

        clasifier.run(input,output);

        float maxim = 0;
        int index = 0;
        for(int i = 0;i <= 61; i++)
            if(output[0][i] > maxim) {
                maxim = output[0][i];
                index = i;
            }

        if(maxim > LETTER_TRESHOLD)
            return toChar(index);
        else
            return '_';
    }

    private float[][][][] convertBitmapToFloatArr(Mat mat) {
        float[][][][] floatValues = new float[1][32][32][1];

        Size size = mat.size();
        for (int i = 0; i < size.height; i++)
            for (int j = 0; j < size.width; j++) {
                double[] data = mat.get(i, j);
                floatValues[0][i][j][0] = (float) data[0] / 255f;
            }
        return floatValues;
    }

    private char toChar(int x) {
        if(x==62) return '_';
        if(x<=9) {
            return (char)(x + 48);
        } else if(x<36) {
            return (char)(x + 55);
        } else {
            return (char)(x + 61);
        }
    }
}
