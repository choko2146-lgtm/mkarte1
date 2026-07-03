package com.example.mkarte1.ui.other;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mkarte1.R;
import com.example.mkarte1.repository.CustomerRepository;
import com.example.mkarte1.ui.MkarteBottomNav;
import com.example.mkarte1.util.CsvShareUtil;
import com.example.mkarte1.util.CustomerAddressCsvUtil;

import java.io.File;
import java.io.IOException;

public class OtherActivity extends AppCompatActivity {
    private static final String TAG = "OtherActivity";

    private CustomerRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        MkarteBottomNav.bind(this, R.id.navMore);
        repository = new CustomerRepository(this);

        setupRow(R.id.rowExportAddressCsv, R.drawable.ic_export_24, "住所録CSV出力", true,
                v -> exportAddressCsv());
        setupRow(R.id.rowBackup, R.drawable.ic_backup_24, "バックアップ・復元", false,
                v -> showNotImplemented());
        setupRow(R.id.rowDataExport, R.drawable.ic_export_24, "データ出力", false,
                v -> showNotImplemented());
        setupRow(R.id.rowAppInfo, R.drawable.ic_info_24, "アプリ情報", false,
                v -> showNotImplemented());
        setupRow(R.id.rowSettings, R.drawable.ic_settings_24, "設定", false,
                v -> showNotImplemented());
        setupRow(R.id.rowHelp, R.drawable.ic_help_24, "ヘルプ・お問い合わせ", false,
                v -> showNotImplemented());
    }

    private void setupRow(int rowId, int iconId, String title, boolean primary, View.OnClickListener listener) {
        View row = findViewById(rowId);
        ((ImageView) row.findViewById(R.id.imageOtherIcon)).setImageResource(iconId);
        TextView titleView = row.findViewById(R.id.textOtherTitle);
        titleView.setText(title);
        row.setAlpha(primary ? 1f : 0.72f);
        row.setOnClickListener(listener);
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

    private void showNotImplemented() {
        Toast.makeText(this, "未実装です", Toast.LENGTH_SHORT).show();
    }
}
