package com.example.mkarte1.ui.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;

import java.util.ArrayList;
import java.util.List;

public class VisitDateAdapter extends RecyclerView.Adapter<VisitDateAdapter.Holder> {
    public static class DateGroup {
        final String takenDate;
        final List<Photo> photos;

        DateGroup(String takenDate, List<Photo> photos) {
            this.takenDate = takenDate;
            this.photos = photos;
        }
    }

    private final List<DateGroup> dateGroups = new ArrayList<>();
    private final PhotoAdapter.OnClick onPhotoClick;

    public VisitDateAdapter(PhotoAdapter.OnClick onPhotoClick) {
        this.onPhotoClick = onPhotoClick;
    }

    public void submit(List<DateGroup> values) {
        dateGroups.clear();
        if (values != null) {
            dateGroups.addAll(values);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit_date, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DateGroup dateGroup = dateGroups.get(position);
        holder.textTakenDate.setText(formatTakenDate(dateGroup.takenDate));
        holder.photoAdapter.submit(dateGroup.photos);
    }

    @Override
    public int getItemCount() {
        return dateGroups.size();
    }

    private String formatTakenDate(String takenDate) {
        if (takenDate == null || takenDate.length() != 8) {
            return "No date";
        }
        return takenDate.substring(0, 4) + "/"
                + takenDate.substring(4, 6) + "/"
                + takenDate.substring(6, 8);
    }

    class Holder extends RecyclerView.ViewHolder {
        final TextView textTakenDate;
        final PhotoAdapter photoAdapter;

        Holder(@NonNull View itemView) {
            super(itemView);
            textTakenDate = itemView.findViewById(R.id.textTakenDate);
            RecyclerView recyclerDatePhotos = itemView.findViewById(R.id.recyclerDatePhotos);
            recyclerDatePhotos.setLayoutManager(new GridLayoutManager(itemView.getContext(), 2));
            recyclerDatePhotos.setNestedScrollingEnabled(false);
            photoAdapter = new PhotoAdapter(onPhotoClick);
            recyclerDatePhotos.setAdapter(photoAdapter);
        }
    }
}
