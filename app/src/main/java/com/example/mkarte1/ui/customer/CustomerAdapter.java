package com.example.mkarte1.ui.customer;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.CustomerWithLatestDate;
import com.example.mkarte1.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.Holder> {
    public interface OnClick {
        void onClick(Customer customer);
    }

    private final List<CustomerWithLatestDate> customers = new ArrayList<>();
    private final OnClick onClick;
    private final boolean showLatestVisitDate;
    private long selectedId = -1;

    public CustomerAdapter(OnClick onClick) {
        this(onClick, false);
    }

    public CustomerAdapter(OnClick onClick, boolean showLatestVisitDate) {
        this.onClick = onClick;
        this.showLatestVisitDate = showLatestVisitDate;
    }

    public void submit(List<CustomerWithLatestDate> values) {
        customers.clear();
        customers.addAll(values);
        notifyDataSetChanged();
    }

    public void submitCustomers(List<Customer> values) {
        customers.clear();
        for (Customer customer : values) {
            CustomerWithLatestDate row = new CustomerWithLatestDate();
            row.customer = customer;
            customers.add(row);
        }
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
        params.setMargins(0, 0, 0, dp(parent, 12));
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setMinimumHeight(dp(parent, 98));
        layout.setPadding(dp(parent, 16), dp(parent, 16), dp(parent, 12), dp(parent, 16));
        layout.setBackgroundResource(R.drawable.bg_card);
        layout.setElevation(dp(parent, 1));

        TextView avatar = new TextView(parent.getContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(parent, 54), dp(parent, 54));
        avatar.setLayoutParams(avatarParams);
        avatar.setBackgroundResource(R.drawable.bg_customer_avatar);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextColor(parent.getResources().getColor(R.color.mkarte_avatar_text, null));
        avatar.setTextSize(20);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        LinearLayout content = new LinearLayout(parent.getContext());
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        contentParams.setMargins(dp(parent, 14), 0, dp(parent, 8), 0);
        content.setLayoutParams(contentParams);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(parent.getContext());
        title.setTextSize(17);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(parent.getResources().getColor(R.color.mkarte_text, null));
        TextView detail = new TextView(parent.getContext());
        detail.setTextSize(12);
        detail.setTextColor(parent.getResources().getColor(R.color.mkarte_text_subtle, null));
        TextView latestVisitDate = new TextView(parent.getContext());
        LinearLayout.LayoutParams latestParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        latestParams.setMargins(0, dp(parent, 3), 0, 0);
        latestVisitDate.setLayoutParams(latestParams);
        latestVisitDate.setTextSize(13);
        latestVisitDate.setTextColor(parent.getResources().getColor(R.color.mkarte_text, null));
        TextView photoCount = new TextView(parent.getContext());
        LinearLayout.LayoutParams photoCountParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        photoCountParams.setMargins(0, dp(parent, 6), 0, 0);
        photoCount.setLayoutParams(photoCountParams);
        photoCount.setPadding(dp(parent, 8), dp(parent, 3), dp(parent, 8), dp(parent, 3));
        photoCount.setBackgroundResource(R.drawable.bg_chip);
        photoCount.setTextSize(11);
        photoCount.setTextColor(parent.getResources().getColor(R.color.mkarte_text_subtle, null));
        ImageView chevron = new ImageView(parent.getContext());
        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(dp(parent, 24), dp(parent, 24));
        chevron.setLayoutParams(chevronParams);
        chevron.setImageResource(R.drawable.ic_chevron_right_24);
        chevron.setContentDescription("詳細へ");

        content.addView(title);
        content.addView(latestVisitDate);
        content.addView(detail);
        content.addView(photoCount);
        layout.addView(avatar);
        layout.addView(content);
        layout.addView(chevron);
        return new Holder(layout, avatar, title, latestVisitDate, detail, photoCount, chevron);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CustomerWithLatestDate row = customers.get(position);
        Customer customer = row.customer;
        holder.avatar.setText(resolveInitial(customer.name));
        holder.title.setText(customer.name);
        holder.latestVisitDate.setText(formatLatestPhotoDate(row.latestTakenDate));
        holder.latestVisitDate.setVisibility(showLatestVisitDate ? TextView.VISIBLE : TextView.GONE);
        String detail = resolveDetail(customer);
        holder.detail.setText(detail);
        holder.detail.setVisibility(detail.isEmpty() ? TextView.GONE : TextView.VISIBLE);
        holder.photoCount.setText("写真 " + row.photoCount + "枚");
        holder.photoCount.setVisibility(showLatestVisitDate ? TextView.VISIBLE : TextView.GONE);
        holder.chevron.setVisibility(showLatestVisitDate ? ImageView.VISIBLE : ImageView.GONE);
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

    private String formatLatestPhotoDate(String latestTakenDate) {
        String displayDate = DateUtil.displayYmd(latestTakenDate);
        return "最終撮影日：" + (displayDate.isEmpty() ? "未登録" : displayDate);
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
        TextView latestVisitDate;
        TextView detail;
        TextView photoCount;
        ImageView chevron;

        Holder(@NonNull LinearLayout itemView, TextView avatar, TextView title, TextView latestVisitDate, TextView detail, TextView photoCount, ImageView chevron) {
            super(itemView);
            this.avatar = avatar;
            this.title = title;
            this.latestVisitDate = latestVisitDate;
            this.detail = detail;
            this.photoCount = photoCount;
            this.chevron = chevron;
        }
    }
}
