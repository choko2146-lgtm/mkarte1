package com.example.mkarte1;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.data.Photo;
import com.example.mkarte1.util.DateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.Holder> {
    public interface OnClick {
        void onClick(Photo photo);
    }

    private final List<Photo> photos = new ArrayList<>();
    private final OnClick onClick;

    public PhotoListAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Photo> values) {
        if (hasSameContent(values)) {
            return;
        }

        photos.clear();
        if (values != null) {
            photos.addAll(values);
        }
        notifyDataSetChanged();
    }

    private boolean hasSameContent(List<Photo> values) {
        int newSize = values == null ? 0 : values.size();
        if (photos.size() != newSize) {
            return false;
        }
        for (int i = 0; i < newSize; i++) {
            if (!hasSameContent(photos.get(i), values.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSameContent(Photo current, Photo next) {
        if (current == next) {
            return true;
        }
        if (current == null || next == null) {
            return false;
        }
        return current.id == next.id
                && current.customerId == next.customerId
                && current.createdAt == next.createdAt
                && sameText(current.customerName, next.customerName)
                && sameText(current.takenDate, next.takenDate)
                && sameText(current.fileName, next.fileName)
                && sameText(current.uri, next.uri)
                && sameText(current.memo, next.memo);
    }

    private boolean sameText(String current, String next) {
        return current == null ? next == null : current.equals(next);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Photo photo = photos.get(position);
        bindImage(holder.imagePhoto, photo);
        holder.textCustomerName.setText(resolveCustomerName(photo));
        holder.textTakenDate.setText(DateUtil.displayYmd(photo.takenDate));

        String memo = resolveMemo(photo);
        holder.textMemo.setText(memo);
        holder.textMemo.setVisibility(memo.isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> onClick.onClick(photo));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    private void bindImage(ImageView imageView, Photo photo) {
        imageView.setImageDrawable(null);
        imageView.setBackgroundColor(Color.rgb(238, 238, 238));

        if (photo.uri == null || photo.uri.trim().isEmpty()) {
            return;
        }

        try {
            Uri uri = Uri.parse(photo.uri);
            if ("file".equals(uri.getScheme()) && !fileExists(uri)) {
                return;
            }
            imageView.setImageURI(uri);
        } catch (Exception ignored) {
            imageView.setImageDrawable(null);
        }
    }

    private boolean fileExists(Uri uri) {
        String path = uri.getPath();
        return path != null && new File(path).exists();
    }

    private String resolveCustomerName(Photo photo) {
        if (photo.customerName == null || photo.customerName.trim().isEmpty()) {
            return "名前未設定";
        }
        return photo.customerName;
    }

    private String resolveMemo(Photo photo) {
        if (photo.memo == null) {
            return "";
        }
        return photo.memo.trim();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imagePhoto;
        final TextView textCustomerName;
        final TextView textTakenDate;
        final TextView textMemo;

        Holder(@NonNull View itemView) {
            super(itemView);
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textTakenDate = itemView.findViewById(R.id.textTakenDate);
            textMemo = itemView.findViewById(R.id.textMemo);
        }
    }
}
