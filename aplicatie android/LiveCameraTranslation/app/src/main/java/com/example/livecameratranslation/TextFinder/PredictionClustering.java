package com.example.livecameratranslation.TextFinder;

import com.example.livecameratranslation.Boxes.PredictionBox;
import com.example.livecameratranslation.Boxes.WordBox;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Math.atan;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

public class PredictionClustering {

    public ArrayList<WordBox> cluster(ArrayList<PredictionBox> predictionBoxes, double min_angle, double max_angle, double letters_distance_treshold, double word_distance_treshold) {

        Collections.sort(predictionBoxes, new BoxComparator());

        int n = predictionBoxes.size();
        float[][] distances = new float[n][n];
        float dist;
        ArrayList<Integer> eliminate = new ArrayList<>();

        for(int i=0;i<n;i++) {
            distances[i][i] = Float.POSITIVE_INFINITY;
            for(int j=0;j<i;j++) {
                dist = (float) sqrt(pow(predictionBoxes.get(i).center.x - predictionBoxes.get(j).center.x,2) + pow(predictionBoxes.get(i).center.y - predictionBoxes.get(j).center.y,2));
                distances[i][j] = distances[j][i] = dist;
                if(dist - (predictionBoxes.get(i).bb.width + predictionBoxes.get(j).bb.width) <= 0) {
                    float overlappingArea = calculateOverlappingArea(predictionBoxes.get(i).bb, predictionBoxes.get(j).bb);
                    int area_i = predictionBoxes.get(i).bb.height * predictionBoxes.get(i).bb.width;
                    int area_j = predictionBoxes.get(j).bb.height * predictionBoxes.get(j).bb.width;

                    if(overlappingArea == area_i)
                        eliminate.add(i);
                    else if(overlappingArea == area_j)
                        eliminate.add(j);
                }
            }
        }

        for(Integer index : eliminate)
            for(int i=0;i<n;i++)
                distances[i][index] = distances[index][i] = Float.POSITIVE_INFINITY;

        boolean[] visited = new boolean[n];

        ArrayList<WordBox> groups = new ArrayList<>();
        double check_angle,x,y;

        for(int i = 0;i < n;i++) {
            int j = i;
            double angle = 0;
            WordBox group = new WordBox();
            groups.add(group);
            while (visited[j] == false) {
                group.extend(predictionBoxes.get(j));
                visited[j] = true;
                int k = argMin(distances[j]);
                if(k!=-1) dist = distances[j][k];
                else dist = Float.POSITIVE_INFINITY;
                while (k != -1 && dist < word_distance_treshold * predictionBoxes.get(k).bb.height) {

                    y = predictionBoxes.get(k).center.y - predictionBoxes.get(j).center.y;
                    x = predictionBoxes.get(k).center.x - predictionBoxes.get(j).center.x;
                    if (x == 0) x = 0.00000000000000001;
                    check_angle = toDegrees(atan(y / x)) - angle;

                    if (visited[k] == false &&
                            min_angle <= check_angle && check_angle <= max_angle &&
                            predictionBoxes.get(j).bb.height / 2 <= predictionBoxes.get(k).bb.height &&
                            predictionBoxes.get(k).bb.height <= predictionBoxes.get(j).bb.height * 2) {
                        if (dist >= letters_distance_treshold * predictionBoxes.get(k).bb.height)
                            group.newWord();

                        distances[j][k] = distances[k][j] = Float.POSITIVE_INFINITY;
                        angle = check_angle;
                        j = k;

                        break;
                    } else {
                        distances[j][k] = distances[k][j] = Float.POSITIVE_INFINITY;
                        k = argMin(distances[j]);
                        if(k!=-1) dist = distances[j][k];
                        else dist = Float.POSITIVE_INFINITY;
                    }
                }
                if(k == -1 || dist >= word_distance_treshold * predictionBoxes.get(k).bb.height)
                    break;
            }
        }

        return groups;
    }

    private float calculateOverlappingArea(Rect a, Rect b) {
        int a_xmax = a.x + a.width;
        int a_xmin = a.x;
        int a_ymax = a.y + a.height;
        int a_ymin = a.y;

        int b_xmax = b.x + b.width;
        int b_xmin = b.x;
        int b_ymax = b.y + b.height;
        int b_ymin = b.y;

        int dx = min(a_xmax, b_xmax) - max(a_xmin, b_xmin);
        int dy = min(a_ymax, b_ymax) - max(a_ymin, b_ymin);
        if ((dx>=0) && (dy>=0))
            return dx*dy;
        else
            return 0;
    }

    public static int argMin(float[] a) {
        float v = Integer.MAX_VALUE;
        int ind = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < v) {
                v = a[i];
                ind = i;
            }
        }
        return ind;
    }


    public static class BoxComparator implements Comparator {

        public int compare(Object obj1, Object obj2) {
            PredictionBox predictionBox1 = (PredictionBox)obj1;
            PredictionBox predictionBox2 = (PredictionBox)obj2;
            return Double.compare(predictionBox1.center.x, predictionBox2.center.x);
        }
    }

}
