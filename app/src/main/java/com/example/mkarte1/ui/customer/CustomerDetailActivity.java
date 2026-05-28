package com.example.mkarte1.ui.customer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.photo.PhotoDetailActivity;
import com.example.mkarte1.util.PhotoFileUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomerDetailActivity extends AppCompatActivity {
    private long customerId;
    private Customer customer;
    private CustomerRepository customerRepository;
    private PhotoRepository photoRepository;
    private VisitDateAdapter visitDateAdapter;
    private List<Photo> currentPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        customerId = getIntent().getLongExtra("customerId", -1);
        customerRepository = new CustomerRepository(this);
        photoRepository = new PhotoRepository(this);

        visitDateAdapter = new VisitDateAdapter(photo -> {
            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("photoId", photo.id);
            startActivity(intent);
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerPhotos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(visitDateAdapter);
        findViewById(R.id.buttonEditCustomer).setOnClickListener(v -> openEdit());
        findViewById(R.id.buttonDeleteCustomer).setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        customerRepository.get(customerId, value -> {
            customer = value;
            if (customer == null) {
                finish();
                return;
            }
            ((TextView) findViewById(R.id.textCustomer)).setText(
                    customer.name + "\n" +
                            customer.kana + "\n" +
                            customer.phone + "\n" +
                            customer.postalCode + "\n" +
                            customer.address + "\n\n" +
                            customer.memo);
        });
        photoRepository.listForCustomer(customerId, photos -> {
            currentPhotos = photos;
            visitDateAdapter.submit(groupPhotosByTakenDate(photos));
        });
    }

    private List<VisitDateAdapter.DateGroup> groupPhotosByTakenDate(List<Photo> photos) {
        Map<String, List<Photo>> photosByDate = new LinkedHashMap<>();
        if (photos != null) {
            for (Photo photo : photos) {
                String takenDate = photo.takenDate;
                if (!photosByDate.containsKey(takenDate)) {
                    photosByDate.put(takenDate, new ArrayList<>());
                }
                photosByDate.get(takenDate).add(photo);
            }
        }

        List<VisitDateAdapter.DateGroup> dateGroups = new ArrayList<>();
        for (Map.Entry<String, List<Photo>> entry : photosByDate.entrySet()) {
            dateGroups.add(new VisitDateAdapter.DateGroup(entry.getKey(), entry.getValue()));
        }
        return dateGroups;
    }

    private void openEdit() {
        Intent intent = new Intent(this, CustomerRegisterActivity.class);
        intent.putExtra("customerId", customerId);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (customer == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("顧客を削除しますか？")
                .setMessage("関連する写真も削除されます。")
                .setPositiveButton("削除", (dialog, which) -> {
                    if (currentPhotos != null) {
                        for (Photo photo : currentPhotos) {
                            PhotoFileUtil.deleteQuietly(photo.uri);
                        }
                    }
                    customerRepository.delete(customer, () -> {
                        Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }
}
