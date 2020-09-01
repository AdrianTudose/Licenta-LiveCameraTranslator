package com.example.livecameratranslation.Camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.livecameratranslation.OverlayWindow.Overlay;
import com.example.livecameratranslation.TextFinder.FrameAnalyzer;
import com.example.livecameratranslation.MainActivity;
import com.example.livecameratranslation.ErrorAlertDialog;
import com.example.livecameratranslation.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera {
    TextureView mTextureView;
    MainActivity activity;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    CameraDevice mCameraDevice;
    private CameraDeviceStateCallback mCameraDeviceStateCallback;
    private String mCameraId;
    Size mPreviewSize;
    BackgroundThread backgroundThread;
    Size mImageSize;
    protected CameraCaptureSession mPreviewCaptureSession;
    protected ImageReader imageReader;

    protected static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public Camera(MainActivity activity, FrameAnalyzer frameAnalyzer, Overlay overlay) {
        this.activity = activity;
        mTextureView = (TextureView) activity.findViewById(R.id.preview_camera);
        mSurfaceTextureListener = new TextureViewSurfaceTextureListener(this,frameAnalyzer,overlay);
        mCameraDeviceStateCallback = new CameraDeviceStateCallback(this,activity.getApplicationContext(),activity.getWindowManager());
        backgroundThread = new BackgroundThread();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resume() {
        backgroundThread.start();
        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener((mSurfaceTextureListener));
        }
    }

    public void pause() {
        closeCamera();
        backgroundThread.stop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraId: cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
                int totalRotation = sensorToDeviceRotation(cameraCharacteristics,deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedWidth,rotatedHeight);
                mCameraId = cameraId;

                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),rotatedWidth,rotatedHeight);
                imageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void connectCamera() {
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Objects.requireNonNull(cameraManager).openCamera(mCameraId,mCameraDeviceStateCallback,backgroundThread.getHandler());
                } else {
                    if(activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        DialogFragment newFragment = new ErrorAlertDialog("Video app requires camera access.");
                        newFragment.show(activity.getSupportFragmentManager(), "version_error");
                    }
                    activity.requestPermissions(new String[] {Manifest.permission.CAMERA}, MainActivity.REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                Objects.requireNonNull(cameraManager).openCamera(mCameraId,mCameraDeviceStateCallback,backgroundThread.getHandler());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeCamera() {
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(cameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation +deviceOrientation + 360) % 360;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : choices) {
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else  {
            return choices[0];
        }
    }

}
