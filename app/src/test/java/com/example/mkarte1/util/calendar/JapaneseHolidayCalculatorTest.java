package com.example.mkarte1.util.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class JapaneseHolidayCalculatorTest {
    private final JapaneseHolidayCalculator calculator = new JapaneseHolidayCalculator();

    @Test
    public void handlesFixedDateHolidays() {
        assertEquals("元日", calculator.getHolidayName(LocalDate.of(2026, 1, 1)));
        assertEquals("建国記念の日", calculator.getHolidayName(LocalDate.of(2026, 2, 11)));
        assertEquals("文化の日", calculator.getHolidayName(LocalDate.of(2026, 11, 3)));
    }

    @Test
    public void handlesNthMondayHolidays() {
        assertEquals("成人の日", calculator.getHolidayName(LocalDate.of(2026, 1, 12)));
        assertEquals("海の日", calculator.getHolidayName(LocalDate.of(2026, 7, 20)));
        assertEquals("敬老の日", calculator.getHolidayName(LocalDate.of(2026, 9, 21)));
        assertEquals("スポーツの日", calculator.getHolidayName(LocalDate.of(2026, 10, 12)));
    }

    @Test
    public void handlesEquinoxHolidays() {
        assertEquals("春分の日", calculator.getHolidayName(LocalDate.of(2026, 3, 20)));
        assertEquals("春分の日", calculator.getHolidayName(LocalDate.of(2027, 3, 21)));
        assertEquals("秋分の日", calculator.getHolidayName(LocalDate.of(2026, 9, 23)));
        assertEquals("秋分の日", calculator.getHolidayName(LocalDate.of(2036, 9, 22)));
    }

    @Test
    public void handlesSubstituteHolidays() {
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2026, 5, 6)));
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2027, 3, 22)));
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2021, 8, 9)));
    }

    @Test
    public void handlesCitizenHolidays() {
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2015, 9, 22)));
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2026, 9, 22)));
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2019, 4, 30)));
        assertEquals("休日", calculator.getHolidayName(LocalDate.of(2019, 5, 2)));
    }

    @Test
    public void handlesSaturdayAndSundayHolidays() {
        assertEquals("秋分の日", calculator.getHolidayName(LocalDate.of(2006, 9, 23)));
        assertEquals("憲法記念日", calculator.getHolidayName(LocalDate.of(2026, 5, 3)));
        assertTrue(calculator.isHoliday(LocalDate.of(2006, 9, 23)));
        assertTrue(calculator.isHoliday(LocalDate.of(2026, 5, 3)));
    }

    @Test
    public void handlesLawAndNameChanges() {
        assertEquals("みどりの日", calculator.getHolidayName(LocalDate.of(2006, 4, 29)));
        assertEquals("昭和の日", calculator.getHolidayName(LocalDate.of(2007, 4, 29)));
        assertEquals("天皇誕生日", calculator.getHolidayName(LocalDate.of(2018, 12, 23)));
        assertNull(calculator.getHolidayName(LocalDate.of(2019, 12, 23)));
        assertEquals("天皇誕生日", calculator.getHolidayName(LocalDate.of(2020, 2, 23)));
    }

    @Test
    public void handlesTemporarySpecialHolidays() {
        assertEquals("天皇の即位の日", calculator.getHolidayName(LocalDate.of(2019, 5, 1)));
        assertEquals("即位礼正殿の儀", calculator.getHolidayName(LocalDate.of(2019, 10, 22)));
        assertEquals("海の日", calculator.getHolidayName(LocalDate.of(2020, 7, 23)));
        assertEquals("スポーツの日", calculator.getHolidayName(LocalDate.of(2020, 7, 24)));
        assertEquals("山の日", calculator.getHolidayName(LocalDate.of(2020, 8, 10)));
        assertEquals("海の日", calculator.getHolidayName(LocalDate.of(2021, 7, 22)));
        assertEquals("スポーツの日", calculator.getHolidayName(LocalDate.of(2021, 7, 23)));
        assertEquals("山の日", calculator.getHolidayName(LocalDate.of(2021, 8, 8)));
    }

    @Test
    public void handlesDisplayRangeFutureSide() {
        assertEquals("海の日", calculator.getHolidayName(LocalDate.of(2036, 7, 21)));
        assertEquals("山の日", calculator.getHolidayName(LocalDate.of(2036, 8, 11)));
        assertEquals("スポーツの日", calculator.getHolidayName(LocalDate.of(2036, 10, 13)));
    }

    @Test
    public void ignoresRegularDays() {
        assertNull(calculator.getHolidayName(LocalDate.of(2026, 7, 21)));
        assertFalse(calculator.isHoliday(LocalDate.of(2026, 7, 21)));
    }
}
