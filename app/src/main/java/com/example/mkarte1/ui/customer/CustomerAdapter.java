package com.example.mkarte1.ui.customer;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.data.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.Holder> {
    public interface OnClick {
        void onClick(Customer customer);
    }

    private final List<Customer> customers = new ArrayList<>();
    private final OnClick onClick;
    private long selectedId = -1;

    public CustomerAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Customer> values) {
        customers.clear();
        customers.addAll(values);
        notifyDataSetChanged();
    }

    public void setSelectedId(long selectedId) {
        this.selectedId = selectedId;
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
        Customer customer = customers.get(position);
        holder.title.setText(customer.name);
        holder.detail.setText(customer.kana + "  " + customer.phone);
        holder.itemView.setBackgroundColor(customer.id == selectedId ? Color.rgb(232, 244, 238) : Color.WHITE);
        holder.itemView.setOnClickListener(v -> onClick.onClick(customer));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        TextView detail;

        Holder(@NonNull LinearLayout itemView, TextView title, TextView detail) {
            super(itemView);
            this.title = title;
            this.detail = detail;
        }
    }
}
