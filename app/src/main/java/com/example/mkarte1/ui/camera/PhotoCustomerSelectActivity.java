package com.example.mkarte1.ui.camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.customer.CustomerAdapter;
import com.example.mkarte1.ui.customer.CustomerRegisterActivity;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.PhotoFileUtil;

import java.io.File;

public class PhotoCustomerSelectActivity extends AppCompatActivity {
    private String tempPath;
    private Customer selectedCustomer;
    private CustomerRepository customerRepository;
    private PhotoRepository photoRepository;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_customer_select);
        tempPath = getIntent().getStringExtra("tempPath");
        customerRepository = new CustomerRepository(this);
        photoRepository = new PhotoRepository(this);

        ImageView preview = findViewById(R.id.imagePreview);
        preview.setImageURI(Uri.fromFile(new File(tempPath)));

        adapter = new CustomerAdapter(customer -> {
            selectedCustomer = customer;
            adapter.setSelectedId(customer.id);
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerCustomers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        EditText search = findViewById(R.id.editSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { load(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.buttonNewCustomer).setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerRegisterActivity.class);
            intent.putExtra("tempPath", tempPath);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.buttonLink).setOnClickListener(v -> link());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(((EditText) findViewById(R.id.editSearch)).getText().toString());
    }

    private void load(String query) {
        customerRepository.list(query, adapter::submit);
    }

    private void link() {
        if (selectedCustomer == null) {
            Toast.makeText(this, "顧客を選択してください", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String ymd = DateUtil.todayYmd();
            File finalFile = PhotoFileUtil.moveTempToCustomer(this, tempPath, selectedCustomer, ymd);
            Photo photo = new Photo();
            photo.customerId = selectedCustomer.id;
            photo.customerName = selectedCustomer.name;
            photo.takenDate = ymd;
            photo.fileName = finalFile.getName();
            photo.uri = Uri.fromFile(finalFile).toString();
            photo.memo = "";
            photo.createdAt = System.currentTimeMillis();
            photoRepository.insert(photo, ignored -> {
                Toast.makeText(this, "写真を紐づけました", Toast.LENGTH_SHORT).show();
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "写真の保存に失敗しました", Toast.LENGTH_LONG).show();
        }
    }
}
