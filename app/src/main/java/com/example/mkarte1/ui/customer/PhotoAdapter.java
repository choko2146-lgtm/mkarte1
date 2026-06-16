package com.example.mkarte1.ui.customer;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;

import java.io.File;
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
        int size = Math.max(120, parent.getResources().getDisplayMetrics().widthPixels / 2 - 32);
        imageView.setLayoutParams(new RecyclerView.LayoutParams(size, size));
        imageView.setBackgroundResource(R.drawable.bg_image_soft);
        imageView.setClickable(true);
        imageView.setFocusable(false);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(6, 6, 6, 6);
        return new Holder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Photo photo = photos.get(position);
        bindImage(holder.imageView, photo);
        holder.itemView.setSelected(false);
        holder.itemView.setActivated(false);
        View.OnClickListener listener = v -> openPhotoAt(holder.getBindingAdapterPosition());
        holder.itemView.setOnClickListener(listener);
        holder.imageView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    private void bindImage(ImageView imageView, Photo photo) {
        imageView.setImageDrawable(null);
        imageView.setBackgroundResource(R.drawable.bg_image_soft);

        if (photo == null || photo.uri == null || photo.uri.trim().isEmpty()) {
            return;
        }

        try {
            Uri uri = Uri.parse(photo.uri);
            if ("file".equals(uri.getScheme()) && uri.getPath() != null) {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    imageView.setImageURI(Uri.fromFile(file));
                    return;
                }
            }
            imageView.setImageURI(uri);
        } catch (Exception ignored) {
            imageView.setImageDrawable(null);
        }
    }

    private void openPhotoAt(int adapterPosition) {
        if (adapterPosition == RecyclerView.NO_POSITION || onClick == null) {
            return;
        }
        onClick.onClick(photos.get(adapterPosition));
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;

        Holder(@NonNull ImageView itemView) {
            super(itemView);
            imageView = itemView;
        }
    }
}
