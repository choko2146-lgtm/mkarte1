package com.example.mkarte1.ui.customer;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.data.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.Holder> {
    public interface OnClick {
        void onClick(Photo photo);
    }

    private final List<Photo> photos = new ArrayList<>();
    private final OnClick onClick;

    public PhotoAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Photo> values) {
        photos.clear();
        if (values != null) {
            photos.addAll(values);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        int size = parent.getResources().getDisplayMetrics().widthPixels / 2 - 32;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(6, 6, 6, 6);
        return new Holder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Photo photo = photos.get(position);
        holder.imageView.setImageURI(Uri.parse(photo.uri));
        holder.imageView.setOnClickListener(v -> onClick.onClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;

        Holder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }
}
