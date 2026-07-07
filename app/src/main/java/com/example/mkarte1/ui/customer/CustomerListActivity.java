package com.example.mkarte1.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.CustomerWithLatestDate;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.ui.EdgeToEdgeUtil;
import com.example.mkarte1.ui.MkarteBottomNav;
import com.example.mkarte1.util.CsvShareUtil;
import com.example.mkarte1.util.CustomerAddressCsvUtil;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerListActivity extends AppCompatActivity {
    private static final String TAG = "CustomerListActivity";
    private static final int SORT_LATEST_DESC = 0;
    private static final int SORT_LATEST_ASC = 1;
    private static final int SORT_NAME = 2;
    private static final int SORT_PHOTO_COUNT_DESC = 3;

    private CustomerRepository repository;
    private CustomerAdapter adapter;
    private int currentSort = SORT_LATEST_DESC;
    private EditText searchEdit;
    private final Collator nameCollator = Collator.getInstance(Locale.JAPANESE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        EdgeToEdgeUtil.apply(this);
        MkarteBottomNav.bind(this, R.id.navCustomers);
        repository = new CustomerRepository(this);
        adapter = new CustomerAdapter(customer -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("customerId", customer.id);
            startActivity(intent);
        }, true);
        RecyclerView recyclerView = findViewById(R.id.recyclerCustomers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        searchEdit = findViewById(R.id.editSearch);

        setupSortSpinner();

        searchEdit.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> load(""));
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { load(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(searchEdit.getText().toString());
    }

    private void load(String query) {
        repository.listWithLatestDate(query, customers -> adapter.submit(sortCustomers(customers)));
    }

    private void setupSortSpinner() {
        Spinner spinner = findViewById(R.id.spinnerCustomerSort);
        String[] options = {
                "最終撮影日 新しい順",
                "最終撮影日 古い順",
                "名前順",
                "写真枚数 多い順"
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sortAdapter);
        spinner.setSelection(currentSort);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                currentSort = position;
                load(searchEdit.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private List<CustomerWithLatestDate> sortCustomers(List<CustomerWithLatestDate> customers) {
        List<CustomerWithLatestDate> sorted = new ArrayList<>();
        if (customers != null) {
            sorted.addAll(customers);
        }
        switch (currentSort) {
            case SORT_LATEST_ASC:
                sorted.sort((left, right) -> {
                    int result = compareLatestDate(left, right, true);
                    return result != 0 ? result : compareName(left, right);
                });
                break;
            case SORT_NAME:
                sorted.sort((left, right) -> {
                    int result = compareName(left, right);
                    return result != 0 ? result : compareLatestDate(left, right, false);
                });
                break;
            case SORT_PHOTO_COUNT_DESC:
                sorted.sort((left, right) -> {
                    int result = Integer.compare(right.photoCount, left.photoCount);
                    return result != 0 ? result : compareLatestDate(left, right, false);
                });
                break;
            case SORT_LATEST_DESC:
            default:
                sorted.sort((left, right) -> {
                    int result = compareLatestDate(left, right, false);
                    return result != 0 ? result : compareName(left, right);
                });
                break;
        }
        return sorted;
    }

    private int compareLatestDate(CustomerWithLatestDate left, CustomerWithLatestDate right, boolean ascending) {
        String leftDate = left == null ? "" : safe(left.latestTakenDate);
        String rightDate = right == null ? "" : safe(right.latestTakenDate);
        boolean leftEmpty = leftDate.isEmpty();
        boolean rightEmpty = rightDate.isEmpty();
        if (leftEmpty && rightEmpty) {
            return 0;
        }
        if (leftEmpty) {
            return 1;
        }
        if (rightEmpty) {
            return -1;
        }
        int result = leftDate.compareTo(rightDate);
        return ascending ? result : -result;
    }

    private int compareName(CustomerWithLatestDate left, CustomerWithLatestDate right) {
        return nameCollator.compare(
                resolveName(left == null ? null : left.customer),
                resolveName(right == null ? null : right.customer)
        );
    }

    private String resolveName(Customer customer) {
        if (customer == null) {
            return "";
        }
        String name = safe(customer.name);
        return name.isEmpty() ? safe(customer.kana) : name;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void exportAddressCsv() {
        repository.listCustomerAddresses(customers -> {
            if (customers == null || customers.isEmpty()) {
                Toast.makeText(this, "出力できる住所データがありません", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    File csvFile = CustomerAddressCsvUtil.createCsvFile(this, customers);
                    runOnUiThread(() -> CsvShareUtil.shareCsv(this, csvFile));
                } catch (IOException e) {
                    Log.e(TAG, "Failed to export customer address CSV", e);
                    runOnUiThread(() -> Toast.makeText(this, "CSV出力に失敗しました", Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }
}
