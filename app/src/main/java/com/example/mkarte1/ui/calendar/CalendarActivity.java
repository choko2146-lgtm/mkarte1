package com.example.mkarte1.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.R;
import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.EdgeToEdgeUtil;
import com.example.mkarte1.ui.MkarteBottomNav;
import com.example.mkarte1.ui.customer.CustomerDetailActivity;
import com.example.mkarte1.util.calendar.JapaneseHolidayCalculator;
import com.example.mkarte1.util.calendar.RokuyoCalculator;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;

public class CalendarActivity extends AppCompatActivity {
    private static final DateTimeFormatter TAKEN_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int START_MONTH_PAST_YEARS = 20;
    private static final int END_MONTH_FUTURE_YEARS = 10;

    private PhotoRepository photoRepository;
    private VisitHistoryAdapter visitHistoryAdapter;
    private RecyclerView recyclerVisitHistories;
    private TextView textNoVisitHistory;
    private TextView textCalendarMonth;
    private TextView textVisitHistoryTitle;
    private ImageButton buttonPreviousMonth;
    private ImageButton buttonNextMonth;
    private CalendarView calendarView;
    private RokuyoCalculator rokuyoCalculator;
    private JapaneseHolidayCalculator holidayCalculator;

    private final Set<LocalDate> visitDates = new HashSet<>();
    private LocalDate todayDate;
    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private YearMonth startMonth;
    private YearMonth endMonth;
    private int normalTextColor;
    private int sundayTextColor;
    private int saturdayTextColor;
    private int disabledTextColor;
    private int rokuyoTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("カレンダー");
        setContentView(R.layout.activity_calendar);
        EdgeToEdgeUtil.apply(this);
        MkarteBottomNav.bind(this, R.id.navCalendar);

        todayDate = LocalDate.now();
        selectedDate = todayDate;
        currentMonth = YearMonth.from(todayDate);
        startMonth = currentMonth.minusYears(START_MONTH_PAST_YEARS);
        endMonth = currentMonth.plusYears(END_MONTH_FUTURE_YEARS);

        normalTextColor = getColor(R.color.mkarte_text);
        sundayTextColor = getColor(R.color.mkarte_calendar_sunday);
        saturdayTextColor = getColor(R.color.mkarte_calendar_saturday);
        disabledTextColor = getColor(R.color.mkarte_text_muted);
        rokuyoTextColor = getColor(R.color.mkarte_text_subtle);

        photoRepository = new PhotoRepository(this);
        rokuyoCalculator = new RokuyoCalculator();
        holidayCalculator = new JapaneseHolidayCalculator();
        visitHistoryAdapter = new VisitHistoryAdapter(visitHistory -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("customerId", visitHistory.customerId);
            startActivity(intent);
        });

        recyclerVisitHistories = findViewById(R.id.recyclerVisitHistories);
        textNoVisitHistory = findViewById(R.id.textNoVisitHistory);
        textCalendarMonth = findViewById(R.id.textCalendarMonth);
        textVisitHistoryTitle = findViewById(R.id.textVisitHistoryTitle);
        buttonPreviousMonth = findViewById(R.id.buttonPreviousMonth);
        buttonNextMonth = findViewById(R.id.buttonNextMonth);
        calendarView = findViewById(R.id.calendarView);

        recyclerVisitHistories.setLayoutManager(new LinearLayoutManager(this));
        recyclerVisitHistories.setAdapter(visitHistoryAdapter);

        setupCalendar();
        updateMonthTitle();
        updateVisitHistoryTitle();
        updateMonthButtonState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        todayDate = LocalDate.now();
        refreshVisitDates();
        loadVisitHistories(formatTakenDate(selectedDate));
        if (calendarView != null) {
            calendarView.notifyDateChanged(todayDate);
            calendarView.notifyDateChanged(selectedDate);
        }
    }

    private void setupCalendar() {
        buttonPreviousMonth.setOnClickListener(v -> moveMonth(-1));
        buttonNextMonth.setOnClickListener(v -> moveMonth(1));

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view, CalendarActivity.this::selectDate);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                bindDay(container, day);
            }
        });
        calendarView.setMonthScrollListener((CalendarMonth month) -> {
            currentMonth = month.getYearMonth();
            updateMonthTitle();
            updateMonthButtonState();
            return Unit.INSTANCE;
        });
        calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);
    }

    private void bindDay(DayViewContainer container, CalendarDay day) {
        container.day = day;
        container.root.setEnabled(false);
        container.root.setClickable(false);
        container.root.setAlpha(1f);
        container.root.setContentDescription(null);
        container.textDay.setText("");
        container.textDay.setVisibility(View.INVISIBLE);
        container.textDay.setTextColor(disabledTextColor);
        container.textDay.setBackground(null);
        container.textRokuyo.setText("");
        container.textRokuyo.setVisibility(View.INVISIBLE);
        container.textRokuyo.setTextColor(rokuyoTextColor);
        container.visitDot.setVisibility(View.INVISIBLE);

        if (day.getPosition() != DayPosition.MonthDate) {
            return;
        }

        LocalDate date = day.getDate();
        String rokuyoName = rokuyoCalculator.getRokuyo(date);
        String holidayName = holidayCalculator.getHolidayName(date);
        boolean hasVisitHistory = visitDates.contains(date);

        container.root.setEnabled(true);
        container.root.setClickable(true);
        container.root.setContentDescription(formatDateCellDescription(
                date, rokuyoName, holidayName, hasVisitHistory));
        container.textDay.setVisibility(View.VISIBLE);
        container.textDay.setText(String.valueOf(date.getDayOfMonth()));
        container.textDay.setTextColor(resolveDayTextColor(date, holidayName));
        container.textRokuyo.setText(rokuyoName);
        container.textRokuyo.setVisibility(rokuyoName.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        container.textRokuyo.setTextColor(rokuyoTextColor);

        if (date.equals(selectedDate)) {
            container.textDay.setBackgroundResource(R.drawable.bg_calendar_day_selected);
        } else if (date.equals(todayDate)) {
            container.textDay.setBackgroundResource(R.drawable.bg_calendar_day_today);
        }

        container.visitDot.setVisibility(hasVisitHistory ? View.VISIBLE : View.INVISIBLE);
    }

    private int resolveDayTextColor(LocalDate date, String holidayName) {
        if (holidayName != null && !holidayName.isEmpty()) {
            return sundayTextColor;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return sundayTextColor;
        }
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return saturdayTextColor;
        }
        return normalTextColor;
    }

    private String formatDateCellDescription(
            LocalDate date,
            String rokuyoName,
            String holidayName,
            boolean hasVisitHistory
    ) {
        StringBuilder description = new StringBuilder(formatDisplayDate(date));
        description.append(" ");
        description.append(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.JAPAN));
        if (holidayName != null && !holidayName.isEmpty()) {
            description.append(" ");
            description.append(holidayName);
        }
        if (rokuyoName != null && !rokuyoName.isEmpty()) {
            description.append(" ");
            description.append(rokuyoName);
        }
        description.append(hasVisitHistory ? " 来店記録あり" : " 来店記録なし");
        return description.toString();
    }

    private void moveMonth(int offset) {
        YearMonth targetMonth = currentMonth.plusMonths(offset);
        if (targetMonth.isBefore(startMonth) || targetMonth.isAfter(endMonth)) {
            updateMonthButtonState();
            return;
        }
        currentMonth = targetMonth;
        updateMonthTitle();
        updateMonthButtonState();
        calendarView.scrollToMonth(targetMonth);
    }

    private void selectDate(LocalDate date) {
        if (date == null) {
            return;
        }

        LocalDate oldSelectedDate = selectedDate;
        selectedDate = date;
        updateVisitHistoryTitle();

        if (oldSelectedDate != null && !oldSelectedDate.equals(date)) {
            calendarView.notifyDateChanged(oldSelectedDate);
        }
        calendarView.notifyDateChanged(date);
        loadVisitHistories(formatTakenDate(date));
    }

    private void refreshVisitDates() {
        photoRepository.listTakenDates(takenDates -> {
            visitDates.clear();
            if (takenDates != null) {
                for (String takenDate : takenDates) {
                    LocalDate parsedDate = parseTakenDate(takenDate);
                    if (parsedDate != null) {
                        visitDates.add(parsedDate);
                    }
                }
            }
            calendarView.notifyCalendarChanged();
        });
    }

    private LocalDate parseTakenDate(String takenDate) {
        if (takenDate == null || takenDate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(takenDate, TAKEN_DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String formatTakenDate(LocalDate date) {
        return TAKEN_DATE_FORMATTER.format(date);
    }

    private void updateMonthTitle() {
        textCalendarMonth.setText(String.format(Locale.JAPAN, "%d年%d月",
                currentMonth.getYear(), currentMonth.getMonthValue()));
    }

    private void updateVisitHistoryTitle() {
        textVisitHistoryTitle.setText("撮影履歴（" + formatDisplayDate(selectedDate) + "）");
    }

    private String formatDisplayDate(LocalDate date) {
        return String.format(Locale.JAPAN, "%d年%d月%d日",
                date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    private void updateMonthButtonState() {
        boolean canMovePrevious = !currentMonth.minusMonths(1).isBefore(startMonth);
        boolean canMoveNext = !currentMonth.plusMonths(1).isAfter(endMonth);
        buttonPreviousMonth.setEnabled(canMovePrevious);
        buttonPreviousMonth.setAlpha(canMovePrevious ? 1f : 0.35f);
        buttonNextMonth.setEnabled(canMoveNext);
        buttonNextMonth.setAlpha(canMoveNext ? 1f : 0.35f);
    }

    private void loadVisitHistories(String takenDate) {
        photoRepository.listForDate(takenDate, photos -> {
            List<VisitHistoryAdapter.VisitHistory> visitHistories = groupByCustomer(photos);
            visitHistoryAdapter.submit(visitHistories);

            boolean hasVisitHistory = !visitHistories.isEmpty();
            recyclerVisitHistories.setVisibility(hasVisitHistory ? View.VISIBLE : View.GONE);
            textNoVisitHistory.setVisibility(hasVisitHistory ? View.GONE : View.VISIBLE);
        });
    }

    private List<VisitHistoryAdapter.VisitHistory> groupByCustomer(List<Photo> photos) {
        Map<Long, CustomerPhotoCount> countsByCustomer = new LinkedHashMap<>();
        if (photos != null) {
            for (Photo photo : photos) {
                CustomerPhotoCount count = countsByCustomer.get(photo.customerId);
                if (count == null) {
                    count = new CustomerPhotoCount(resolveCustomerName(photo));
                    countsByCustomer.put(photo.customerId, count);
                }
                count.photoCount++;
            }
        }

        List<VisitHistoryAdapter.VisitHistory> visitHistories = new ArrayList<>();
        for (Map.Entry<Long, CustomerPhotoCount> entry : countsByCustomer.entrySet()) {
            CustomerPhotoCount count = entry.getValue();
            visitHistories.add(new VisitHistoryAdapter.VisitHistory(
                    entry.getKey(), count.customerName, count.photoCount));
        }
        return visitHistories;
    }

    private String resolveCustomerName(Photo photo) {
        if (photo.customerName == null || photo.customerName.trim().isEmpty()) {
            return "名前未設定";
        }
        return photo.customerName;
    }

    private interface OnDateClick {
        void onClick(LocalDate date);
    }

    private static class DayViewContainer extends ViewContainer {
        final View root;
        final TextView textDay;
        final TextView textRokuyo;
        final View visitDot;
        CalendarDay day;

        DayViewContainer(View view, OnDateClick onDateClick) {
            super(view);
            root = view;
            textDay = view.findViewById(R.id.textCalendarDayNumber);
            textRokuyo = view.findViewById(R.id.textCalendarRokuyo);
            visitDot = view.findViewById(R.id.viewCalendarVisitDot);
            view.setOnClickListener(v -> {
                CalendarDay clickedDay = day;
                if (clickedDay != null && clickedDay.getPosition() == DayPosition.MonthDate) {
                    onDateClick.onClick(clickedDay.getDate());
                }
            });
        }
    }

    private static class CustomerPhotoCount {
        final String customerName;
        int photoCount;

        CustomerPhotoCount(String customerName) {
            this.customerName = customerName;
        }
    }
}
