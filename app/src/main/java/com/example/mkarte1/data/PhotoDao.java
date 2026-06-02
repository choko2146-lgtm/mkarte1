package com.example.mkarte1.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert
    long insert(Photo photo);

    @Update
    void update(Photo photo);

    @Delete
    void delete(Photo photo);

    @Query("SELECT * FROM photos WHERE id = :id")
    Photo getById(long id);

    @Query("SELECT * FROM photos ORDER BY takenDate DESC, createdAt DESC")
    List<Photo> getAllPhotos();

    @Query("SELECT * FROM photos WHERE customerId = :customerId ORDER BY takenDate DESC, createdAt DESC")
    List<Photo> getByCustomerId(long customerId);

    @Query("SELECT * FROM photos WHERE takenDate = :takenDate ORDER BY customerName ASC, createdAt DESC")
    List<Photo> getByTakenDate(String takenDate);

    @Query("SELECT DISTINCT takenDate FROM photos WHERE takenDate IS NOT NULL AND takenDate != '' ORDER BY takenDate DESC")
    List<String> getDistinctTakenDates();

    @Query("SELECT COUNT(*) FROM photos WHERE customerId = :customerId")
    int countByCustomerId(long customerId);

    @Query("SELECT MAX(takenDate) FROM photos WHERE customerId = :customerId")
    String latestTakenDate(long customerId);

    @Query("UPDATE photos SET customerName = :customerName WHERE customerId = :customerId")
    void updateCustomerNameByCustomerId(long customerId, String customerName);
}
