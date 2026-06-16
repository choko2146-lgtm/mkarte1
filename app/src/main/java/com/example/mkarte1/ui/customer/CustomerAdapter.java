package com.example.mkarte1.ui.customer;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
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
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(parent, 10));
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dp(parent, 16), dp(parent, 14), dp(parent, 16), dp(parent, 14));
        layout.setBackgroundResource(R.drawable.bg_card);
        layout.setElevation(dp(parent, 1));

        TextView avatar = new TextView(parent.getContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(parent, 42), dp(parent, 42));
        avatar.setLayoutParams(avatarParams);
        avatar.setBackgroundResource(R.drawable.bg_customer_avatar);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextColor(parent.getResources().getColor(R.color.white, null));
        avatar.setTextSize(18);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout content = new LinearLayout(parent.getContext());
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        contentParams.setMargins(dp(parent, 14), 0, 0, 0);
        content.setLayoutParams(contentParams);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(parent.getContext());
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(parent.getResources().getColor(R.color.mkarte_text, null));
        TextView detail = new TextView(parent.getContext());
        detail.setTextSize(14);
        detail.setTextColor(parent.getResources().getColor(R.color.mkarte_text_subtle, null));
        TextView address = new TextView(parent.getContext());
        address.setTextSize(13);
        address.setTextColor(parent.getResources().getColor(R.color.mkarte_text_subtle, null));

        content.addView(title);
        content.addView(detail);
        content.addView(address);
        layout.addView(avatar);
        layout.addView(content);
        return new Holder(layout, avatar, title, detail, address);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Customer customer = customers.get(position);
        holder.avatar.setText(resolveInitial(customer.name));
        holder.title.setText(customer.name);
        holder.detail.setText(resolveDetail(customer));
        String address = safe(customer.address);
        holder.address.setText(address);
        holder.address.setVisibility(address.isEmpty() ? TextView.GONE : TextView.VISIBLE);
        holder.itemView.setBackgroundResource(customer.id == selectedId
                ? R.drawable.bg_card_selected
                : R.drawable.bg_card);
        holder.itemView.setOnClickListener(v -> onClick.onClick(customer));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    private String resolveInitial(String name) {
        String safeName = safe(name);
        return safeName.isEmpty() ? "？" : safeName.substring(0, 1);
    }

    private String resolveDetail(Customer customer) {
        String kana = safe(customer.kana);
        String phone = safe(customer.phone);
        if (kana.isEmpty()) {
            return phone;
        }
        if (phone.isEmpty()) {
            return kana;
        }
        return kana + "  " + phone;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(ViewGroup parent, int value) {
        return Math.round(value * parent.getResources().getDisplayMetrics().density);
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView avatar;
        TextView title;
        TextView detail;
        TextView address;

        Holder(@NonNull LinearLayout itemView, TextView avatar, TextView title, TextView detail, TextView address) {
            super(itemView);
            this.avatar = avatar;
            this.title = title;
            this.detail = detail;
            this.address = address;
        }
    }
}
