package com.example.livecameratranslation.Camera;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.TextureView;

import androidx.annotation.RequiresApi;

import com.example.livecameratranslation.OverlayWindow.Overlay;
import com.example.livecameratranslation.TextFinder.FrameAnalyzer;

public class TextureViewSurfaceTextureListener implements TextureView.SurfaceTextureListener {

    Camera cam;
    FrameAnalyzer frameAnalyzer;
    Overlay overlay;

    public TextureViewSurfaceTextureListener(Camera activeCamera, FrameAnalyzer frameAnalyzer, Overlay overlay) {
        cam = activeCamera;
        this.frameAnalyzer = frameAnalyzer;
        this.overlay = overlay;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        cam.setupCamera(width,height);
        cam.connectCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Bitmap frame = Bitmap.createBitmap(cam.mTextureView.getWidth(), cam.mTextureView.getHeight(), Bitmap.Config.ARGB_8888);
        cam.mTextureView.getBitmap(frame);

        frameAnalyzer.setFrame(frame);
        overlay.setFrame(frame);
    }
}
