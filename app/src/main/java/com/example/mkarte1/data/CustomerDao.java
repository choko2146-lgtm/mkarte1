package com.example.mkarte1.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomerDao {
    @Insert
    long insert(Customer customer);

    @Update
    void update(Customer customer);

    @Delete
    void delete(Customer customer);

    @Query("SELECT * FROM customers WHERE id = :id")
    Customer getById(long id);

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    List<Customer> getAll();

    @Query("SELECT c.* FROM customers c LEFT JOIN photos p ON p.customerId = c.id " +
            "GROUP BY c.id ORDER BY COALESCE(MAX(p.takenDate), '') DESC, c.updatedAt DESC")
    List<Customer> getAllOrderByLatestPhoto();

    @Query("SELECT c.*, MAX(p.takenDate) AS latestTakenDate FROM customers c LEFT JOIN photos p ON p.customerId = c.id " +
            "GROUP BY c.id ORDER BY COALESCE(MAX(p.takenDate), '') DESC, c.updatedAt DESC")
    List<CustomerWithLatestDate> getAllWithLatestDate();

    @Query("SELECT c.* FROM customers c LEFT JOIN photos p ON p.customerId = c.id " +
            "WHERE c.name LIKE '%' || :query || '%' OR c.kana LIKE '%' || :query || '%' OR c.phone LIKE '%' || :query || '%' " +
            "GROUP BY c.id ORDER BY COALESCE(MAX(p.takenDate), '') DESC, c.updatedAt DESC")
    List<Customer> search(String query);

    @Query("SELECT c.*, MAX(p.takenDate) AS latestTakenDate FROM customers c LEFT JOIN photos p ON p.customerId = c.id " +
            "WHERE c.name LIKE '%' || :query || '%' OR c.kana LIKE '%' || :query || '%' OR c.phone LIKE '%' || :query || '%' " +
            "GROUP BY c.id ORDER BY COALESCE(MAX(p.takenDate), '') DESC, c.updatedAt DESC")
    List<CustomerWithLatestDate> searchWithLatestDate(String query);

    @Query("SELECT name, postalCode, address FROM customers " +
            "WHERE address IS NOT NULL AND TRIM(address) != '' " +
            "ORDER BY name")
    List<CustomerAddressExport> getCustomerAddressList();
}
