package com.example.mkarte1.ui.photo;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.PhotoFileUtil;

public class PhotoDetailActivity extends AppCompatActivity {
    private PhotoRepository repository;
    private Photo photo;
    private EditText memo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        repository = new PhotoRepository(this);
        memo = findViewById(R.id.editMemo);

        long photoId = getIntent().getLongExtra("photoId", -1);
        repository.get(photoId, value -> {
            photo = value;
            if (photo == null) {
                finish();
                return;
            }
            ((ImageView) findViewById(R.id.imagePhoto)).setImageURI(Uri.parse(photo.uri));
            ((TextView) findViewById(R.id.textPhoto)).setText(
                    photo.customerName + "\n" + DateUtil.displayYmd(photo.takenDate) + "\n" + photo.fileName);
            memo.setText(photo.memo);
        });

        findViewById(R.id.buttonSaveMemo).setOnClickListener(v -> saveMemo());
        findViewById(R.id.buttonDeletePhoto).setOnClickListener(v -> confirmDelete());
    }

    private void saveMemo() {
        if (photo == null) {
            return;
        }
        photo.memo = memo.getText().toString();
        repository.update(photo, () -> Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show());
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
