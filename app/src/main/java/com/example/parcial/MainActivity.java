package com.example.parcial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 101;
    private static final int PERMISSION_REQUEST = 200;

    private ImageView imageView;
    private Spinner spinnerFilters;

    private Bitmap originalBitmap;
    private int selectedFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        spinnerFilters = findViewById(R.id.spinnerFilters);

        checkPermissions();

        // Botones
        findViewById(R.id.btnCamera).setOnClickListener(v -> openCamera());
        findViewById(R.id.btnGallery).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnCamera2).setOnClickListener(v -> openLiveCamera());

        // Spinner
        String[] options = {"Original", "Gris", "Sepia", "Blur", "Edge Detection"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilters.setAdapter(adapter);

        spinnerFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedFilter = position;
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void checkPermissions() {

        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean allGranted = true;

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    private void openLiveCamera() {

        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = null;

            if (requestCode == CAMERA_REQUEST) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            else if (requestCode == GALLERY_REQUEST) {

                Uri imageUri = data.getData();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {

                originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(originalBitmap);

                applyFilter();
            }
        }
    }

    private void applyFilter() {

        if (originalBitmap == null) return;

        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        processWithOpenCV(pixels, width, height, selectedFilter);

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        imageView.setImageBitmap(bitmap);
    }

    // Filtro nativo
    private native void processWithOpenCV(int[] pixels, int width, int height, int filterType);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }
}