package com.example.mkarte1.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mkarte1.MainActivity;
import com.example.mkarte1.PhotoListActivity;
import com.example.mkarte1.R;
import com.example.mkarte1.ui.calendar.CalendarActivity;
import com.example.mkarte1.ui.customer.CustomerListActivity;
import com.example.mkarte1.ui.other.OtherActivity;

public final class MkarteBottomNav {
    private MkarteBottomNav() {
    }

    public static void bind(Activity activity, int currentNavId) {
        bind(activity, currentNavId, true);
    }

    public static void bind(Activity activity, int currentNavId, boolean disableCurrentClick) {
        bindItem(activity, R.id.navHome, currentNavId, MainActivity.class, disableCurrentClick);
        bindItem(activity, R.id.navCustomers, currentNavId, CustomerListActivity.class, disableCurrentClick);
        bindItem(activity, R.id.navCalendar, currentNavId, CalendarActivity.class, disableCurrentClick);
        bindItem(activity, R.id.navPhotos, currentNavId, PhotoListActivity.class, disableCurrentClick);
        bindItem(activity, R.id.navMore, currentNavId, OtherActivity.class, disableCurrentClick);
    }

    private static void bindItem(Activity activity, int navId, int currentNavId, Class<?> target, boolean disableCurrentClick) {
        if (activity.findViewById(navId) == null) {
            return;
        }
        View item = activity.findViewById(navId);
        item.setSelected(navId == currentNavId);
        tintNavItem(activity, item, navId == currentNavId);
        item.setOnClickListener(v -> {
            if (disableCurrentClick && navId == currentNavId) {
                return;
            }
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        });
    }

    private static void tintNavItem(Activity activity, View view, boolean active) {
        int color = activity.getResources().getColor(
                active ? R.color.mkarte_primary : R.color.mkarte_text_subtle,
                null
        );
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(color);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                tintNavItem(activity, group.getChildAt(i), active);
            }
        }
    }
}
