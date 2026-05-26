package com.example.mkarte1.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.repository.CustomerRepository;

public class CustomerListActivity extends AppCompatActivity {
    private CustomerRepository repository;
    private CustomerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        repository = new CustomerRepository(this);
        adapter = new CustomerAdapter(customer -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("customerId", customer.id);
            startActivity(intent);
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerCustomers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.editSearch).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> load(""));
        ((android.widget.EditText) findViewById(R.id.editSearch)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { load(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(((android.widget.EditText) findViewById(R.id.editSearch)).getText().toString());
    }

    private void load(String query) {
        repository.list(query, adapter::submit);
    }
}
