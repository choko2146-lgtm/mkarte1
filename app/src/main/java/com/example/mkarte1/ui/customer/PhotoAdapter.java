package com.example.mkarte1.ui.customer;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        Glide.with(holder.imageView).clear(holder.imageView);
        holder.imageView.setImageDrawable(null);
        super.onViewRecycled(holder);
    }

    private void bindImage(ImageView imageView, Photo photo) {
        Glide.with(imageView).clear(imageView);
        imageView.setImageDrawable(null);
        imageView.setBackgroundResource(R.drawable.bg_image_soft);

        Object imageModel = resolveImageModel(photo);
        if (imageModel == null) {
            return;
        }

        Glide.with(imageView)
                .load(imageModel)
                .centerCrop()
                .override(resolveTargetWidth(imageView), resolveTargetHeight(imageView))
                .placeholder(R.drawable.bg_image_soft)
                .error(R.drawable.bg_image_soft)
                .fallback(R.drawable.bg_image_soft)
                .dontAnimate()
                .into(imageView);
    }

    private Object resolveImageModel(Photo photo) {
        if (photo == null || photo.uri == null) {
            return null;
        }

        String rawUri = photo.uri.trim();
        if (rawUri.isEmpty()) {
            return null;
        }

        try {
            Uri uri = Uri.parse(rawUri);
            if ("file".equals(uri.getScheme()) && uri.getPath() != null) {
                return new File(uri.getPath());
            }
            if (uri.getScheme() != null) {
                return uri;
            }
        } catch (Exception ignored) {
            return null;
        }
        return new File(rawUri);
    }

    private int resolveTargetWidth(ImageView imageView) {
        if (imageView.getWidth() > 0) {
            return imageView.getWidth();
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params != null && params.width > 0) {
            return params.width;
        }
        return Math.max(1, imageView.getResources().getDisplayMetrics().widthPixels / 2);
    }

    private int resolveTargetHeight(ImageView imageView) {
        if (imageView.getHeight() > 0) {
            return imageView.getHeight();
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        if (params != null && params.height > 0) {
            return params.height;
        }
        return resolveTargetWidth(imageView);
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
