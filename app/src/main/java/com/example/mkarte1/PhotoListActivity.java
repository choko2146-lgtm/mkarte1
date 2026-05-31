package com.example.mkarte1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity {
    private PhotoRepository photoRepository;
    private RecyclerView recyclerPhotos;
    private TextView textNoPhotos;
    private PlaceholderPhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("写真一覧");
        setContentView(R.layout.activity_photo_list);

        photoRepository = new PhotoRepository(this);
        recyclerPhotos = findViewById(R.id.recyclerPhotos);
        textNoPhotos = findViewById(R.id.textNoPhotos);

        adapter = new PlaceholderPhotoAdapter();
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

    private static class PlaceholderPhotoAdapter extends RecyclerView.Adapter<PlaceholderPhotoAdapter.Holder> {
        private final List<Photo> photos = new ArrayList<>();

        void submit(List<Photo> values) {
            photos.clear();
            if (values != null) {
                photos.addAll(values);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(24, 20, 24, 20);
            layout.setBackgroundColor(Color.WHITE);

            TextView title = new TextView(parent.getContext());
            title.setTextSize(18);
            title.setTextColor(Color.rgb(46, 42, 38));

            TextView detail = new TextView(parent.getContext());
            detail.setTextSize(14);
            detail.setTextColor(Color.DKGRAY);

            layout.addView(title);
            layout.addView(detail);
            return new Holder(layout, title, detail);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Photo photo = photos.get(position);
            holder.title.setText(resolveCustomerName(photo));
            holder.detail.setText(DateUtil.displayYmd(photo.takenDate) + "  " + resolveFileName(photo));
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        private static String resolveCustomerName(Photo photo) {
            if (photo.customerName == null || photo.customerName.trim().isEmpty()) {
                return "名前未設定";
            }
            return photo.customerName;
        }

        private static String resolveFileName(Photo photo) {
            if (photo.fileName == null || photo.fileName.trim().isEmpty()) {
                return "";
            }
            return photo.fileName;
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView detail;

            Holder(@NonNull LinearLayout itemView, TextView title, TextView detail) {
                super(itemView);
                this.title = title;
                this.detail = detail;
            }
        }
    }
}
