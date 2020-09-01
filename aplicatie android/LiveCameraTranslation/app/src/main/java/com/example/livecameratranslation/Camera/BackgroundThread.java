package com.example.livecameratranslation.Camera;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.RequiresApi;

class BackgroundThread {

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

   void start() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler((mBackgroundHandlerThread.getLooper()));
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    void stop() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Handler getHandler() {
       return mBackgroundHandler;
    }
}
