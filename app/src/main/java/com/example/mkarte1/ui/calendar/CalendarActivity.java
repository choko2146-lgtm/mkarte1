package com.example.mkarte1.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.customer.CustomerDetailActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {
    private PhotoRepository photoRepository;
    private VisitHistoryAdapter visitHistoryAdapter;
    private RecyclerView recyclerVisitHistories;
    private TextView textNoVisitHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("\u30ab\u30ec\u30f3\u30c0\u30fc");
        setContentView(R.layout.activity_calendar);

        photoRepository = new PhotoRepository(this);
        visitHistoryAdapter = new VisitHistoryAdapter(visitHistory -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("customerId", visitHistory.customerId);
            startActivity(intent);
        });
        recyclerVisitHistories = findViewById(R.id.recyclerVisitHistories);
        textNoVisitHistory = findViewById(R.id.textNoVisitHistory);
        recyclerVisitHistories.setLayoutManager(new LinearLayoutManager(this));
        recyclerVisitHistories.setAdapter(visitHistoryAdapter);

        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String takenDate = String.format(Locale.JAPAN, "%04d%02d%02d",
                    year, month + 1, dayOfMonth);
            loadVisitHistories(takenDate);
        });
        loadVisitHistories(formatTakenDate(calendarView.getDate()));
    }

    private String formatTakenDate(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        return String.format(Locale.JAPAN, "%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void loadVisitHistories(String takenDate) {
        photoRepository.listForDate(takenDate, photos -> {
            List<VisitHistoryAdapter.VisitHistory> visitHistories = groupByCustomer(photos);
            visitHistoryAdapter.submit(visitHistories);

            boolean hasVisitHistory = !visitHistories.isEmpty();
            recyclerVisitHistories.setVisibility(hasVisitHistory ? View.VISIBLE : View.GONE);
            textNoVisitHistory.setVisibility(hasVisitHistory ? View.GONE : View.VISIBLE);
        });
    }

    private List<VisitHistoryAdapter.VisitHistory> groupByCustomer(List<Photo> photos) {
        Map<Long, CustomerPhotoCount> countsByCustomer = new LinkedHashMap<>();
        if (photos != null) {
            for (Photo photo : photos) {
                CustomerPhotoCount count = countsByCustomer.get(photo.customerId);
                if (count == null) {
                    count = new CustomerPhotoCount(resolveCustomerName(photo));
                    countsByCustomer.put(photo.customerId, count);
                }
                count.photoCount++;
            }
        }

        List<VisitHistoryAdapter.VisitHistory> visitHistories = new ArrayList<>();
        for (Map.Entry<Long, CustomerPhotoCount> entry : countsByCustomer.entrySet()) {
            CustomerPhotoCount count = entry.getValue();
            visitHistories.add(new VisitHistoryAdapter.VisitHistory(
                    entry.getKey(), count.customerName, count.photoCount));
        }
        return visitHistories;
    }

    private String resolveCustomerName(Photo photo) {
        if (photo.customerName == null || photo.customerName.trim().isEmpty()) {
            return "\u540d\u524d\u672a\u8a2d\u5b9a";
        }
        return photo.customerName;
    }

    private static class CustomerPhotoCount {
        final String customerName;
        int photoCount;

        CustomerPhotoCount(String customerName) {
            this.customerName = customerName;
        }
    }
}
