package com.example.livecameratranslation.Camera;

import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.util.Comparator;

public class CompareSizeByArea implements Comparator<Size> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int compare(Size lhs, Size rhs) {
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                (long) rhs.getHeight() * rhs.getWidth());
    }
}