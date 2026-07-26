package com.example.mkarte1.ui.customer;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.network.ZipCloudAddressClient;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.EdgeToEdgeUtil;
import com.example.mkarte1.util.DateUtil;
import com.example.mkarte1.util.MediaStoreHelper;
import com.example.mkarte1.util.PhotoFileUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerRegisterActivity extends AppCompatActivity {
    private static final String TAG = "CustomerRegisterActivity";
    private static final String ADDRESS_NOT_FOUND_MESSAGE = "該当する住所が見つかりませんでした";
    private static final String ADDRESS_LOOKUP_ERROR_MESSAGE = "住所を取得できませんでした。通信状況を確認してください";

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
    private ZipCloudAddressClient zipCloudAddressClient;
    private ExecutorService addressLookupExecutor;
    private boolean settingInitialCustomerData;
    private boolean activityDestroyed;
    private String lastRequestedPostalCode = "";
    private final Set<String> inFlightPostalCodes = new HashSet<>();
    private AlertDialog addressSelectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        EdgeToEdgeUtil.apply(this);
        customerRepository = new CustomerRepository(this);
        photoRepository = new PhotoRepository(this);
        zipCloudAddressClient = new ZipCloudAddressClient();
        addressLookupExecutor = Executors.newSingleThreadExecutor();
        tempPath = getIntent().getStringExtra("tempPath");
        customerId = getIntent().getLongExtra("customerId", -1);

        name = findViewById(R.id.editName);
        kana = findViewById(R.id.editKana);
        phone = findViewById(R.id.editPhone);
        postalCode = findViewById(R.id.editPostalCode);
        address = findViewById(R.id.editAddress);
        memo = findViewById(R.id.editMemo);
        setupPostalCodeAddressLookup();

        TextView title = findViewById(R.id.textRegisterTitle);
        Button saveButton = findViewById(R.id.buttonSave);
        Button backButton = findViewById(R.id.buttonBack);
        saveButton.setOnClickListener(v -> save());
        findViewById(R.id.buttonBackTop).setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
        configurePrimaryActionButton(saveButton);
        clearButtonTint(backButton);

        if (isEditMode()) {
            title.setText("顧客情報");
            saveButton.setText("修正する");
            configurePrimaryActionButton(saveButton);
            saveButton.setContentDescription("修正する");
            loadCustomerForEdit();
        } else {
            title.setText("新規顧客登録");
            saveButton.setText("保存する");
            configurePrimaryActionButton(saveButton);
            saveButton.setContentDescription("保存する");
        }
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        if (addressSelectionDialog != null && addressSelectionDialog.isShowing()) {
            addressSelectionDialog.dismiss();
        }
        if (addressLookupExecutor != null) {
            addressLookupExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    private void configurePrimaryActionButton(Button button) {
        button.setBackgroundResource(R.drawable.bg_button_register);
        clearButtonTint(button);
        button.setTextColor(getColor(R.color.white));
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_button_customer_save_48,
                0,
                0,
                0
        );
        button.setCompoundDrawablePadding(
                getResources().getDimensionPixelSize(R.dimen.mkarte_form_button_icon_gap)
        );
    }

    private void clearButtonTint(Button button) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setCompoundDrawableTintList(null);
        }
    }

    private void loadCustomerForEdit() {
        customerRepository.get(customerId, customer -> {
            editingCustomer = customer;
            if (editingCustomer == null) {
                finish();
                return;
            }
            settingInitialCustomerData = true;
            name.setText(editingCustomer.name);
            kana.setText(editingCustomer.kana);
            phone.setText(editingCustomer.phone);
            postalCode.setText(editingCustomer.postalCode);
            address.setText(editingCustomer.address);
            memo.setText(editingCustomer.memo);
            settingInitialCustomerData = false;
        });
    }

    private void setupPostalCodeAddressLookup() {
        postalCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handlePostalCodeChanged(s == null ? "" : s.toString());
            }
        });
    }

    private void handlePostalCodeChanged(String input) {
        if (settingInitialCustomerData) {
            return;
        }

        String normalizedPostalCode = normalizePostalCode(input);
        if (!normalizedPostalCode.matches("\\d{7}")) {
            lastRequestedPostalCode = "";
            return;
        }

        if (normalizedPostalCode.equals(lastRequestedPostalCode)
                || inFlightPostalCodes.contains(normalizedPostalCode)) {
            return;
        }

        lastRequestedPostalCode = normalizedPostalCode;
        inFlightPostalCodes.add(normalizedPostalCode);
        requestAddressForPostalCode(normalizedPostalCode);
    }

    private String normalizePostalCode(String input) {
        String trimmed = input == null ? "" : input.trim();
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char value = trimmed.charAt(i);
            if (value >= '０' && value <= '９') {
                normalized.append((char) ('0' + (value - '０')));
            } else if (value == '-' || value == '－') {
                continue;
            } else {
                normalized.append(value);
            }
        }
        return normalized.toString();
    }

    private void requestAddressForPostalCode(String normalizedPostalCode) {
        if (addressLookupExecutor == null || addressLookupExecutor.isShutdown()) {
            return;
        }

        addressLookupExecutor.execute(() -> {
            try {
                ZipCloudAddressClient.SearchResult result =
                        zipCloudAddressClient.search(normalizedPostalCode);
                runOnUiThread(() -> handleAddressLookupSuccess(normalizedPostalCode, result));
            } catch (Exception e) {
                Log.w(TAG, "ZipCloud address lookup failed", e);
                runOnUiThread(() -> handleAddressLookupError(normalizedPostalCode));
            }
        });
    }

    private void handleAddressLookupSuccess(
            String requestedPostalCode,
            ZipCloudAddressClient.SearchResult result
    ) {
        markAddressLookupFinished(requestedPostalCode);
        if (!canApplyAddressLookupResult(requestedPostalCode)) {
            return;
        }

        if (!result.hasCandidates()) {
            Toast.makeText(this, ADDRESS_NOT_FOUND_MESSAGE, Toast.LENGTH_SHORT).show();
            return;
        }

        List<ZipCloudAddressClient.AddressCandidate> candidates = result.getCandidates();
        if (candidates.size() == 1) {
            address.setText(candidates.get(0).getAddress());
            return;
        }

        showAddressSelectionDialog(requestedPostalCode, candidates);
    }

    private void handleAddressLookupError(String requestedPostalCode) {
        markAddressLookupFinished(requestedPostalCode);
        if (!canApplyAddressLookupResult(requestedPostalCode)) {
            return;
        }
        Toast.makeText(this, ADDRESS_LOOKUP_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
    }

    private void markAddressLookupFinished(String requestedPostalCode) {
        inFlightPostalCodes.remove(requestedPostalCode);
    }

    private boolean canApplyAddressLookupResult(String requestedPostalCode) {
        if (activityDestroyed || isFinishing() || isDestroyed()) {
            return false;
        }
        return requestedPostalCode.equals(normalizePostalCode(postalCode.getText().toString()));
    }

    private void showAddressSelectionDialog(
            String requestedPostalCode,
            List<ZipCloudAddressClient.AddressCandidate> candidates
    ) {
        if (addressSelectionDialog != null && addressSelectionDialog.isShowing()) {
            addressSelectionDialog.dismiss();
        }

        String[] addresses = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            addresses[i] = candidates.get(i).getAddress();
        }

        addressSelectionDialog = new AlertDialog.Builder(this)
                .setTitle("住所を選択")
                .setItems(addresses, (dialog, which) -> {
                    if (canApplyAddressLookupResult(requestedPostalCode)) {
                        address.setText(addresses[which]);
                    }
                })
                .create();
        addressSelectionDialog.setOnDismissListener(dialog -> addressSelectionDialog = null);
        addressSelectionDialog.show();
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
