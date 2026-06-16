package com.example.mkarte1.ui.customer;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.MediaStoreHelper;
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
    private long customerId = -1;
    private Customer editingCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        customerRepository = new CustomerRepository(this);
        photoRepository = new PhotoRepository(this);
        tempPath = getIntent().getStringExtra("tempPath");
        customerId = getIntent().getLongExtra("customerId", -1);

        name = findViewById(R.id.editName);
        kana = findViewById(R.id.editKana);
        phone = findViewById(R.id.editPhone);
        postalCode = findViewById(R.id.editPostalCode);
        address = findViewById(R.id.editAddress);
        memo = findViewById(R.id.editMemo);

        findViewById(R.id.buttonSave).setOnClickListener(v -> save());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        if (isEditMode()) {
            ((Button) findViewById(R.id.buttonSave)).setText("更新する");
            loadCustomerForEdit();
        }
    }

    private void loadCustomerForEdit() {
        customerRepository.get(customerId, customer -> {
            editingCustomer = customer;
            if (editingCustomer == null) {
                finish();
                return;
            }
            name.setText(editingCustomer.name);
            kana.setText(editingCustomer.kana);
            phone.setText(editingCustomer.phone);
            postalCode.setText(editingCustomer.postalCode);
            address.setText(editingCustomer.address);
            memo.setText(editingCustomer.memo);
        });
    }

    private void save() {
        String customerName = name.getText().toString().trim();
        if (customerName.isEmpty()) {
            name.setError("顧客名を入力してください");
            return;
        }
        if (isEditMode() && editingCustomer == null) {
            Toast.makeText(this, "顧客情報を読み込み中です", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        Customer customer = new Customer();
        if (isEditMode()) {
            customer.id = customerId;
        }
        customer.name = customerName;
        customer.kana = kana.getText().toString().trim();
        customer.phone = phone.getText().toString().trim();
        customer.postalCode = postalCode.getText().toString().trim();
        customer.address = address.getText().toString().trim();
        customer.memo = memo.getText().toString().trim();
        customer.createdAt = editingCustomer == null ? now : editingCustomer.createdAt;
        customer.updatedAt = now;

        if (editingCustomer != null) {
            customerRepository.update(customer, () -> {
                photoRepository.updateCustomerNameForCustomer(customer.id, customer.name, () -> {
                    Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
            return;
        }

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
            MediaStoreHelper.copyToGallery(this, finalFile, finalFile.getName());
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

    private boolean isEditMode() {
        return customerId != -1;
    }
}
