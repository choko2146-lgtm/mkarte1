package com.example.mkarte1.util.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RokuyoCalculatorTest {
    private final RokuyoCalculator calculator = new RokuyoCalculator();
    private final JapaneseLunarCalendarConverter converter = new JapaneseLunarCalendarConverter();

    @Test
    public void calculatesAllSixRokuyoNames() {
        Set<String> names = new HashSet<>();
        for (int day = 1; day <= 6; day++) {
            names.add(calculator.getRokuyo(LocalDate.of(2026, 1, day)));
        }

        assertTrue(names.contains("先勝"));
        assertTrue(names.contains("友引"));
        assertTrue(names.contains("先負"));
        assertTrue(names.contains("仏滅"));
        assertTrue(names.contains("大安"));
        assertTrue(names.contains("赤口"));
    }

    @Test
    public void matchesKnownRokuyoValuesInJanuary2026() {
        JapaneseLunarDate newYearDate = converter.convert(LocalDate.of(2026, 1, 1));
        assertNotNull(newYearDate);
        assertEquals(2025, newYearDate.getYear());
        assertEquals(11, newYearDate.getMonth());
        assertEquals(13, newYearDate.getDay());

        assertEquals("大安", calculator.getRokuyo(LocalDate.of(2026, 1, 1)));
        assertEquals("赤口", calculator.getRokuyo(LocalDate.of(2026, 1, 2)));
        assertEquals("先勝", calculator.getRokuyo(LocalDate.of(2026, 1, 3)));
        assertEquals("友引", calculator.getRokuyo(LocalDate.of(2026, 1, 4)));
        assertEquals("先負", calculator.getRokuyo(LocalDate.of(2026, 1, 5)));
        assertEquals("仏滅", calculator.getRokuyo(LocalDate.of(2026, 1, 6)));
    }

    @Test
    public void matchesKnownLunarMonthStartInEarly2026() {
        JapaneseLunarDate twelfthMonth = converter.convert(LocalDate.of(2026, 1, 19));
        JapaneseLunarDate firstMonth = converter.convert(LocalDate.of(2026, 2, 17));

        assertNotNull(twelfthMonth);
        assertNotNull(firstMonth);
        assertEquals(2025, twelfthMonth.getYear());
        assertEquals(12, twelfthMonth.getMonth());
        assertEquals(1, twelfthMonth.getDay());
        assertEquals("赤口", calculator.getRokuyo(LocalDate.of(2026, 1, 19)));
        assertEquals(2026, firstMonth.getYear());
        assertEquals(1, firstMonth.getMonth());
        assertEquals(1, firstMonth.getDay());
        assertEquals("先勝", calculator.getRokuyo(LocalDate.of(2026, 2, 17)));
    }

    @Test
    public void resetsRokuyoAtLunarMonthBoundary() {
        JapaneseLunarDate julyBoundary = converter.convert(LocalDate.of(2026, 7, 14));
        assertNotNull(julyBoundary);
        assertEquals(6, julyBoundary.getMonth());
        assertEquals(1, julyBoundary.getDay());
        assertEquals("赤口", calculator.getRokuyo(LocalDate.of(2026, 7, 14)));

        JapaneseLunarDate augustBoundary = converter.convert(LocalDate.of(2026, 8, 13));
        assertNotNull(augustBoundary);
        assertEquals(7, augustBoundary.getMonth());
        assertEquals(1, augustBoundary.getDay());
        assertEquals("先勝", calculator.getRokuyo(LocalDate.of(2026, 8, 13)));
    }

    @Test
    public void matchesDisplayRangePastKnownValues() {
        JapaneseLunarDate date = converter.convert(LocalDate.of(2006, 7, 1));
        assertNotNull(date);
        assertEquals(6, date.getMonth());
        assertEquals(6, date.getDay());
        assertEquals("大安", calculator.getRokuyo(LocalDate.of(2006, 7, 1)));
        assertEquals("先勝", calculator.getRokuyo(LocalDate.of(2006, 7, 25)));
    }

    @Test
    public void matchesDisplayRangeFutureKnownValues() {
        assertEquals("先負", calculator.getRokuyo(LocalDate.of(2036, 7, 21)));
        assertEquals("赤口", calculator.getRokuyo(LocalDate.of(2036, 7, 23)));
    }

    @Test
    public void handlesCurrentRangeLunarBoundaryAround2033() {
        JapaneseLunarDate beforeBoundary = converter.convert(LocalDate.of(2033, 11, 21));
        JapaneseLunarDate boundary = converter.convert(LocalDate.of(2033, 11, 22));

        assertNotNull(beforeBoundary);
        assertNotNull(boundary);
        assertEquals(10, beforeBoundary.getMonth());
        assertEquals(30, beforeBoundary.getDay());
        assertEquals(11, boundary.getMonth());
        assertEquals(1, boundary.getDay());
        assertEquals("先負", calculator.getRokuyo(LocalDate.of(2033, 11, 21)));
        assertEquals("大安", calculator.getRokuyo(LocalDate.of(2033, 11, 22)));
    }
}
