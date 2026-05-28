package com.example.mkarte1.repository;

import android.content.Context;

import com.example.mkarte1.data.AppDatabase;
import com.example.mkarte1.data.Customer;
import com.example.mkarte1.data.CustomerAddressExport;

import java.util.List;

public class CustomerRepository {
    private final AppDatabase db;

    public CustomerRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public interface Callback<T> {
        void onResult(T value);
    }

    public void insert(Customer customer, Callback<Long> callback) {
        DbExecutor.IO.execute(() -> {
            long id = db.customerDao().insert(customer);
            DbExecutor.MAIN.post(() -> callback.onResult(id));
        });
    }

    public void update(Customer customer, Runnable callback) {
        DbExecutor.IO.execute(() -> {
            db.customerDao().update(customer);
            DbExecutor.MAIN.post(callback);
        });
    }

    public void get(long id, Callback<Customer> callback) {
        DbExecutor.IO.execute(() -> {
            Customer customer = db.customerDao().getById(id);
            DbExecutor.MAIN.post(() -> callback.onResult(customer));
        });
    }

    public void list(String query, Callback<List<Customer>> callback) {
        DbExecutor.IO.execute(() -> {
            List<Customer> customers = query == null || query.trim().isEmpty()
                    ? db.customerDao().getAllOrderByLatestPhoto()
                    : db.customerDao().search(query.trim());
            DbExecutor.MAIN.post(() -> callback.onResult(customers));
        });
    }

    public void listCustomerAddresses(Callback<List<CustomerAddressExport>> callback) {
        DbExecutor.IO.execute(() -> {
            List<CustomerAddressExport> customers = db.customerDao().getCustomerAddressList();
            DbExecutor.MAIN.post(() -> callback.onResult(customers));
        });
    }

    public void delete(Customer customer, Runnable callback) {
        DbExecutor.IO.execute(() -> {
            db.customerDao().delete(customer);
            DbExecutor.MAIN.post(callback);
        });
    }
}
