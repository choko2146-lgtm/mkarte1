package com.example.mkarte1.util.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class JapaneseHolidayCalculator {
    private static final String HOLIDAY_OFF = "休日";

    private final Map<Integer, Map<LocalDate, String>> cacheByYear = new HashMap<>();

    public boolean isHoliday(LocalDate date) {
        return getHolidayName(date) != null;
    }

    public String getHolidayName(LocalDate date) {
        if (date == null) {
            return null;
        }
        return holidaysForYear(date.getYear()).get(date);
    }

    private Map<LocalDate, String> holidaysForYear(int year) {
        Map<LocalDate, String> cached = cacheByYear.get(year);
        if (cached != null) {
            return cached;
        }

        Map<LocalDate, String> baseHolidays = new TreeMap<>();
        addBaseHolidays(year, baseHolidays);

        Map<LocalDate, String> holidays = new TreeMap<>(baseHolidays);
        addCitizenHolidays(year, baseHolidays, holidays);
        addSubstituteHolidays(baseHolidays, holidays);

        Map<LocalDate, String> unmodifiable = Collections.unmodifiableMap(holidays);
        cacheByYear.put(year, unmodifiable);
        return unmodifiable;
    }

    private void addBaseHolidays(int year, Map<LocalDate, String> holidays) {
        add(holidays, year, 1, 1, "元日");
        add(holidays, nthMonday(year, 1, 2), "成人の日");
        add(holidays, year, 2, 11, "建国記念の日");

        if (year >= 2020) {
            add(holidays, year, 2, 23, "天皇誕生日");
        } else if (year <= 2018) {
            add(holidays, year, 12, 23, "天皇誕生日");
        }

        add(holidays, year, 3, springEquinoxDay(year), "春分の日");
        add(holidays, year, 4, 29, year >= 2007 ? "昭和の日" : "みどりの日");
        add(holidays, year, 5, 3, "憲法記念日");
        if (year >= 2007) {
            add(holidays, year, 5, 4, "みどりの日");
        }
        add(holidays, year, 5, 5, "こどもの日");

        addMarineDay(year, holidays);
        addMountainDay(year, holidays);
        add(holidays, nthMonday(year, 9, 3), "敬老の日");
        add(holidays, year, 9, autumnEquinoxDay(year), "秋分の日");
        addSportsDay(year, holidays);
        add(holidays, year, 11, 3, "文化の日");
        add(holidays, year, 11, 23, "勤労感謝の日");
        addOneTimeHolidays(year, holidays);
    }

    private void addMarineDay(int year, Map<LocalDate, String> holidays) {
        if (year == 2020) {
            add(holidays, year, 7, 23, "海の日");
        } else if (year == 2021) {
            add(holidays, year, 7, 22, "海の日");
        } else {
            add(holidays, nthMonday(year, 7, 3), "海の日");
        }
    }

    private void addMountainDay(int year, Map<LocalDate, String> holidays) {
        if (year < 2016) {
            return;
        }
        if (year == 2020) {
            add(holidays, year, 8, 10, "山の日");
        } else if (year == 2021) {
            add(holidays, year, 8, 8, "山の日");
        } else {
            add(holidays, year, 8, 11, "山の日");
        }
    }

    private void addSportsDay(int year, Map<LocalDate, String> holidays) {
        if (year == 2020) {
            add(holidays, year, 7, 24, "スポーツの日");
        } else if (year == 2021) {
            add(holidays, year, 7, 23, "スポーツの日");
        } else if (year >= 2022) {
            add(holidays, nthMonday(year, 10, 2), "スポーツの日");
        } else {
            add(holidays, nthMonday(year, 10, 2), "体育の日");
        }
    }

    private void addOneTimeHolidays(int year, Map<LocalDate, String> holidays) {
        if (year == 2019) {
            add(holidays, year, 5, 1, "天皇の即位の日");
            add(holidays, year, 10, 22, "即位礼正殿の儀");
        }
    }

    private void addCitizenHolidays(
            int year,
            Map<LocalDate, String> baseHolidays,
            Map<LocalDate, String> holidays
    ) {
        LocalDate date = LocalDate.of(year, 1, 2);
        LocalDate endDate = LocalDate.of(year, 12, 30);
        while (!date.isAfter(endDate)) {
            if (!holidays.containsKey(date)
                    && baseHolidays.containsKey(date.minusDays(1))
                    && baseHolidays.containsKey(date.plusDays(1))) {
                holidays.put(date, HOLIDAY_OFF);
            }
            date = date.plusDays(1);
        }
    }

    private void addSubstituteHolidays(
            Map<LocalDate, String> baseHolidays,
            Map<LocalDate, String> holidays
    ) {
        for (LocalDate holiday : new ArrayList<>(baseHolidays.keySet())) {
            if (holiday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                continue;
            }
            LocalDate substitute = holiday.plusDays(1);
            while (holidays.containsKey(substitute)) {
                substitute = substitute.plusDays(1);
            }
            holidays.put(substitute, HOLIDAY_OFF);
        }
    }

    private LocalDate nthMonday(int year, int month, int nth) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysUntilMonday = DayOfWeek.MONDAY.getValue() - firstDay.getDayOfWeek().getValue();
        if (daysUntilMonday < 0) {
            daysUntilMonday += 7;
        }
        return firstDay.plusDays(daysUntilMonday + (long) (nth - 1) * 7);
    }

    private int springEquinoxDay(int year) {
        return (int) Math.floor(20.8431 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }

    private int autumnEquinoxDay(int year) {
        return (int) Math.floor(23.2488 + 0.242194 * (year - 1980) - Math.floor((year - 1980) / 4.0));
    }

    private void add(Map<LocalDate, String> holidays, int year, int month, int day, String name) {
        add(holidays, LocalDate.of(year, month, day), name);
    }

    private void add(Map<LocalDate, String> holidays, LocalDate date, String name) {
        holidays.put(date, name);
    }
}
