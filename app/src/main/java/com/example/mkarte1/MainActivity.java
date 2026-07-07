package com.example.mkarte1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.CustomerWithLatestDate;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.ui.EdgeToEdgeUtil;
import com.example.mkarte1.ui.camera.CameraActivity;
import com.example.mkarte1.ui.MkarteBottomNav;
import com.example.mkarte1.ui.customer.CustomerDetailActivity;
import com.example.mkarte1.ui.customer.CustomerListActivity;
import com.example.mkarte1.ui.customer.CustomerRegisterActivity;
import com.example.mkarte1.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CustomerRepository customerRepository;
    private final int[] recentRows = {
            R.id.recentCustomerRow1,
            R.id.recentCustomerRow2,
            R.id.recentCustomerRow3
    };
    private final int[] recentInitials = {
            R.id.recentInitial1,
            R.id.recentInitial2,
            R.id.recentInitial3
    };
    private final int[] recentNames = {
            R.id.recentName1,
            R.id.recentName2,
            R.id.recentName3
    };
    private final int[] recentDates = {
            R.id.recentDate1,
            R.id.recentDate2,
            R.id.recentDate3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeUtil.apply(this);
        MkarteBottomNav.bind(this, R.id.navHome);
        customerRepository = new CustomerRepository(this);

        findViewById(R.id.buttonCamera).setOnClickListener(v ->
                startActivity(new Intent(this, CameraActivity.class)));
        findViewById(R.id.buttonCustomerRegister).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerRegisterActivity.class)));
        findViewById(R.id.buttonAllCustomers).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerListActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentCustomers();
    }

    private void loadRecentCustomers() {
        customerRepository.listWithLatestDate("", customers -> {
            List<CustomerWithLatestDate> recentCustomers = filterCustomersWithPhotos(customers);
            int count = Math.min(3, recentCustomers.size());
            findViewById(R.id.recentCustomersContainer).setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            findViewById(R.id.recentEmptyText).setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            for (int i = 0; i < recentRows.length; i++) {
                View row = findViewById(recentRows[i]);
                if (i >= count) {
                    row.setVisibility(View.GONE);
                    continue;
                }
                CustomerWithLatestDate item = recentCustomers.get(i);
                Customer customer = item.customer;
                row.setVisibility(View.VISIBLE);
                ((TextView) findViewById(recentInitials[i])).setText(resolveInitial(customer.name));
                ((TextView) findViewById(recentNames[i])).setText(safe(customer.name));
                ((TextView) findViewById(recentDates[i])).setText(formatLatestPhotoDate(item.latestTakenDate));
                row.setOnClickListener(v -> openCustomer(customer));
            }
        });
    }

    private List<CustomerWithLatestDate> filterCustomersWithPhotos(List<CustomerWithLatestDate> customers) {
        List<CustomerWithLatestDate> filtered = new ArrayList<>();
        if (customers == null) {
            return filtered;
        }
        for (CustomerWithLatestDate item : customers) {
            if (item != null && !safe(item.latestTakenDate).isEmpty()) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private void openCustomer(Customer customer) {
        if (customer == null) {
            return;
        }
        Intent intent = new Intent(this, CustomerDetailActivity.class);
        intent.putExtra("customerId", customer.id);
        startActivity(intent);
    }

    private String formatLatestPhotoDate(String latestTakenDate) {
        String displayDate = DateUtil.displayYmd(latestTakenDate);
        return "最終撮影日: " + (displayDate.isEmpty() ? "未登録" : displayDate);
    }

    private String resolveInitial(String value) {
        String text = safe(value);
        return text.isEmpty() ? "？" : text.substring(0, 1);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
