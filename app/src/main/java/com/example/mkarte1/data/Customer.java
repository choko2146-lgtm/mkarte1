package com.example.mkarte1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String kana;
    public String phone;
    public String postalCode;
    public String address;
    public String memo;
    public long createdAt;
    public long updatedAt;
}
