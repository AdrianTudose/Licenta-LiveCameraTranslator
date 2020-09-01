package com.example.livecameratranslation.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraDeviceStateCallback extends CameraDevice.StateCallback {

    private final WindowManager windowManager;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    BackgroundThread backgroundThread;
    private Camera camera;
    private Context context;
    private CameraCaptureSession cameraCaptureSession;

    CameraDeviceStateCallback(Camera camera, Context context, WindowManager windowManager) {
        this.camera = camera;
        this.context = context;
        this.windowManager = windowManager;
    }

    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        this.camera.mCameraDevice = camera;
        startPreview();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
        camera.close();
        this.camera.mCameraDevice = null;
    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
        camera.close();
        this.camera.mCameraDevice = null;
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = camera.mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(camera.mPreviewSize.getWidth(),camera.mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = camera.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            camera.mCameraDevice.createCaptureSession(Arrays.asList(previewSurface,camera.imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            camera.mPreviewCaptureSession = session;
                            try {
                                session.setRepeatingRequest(mCaptureRequestBuilder.build(),null,camera.backgroundThread.getHandler());
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(context,
                                    "Unable to setup camera preview",Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
