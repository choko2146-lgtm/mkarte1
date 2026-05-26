package com.example.mkarte1.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VisitHistoryAdapter extends RecyclerView.Adapter<VisitHistoryAdapter.Holder> {
    public interface OnClick {
        void onClick(VisitHistory visitHistory);
    }

    public static class VisitHistory {
        final long customerId;
        final String customerName;
        final int photoCount;

        VisitHistory(long customerId, String customerName, int photoCount) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.photoCount = photoCount;
        }
    }

    private final List<VisitHistory> visitHistories = new ArrayList<>();
    private final OnClick onClick;

    public VisitHistoryAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<VisitHistory> values) {
        visitHistories.clear();
        if (values != null) {
            visitHistories.addAll(values);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit_history, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        VisitHistory visitHistory = visitHistories.get(position);
        holder.textCustomerName.setText(visitHistory.customerName);
        holder.textPhotoCount.setText(String.format(Locale.JAPAN,
                "\u5199\u771f%d\u679a", visitHistory.photoCount));
        holder.itemView.setOnClickListener(v -> onClick.onClick(visitHistory));
    }

    @Override
    public int getItemCount() {
        return visitHistories.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView textCustomerName;
        final TextView textPhotoCount;

        Holder(@NonNull View itemView) {
            super(itemView);
            textCustomerName = itemView.findViewById(R.id.textVisitCustomerName);
            textPhotoCount = itemView.findViewById(R.id.textVisitPhotoCount);
        }
    }
}
