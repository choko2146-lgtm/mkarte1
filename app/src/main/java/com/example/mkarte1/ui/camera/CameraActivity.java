package com.example.mkarte1.ui.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mkarte1.R;
import com.example.mkarte1.util.PhotoFileUtil;

import java.io.File;

public class CameraActivity extends AppCompatActivity {
    private File tempFile;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "カメラ権限が必要です", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && tempFile != null && tempFile.exists()) {
                    Intent intent = new Intent(this, PhotoCustomerSelectActivity.class);
                    intent.putExtra("tempPath", tempFile.getAbsolutePath());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "撮影をキャンセルしました", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        findViewById(R.id.buttonTakePhoto).setOnClickListener(v -> requestCamera());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            tempFile = PhotoFileUtil.createTempPhotoFile(this);
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "カメラを起動できません", Toast.LENGTH_LONG).show();
        }
    }
}
