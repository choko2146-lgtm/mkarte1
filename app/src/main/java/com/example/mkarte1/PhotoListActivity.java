package com.example.mkarte1;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.repository.PhotoRepository;

public class PhotoListActivity extends AppCompatActivity {
    private PhotoRepository photoRepository;
    private RecyclerView recyclerPhotos;
    private TextView textNoPhotos;
    private PhotoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("写真一覧");
        setContentView(R.layout.activity_photo_list);

        photoRepository = new PhotoRepository(this);
        recyclerPhotos = findViewById(R.id.recyclerPhotos);
        textNoPhotos = findViewById(R.id.textNoPhotos);

        adapter = new PhotoListAdapter();
        recyclerPhotos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPhotos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos();
    }

    private void loadPhotos() {
        photoRepository.listAll(photos -> {
            boolean hasPhotos = photos != null && !photos.isEmpty();
            adapter.submit(photos);
            recyclerPhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
            textNoPhotos.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
        });
    }
}
