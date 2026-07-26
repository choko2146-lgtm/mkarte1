package com.example.mkarte1.util.calendar;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class RokuyoCalculator {
    private final JapaneseLunarCalendarConverter lunarCalendarConverter;
    private final Map<LocalDate, Rokuyo> cache = new HashMap<>();

    public RokuyoCalculator() {
        this(new JapaneseLunarCalendarConverter());
    }

    RokuyoCalculator(JapaneseLunarCalendarConverter lunarCalendarConverter) {
        this.lunarCalendarConverter = lunarCalendarConverter;
    }

    public String getRokuyo(LocalDate date) {
        Rokuyo rokuyo = calculate(date);
        return rokuyo != null ? rokuyo.getDisplayName() : "";
    }

    public Rokuyo calculate(LocalDate date) {
        if (date == null) {
            return null;
        }

        Rokuyo cached = cache.get(date);
        if (cached != null) {
            return cached;
        }

        Rokuyo calculated = calculateInternal(date);
        if (calculated != null) {
            cache.put(date, calculated);
        }
        return calculated;
    }

    private Rokuyo calculateInternal(LocalDate date) {
        JapaneseLunarDate lunarDate = lunarCalendarConverter.convert(date);
        if (lunarDate == null) {
            return null;
        }
        int remainder = (lunarDate.getMonth() + lunarDate.getDay()) % Rokuyo.values().length;
        return Rokuyo.fromRemainder(remainder);
    }
}
