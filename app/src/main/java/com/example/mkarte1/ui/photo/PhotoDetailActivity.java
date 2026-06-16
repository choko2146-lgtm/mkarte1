package com.example.mkarte1.ui.photo;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.PhotoImageLoader;
import com.example.mkarte1.util.PhotoFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PHOTO_ID = "photoId";

    private PhotoRepository repository;
    private Photo photo;
    private final List<Photo> photoList = new ArrayList<>();
    private int currentIndex = -1;
    private ImageView imagePhoto;
    private TextView textPhoto;
    private EditText memo;
    private Button buttonPreviousPhoto;
    private Button buttonNextPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        repository = new PhotoRepository(this);
        imagePhoto = findViewById(R.id.imagePhoto);
        textPhoto = findViewById(R.id.textPhoto);
        memo = findViewById(R.id.editMemo);
        buttonPreviousPhoto = findViewById(R.id.buttonPreviousPhoto);
        buttonNextPhoto = findViewById(R.id.buttonNextPhoto);

        long photoId = getIntent().getLongExtra(EXTRA_PHOTO_ID, -1);
        if (photoId <= 0) {
            Toast.makeText(this, "写真を開けません", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadPhotoAndCustomerPhotos(photoId);

        imagePhoto.setOnClickListener(v -> openPhotoPreview());
        buttonPreviousPhoto.setOnClickListener(v -> showPreviousPhoto());
        buttonNextPhoto.setOnClickListener(v -> showNextPhoto());
        findViewById(R.id.buttonSaveMemo).setOnClickListener(v -> saveMemo());
        findViewById(R.id.buttonDeletePhoto).setOnClickListener(v -> confirmDelete());
    }

    private void loadPhotoAndCustomerPhotos(long photoId) {
        repository.get(photoId, value -> {
            if (value == null) {
                Toast.makeText(this, "写真が見つかりません", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            repository.listForCustomer(value.customerId, photos -> {
                photoList.clear();
                if (photos != null) {
                    photoList.addAll(photos);
                }
                currentIndex = findPhotoIndex(photoId);
                if (currentIndex == -1) {
                    photoList.add(value);
                    currentIndex = photoList.size() - 1;
                }
                showPhoto(photoList.get(currentIndex));
            });
        });
    }

    private int findPhotoIndex(long photoId) {
        for (int i = 0; i < photoList.size(); i++) {
            if (photoList.get(i).id == photoId) {
                return i;
            }
        }
        return -1;
    }

    private void showPhoto(Photo value) {
        photo = value;
        bindPhotoImage(imagePhoto, photo.uri);
        textPhoto.setText(photo.customerName + "\n"
                + DateUtil.displayYmd(photo.takenDate) + "\n"
                + photo.fileName);
        memo.setText(photo.memo);
        updateArrowVisibility();
    }

    private void showPreviousPhoto() {
        if (currentIndex > 0) {
            currentIndex--;
            showPhoto(photoList.get(currentIndex));
        }
    }

    private void showNextPhoto() {
        if (currentIndex < photoList.size() - 1) {
            currentIndex++;
            showPhoto(photoList.get(currentIndex));
        }
    }

    private void updateArrowVisibility() {
        if (photoList.size() <= 1) {
            buttonPreviousPhoto.setVisibility(View.GONE);
            buttonNextPhoto.setVisibility(View.GONE);
            return;
        }
        buttonPreviousPhoto.setVisibility(currentIndex > 0 ? View.VISIBLE : View.INVISIBLE);
        buttonNextPhoto.setVisibility(currentIndex < photoList.size() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindPhotoImage(ImageView imagePhoto, String uriText) {
        if (uriText == null || uriText.trim().isEmpty()) {
            imagePhoto.setImageDrawable(null);
            return;
        }

        try {
            Uri uri = Uri.parse(uriText);
            if ("file".equals(uri.getScheme()) && uri.getPath() != null) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    imagePhoto.post(() -> {
                        if (!PhotoImageLoader.loadFileInto(imagePhoto, file)) {
                            imagePhoto.setImageURI(Uri.fromFile(file));
                        }
                    });
                    return;
                }
            }
            imagePhoto.setImageURI(uri);
        } catch (Exception ignored) {
            imagePhoto.setImageDrawable(null);
        }
    }

    private void saveMemo() {
        if (photo == null) {
            return;
        }
        photo.memo = memo.getText().toString();
        repository.update(photo, () -> Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show());
    }

    private void openPhotoPreview() {
        if (photo == null || photo.id <= 0) {
            Toast.makeText(this, "写真を開けません", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(PhotoPreviewActivity.EXTRA_PHOTO_ID, photo.id);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (photo == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("写真を削除しますか？")
                .setMessage("削除した写真は元に戻せません。\n端末内の画像も削除されます。")
                .setPositiveButton("削除", (dialog, which) -> {
                    PhotoFileUtil.deleteQuietly(photo.uri);
                    repository.delete(photo, () -> {
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }
}
