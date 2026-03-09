package com.example.parcial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "CameraFix";
    private static final int PERMISSION_ID = 1;

    private CameraBridgeViewBase mOpenCvCameraView;
    private CheckBox checkBoxProcessing;
    private boolean processingEnabled = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV cargado correctamente");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        mOpenCvCameraView = findViewById(R.id.camera_surface);
        checkBoxProcessing = findViewById(R.id.checkBoxProcessing);

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);

            // CONFIGURACIÓN DE COMPATIBILIDAD
            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
            mOpenCvCameraView.setZOrderMediaOverlay(true);
        }

        checkBoxProcessing.setOnCheckedChangeListener((v, isChecked) -> processingEnabled = isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mOpenCvCameraView.setCameraPermissionGranted();
            if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
            } else {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_ID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mOpenCvCameraView.setCameraPermissionGranted();
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Toast.makeText(this, "Permiso necesario", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();

        if (processingEnabled) {
            Mat gray = new Mat();
            Mat edges = new Mat();
            
            // 1. Convertir a escala de grises
            Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
            
            // 2. Detectar bordes con Canny
            Imgproc.Canny(gray, edges, 80, 100);
            
            // 3. Convertir bordes de vuelta a RGBA para mostrar
            Imgproc.cvtColor(edges, rgba, Imgproc.COLOR_GRAY2RGBA);
            
            // Liberar memoria temporal
            gray.release();
            edges.release();
        }
        
        return rgba;
    }
}
