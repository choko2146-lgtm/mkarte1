package com.example.mkarte1.data;

import androidx.room.Embedded;

public class CustomerWithLatestDate {
    @Embedded
    public Customer customer;

    public String latestTakenDate;
}
