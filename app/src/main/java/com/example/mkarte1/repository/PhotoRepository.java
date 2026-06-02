package com.example.mkarte1.repository;

import android.content.Context;

import com.example.mkarte1.data.AppDatabase;
import com.example.mkarte1.data.Photo;

import java.util.List;

public class PhotoRepository {
    private final AppDatabase db;

    public PhotoRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public interface Callback<T> {
        void onResult(T value);
    }

    public void insert(Photo photo, Callback<Long> callback) {
        DbExecutor.IO.execute(() -> {
            long id = db.photoDao().insert(photo);
            DbExecutor.MAIN.post(() -> callback.onResult(id));
        });
    }

    public void update(Photo photo, Runnable callback) {
        DbExecutor.IO.execute(() -> {
            db.photoDao().update(photo);
            DbExecutor.MAIN.post(callback);
        });
    }

    public void get(long id, Callback<Photo> callback) {
        DbExecutor.IO.execute(() -> {
            Photo photo = db.photoDao().getById(id);
            DbExecutor.MAIN.post(() -> callback.onResult(photo));
        });
    }

    public void listAll(Callback<List<Photo>> callback) {
        DbExecutor.IO.execute(() -> {
            List<Photo> photos = db.photoDao().getAllPhotos();
            DbExecutor.MAIN.post(() -> callback.onResult(photos));
        });
    }

    public void listForCustomer(long customerId, Callback<List<Photo>> callback) {
        DbExecutor.IO.execute(() -> {
            List<Photo> photos = db.photoDao().getByCustomerId(customerId);
            DbExecutor.MAIN.post(() -> callback.onResult(photos));
        });
    }

    public void listForDate(String takenDate, Callback<List<Photo>> callback) {
        DbExecutor.IO.execute(() -> {
            List<Photo> photos = db.photoDao().getByTakenDate(takenDate);
            DbExecutor.MAIN.post(() -> callback.onResult(photos));
        });
    }

    public void listTakenDates(Callback<List<String>> callback) {
        DbExecutor.IO.execute(() -> {
            List<String> takenDates = db.photoDao().getDistinctTakenDates();
            DbExecutor.MAIN.post(() -> callback.onResult(takenDates));
        });
    }

    public void delete(Photo photo, Runnable callback) {
        DbExecutor.IO.execute(() -> {
            db.photoDao().delete(photo);
            DbExecutor.MAIN.post(callback);
        });
    }

    public void updateCustomerNameForCustomer(long customerId, String customerName, Runnable callback) {
        DbExecutor.IO.execute(() -> {
            db.photoDao().updateCustomerNameByCustomerId(customerId, customerName);
            DbExecutor.MAIN.post(callback);
        });
    }
}
