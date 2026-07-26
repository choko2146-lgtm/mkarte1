package com.example.mkarte1.util.calendar;

public final class JapaneseLunarDate {
    private final int year;
    private final int month;
    private final int day;
    private final boolean leapMonth;

    JapaneseLunarDate(int year, int month, int day, boolean leapMonth) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.leapMonth = leapMonth;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public boolean isLeapMonth() {
        return leapMonth;
    }
}
