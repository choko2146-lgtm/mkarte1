package com.example.mkarte1.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "photos",
        foreignKeys = @ForeignKey(
                entity = Customer.class,
                parentColumns = "id",
                childColumns = "customerId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("customerId")
)
public class Photo {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long customerId;
    public String customerName;
    public String takenDate;
    public String fileName;
    public String uri;
    public String memo;
    public long createdAt;
}
