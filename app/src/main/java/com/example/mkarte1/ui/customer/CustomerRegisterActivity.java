package com.example.mkarte1.ui.customer;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.PhotoFileUtil;

import java.io.File;

public class CustomerRegisterActivity extends AppCompatActivity {
    private EditText name;
    private EditText kana;
    private EditText phone;
    private EditText postalCode;
    private EditText address;
    private EditText memo;
    private CustomerRepository customerRepository;
    private PhotoRepository photoRepository;
    private String tempPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        customerRepository = new CustomerRepository(this);
        photoRepository = new PhotoRepository(this);
        tempPath = getIntent().getStringExtra("tempPath");

        name = findViewById(R.id.editName);
        kana = findViewById(R.id.editKana);
        phone = findViewById(R.id.editPhone);
        postalCode = findViewById(R.id.editPostalCode);
        address = findViewById(R.id.editAddress);
        memo = findViewById(R.id.editMemo);

        findViewById(R.id.buttonSave).setOnClickListener(v -> save());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void save() {
        String customerName = name.getText().toString().trim();
        if (customerName.isEmpty()) {
            name.setError("顧客名を入力してください");
            return;
        }

        long now = System.currentTimeMillis();
        Customer customer = new Customer();
        customer.name = customerName;
        customer.kana = kana.getText().toString().trim();
        customer.phone = phone.getText().toString().trim();
        customer.postalCode = postalCode.getText().toString().trim();
        customer.address = address.getText().toString().trim();
        customer.memo = memo.getText().toString().trim();
        customer.createdAt = now;
        customer.updatedAt = now;

        customerRepository.insert(customer, id -> {
            customer.id = id;
            if (tempPath == null) {
                Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                linkTempPhoto(customer);
            }
        });
    }

    private void linkTempPhoto(Customer customer) {
        try {
            String ymd = DateUtil.todayYmd();
            File finalFile = PhotoFileUtil.moveTempToCustomer(this, tempPath, customer, ymd);
            Photo photo = new Photo();
            photo.customerId = customer.id;
            photo.customerName = customer.name;
            photo.takenDate = ymd;
            photo.fileName = finalFile.getName();
            photo.uri = Uri.fromFile(finalFile).toString();
            photo.memo = "";
            photo.createdAt = System.currentTimeMillis();
            photoRepository.insert(photo, ignored -> {
                Toast.makeText(this, "登録して写真を紐づけました", Toast.LENGTH_SHORT).show();
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "写真の保存に失敗しました", Toast.LENGTH_LONG).show();
        }
    }
}
