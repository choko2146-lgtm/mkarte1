# Step20B Rokuyo And Japanese Holiday Implementation

Updated: 2026-07-26

## Scope

- Calendar screen remains implemented in Java.
- Kizitonwose Calendar View remains `2.10.1`.
- No Kotlin files, Compose, DB schema changes, Entity changes, DAO changes, Migration, or external runtime API calls were added.
- Rokuyo and Japanese holiday calculation are implemented as local Java helper classes.
- Holiday names are used for date text color and accessibility text only. Holiday names are not displayed inside each calendar cell in this step.

## Rokuyo

Implementation files:

- `JapaneseLunarDate.java`
- `JapaneseLunarCalendarConverter.java`
- `Rokuyo.java`
- `RokuyoCalculator.java`

The calendar converts a Gregorian `LocalDate` to a Japanese lunisolar date using an internal table for 1900-2100, then calculates Rokuyo with:

```text
(lunarMonth + lunarDay) % 6
```

Mapping:

- `0`: 大安
- `1`: 赤口
- `2`: 先勝
- `3`: 友引
- `4`: 先負
- `5`: 仏滅

The calculator caches results for the activity lifetime. If a date cannot be converted, it returns an empty display string instead of crashing.

Notes:

- Rokuyo has no modern official government-maintained calendar source.
- The old-calendar table is an internal approximation/convention for the app display range.
- Some public sources differ around exceptional old-calendar handling, especially around 2033. The current implementation adopts the internal table convention and documents this as a future review point if stricter almanac compatibility becomes necessary.

## Japanese Holidays

Implementation file:

- `JapaneseHolidayCalculator.java`

Supported rules:

- Fixed-date national holidays
- Happy Monday holidays
- Spring and autumn equinox calculations
- Substitute holidays
- Citizen's holidays
- Law/name changes for display-range years
- 2019 imperial-transition one-time holidays
- 2020/2021 Olympic-year holiday moves

Display behavior:

- Holiday dates use the same red text priority as Sundays.
- If a date is both Saturday and a holiday, holiday red takes priority.
- Holiday names are included in `contentDescription`.
- No holiday label is rendered inside the cell in this step.

Future note:

- Future holiday law changes and officially announced equinox dates may require app updates.

## Calendar Cell Layout

Each date cell is arranged vertically:

```text
date number
rokuyo
visit dot
```

State priority:

- Selected date background has priority.
- Today background is used when the date is today and not selected.
- Visit dot remains independent from selected/today/holiday state.
- Date text color priority is holiday, Sunday, Saturday, normal weekday.

Binder safety:

- Reused cells reset date text, Rokuyo text, text colors, backgrounds, clickability, content description, and dot visibility on every bind.
- Out-of-month cells hide date number, Rokuyo, and dot and are not clickable.

## Test Results

Command:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest --console=plain --no-daemon
```

Result:

- `BUILD SUCCESSFUL`

Representative test coverage:

- All six Rokuyo names
- Lunar month boundary resets
- 2026, 2006, 2033, and 2036 representative dates
- Fixed holidays
- Happy Monday holidays
- Equinox holidays
- Substitute holidays
- Citizen's holidays
- Saturday/Sunday holiday overlap
- 2019, 2020, and 2021 special holiday rules

## Build Results

Command:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug --console=plain --no-daemon
```

Result:

- `BUILD SUCCESSFUL`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Galaxy SC-03L Check

Target:

- Device ID: `RF8M50DL2NA`
- Manufacturer: `samsung`
- Model: `SC-03L`
- Android: `12`
- SDK: `31`
- Size: physical `1440x3040`, override `1080x2280`
- Density: `420`

ADB:

- `adb devices` showed `RF8M50DL2NA    device`.

Install:

- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Result: `Success`
- Existing app data was preserved.

Launch:

- MainActivity launched successfully.
- Calendar screen focus confirmed as `com.example.mkarte1/.ui.calendar.CalendarActivity`.

Screenshots were saved under:

```text
%TEMP%\mkarte1-calendar-step20b-check
```

Confirmed on screenshots:

- Initial July 2026 calendar display
- Sunday-to-Saturday 7 columns
- No missing in-month dates
- Month title
- Previous month button
- Next month button
- Month swipe
- Date selection and selected background movement
- Today background after selecting a different date
- Sunday red
- Saturday blue
- Holiday red, including 2026-07-20 and 2026-08-11
- Rokuyo visible for each in-month date
- Rokuyo not clipped after the layout adjustment
- 5-week month display
- 6-week month display
- 6th-row date selection
- No overlap between the calendar, history card, and bottom navigation

Confirmed within available existing data:

- The checked months did not show visible visit dots or visit history rows.
- No customer/photo records were created, edited, deleted, or initialized.
- Customer detail navigation from visit history was not confirmed because no selectable visit history row was visible in the checked states.

Logcat:

- No app crash or exception stack was found.
- Only Samsung/OS diagnostic lines such as `USNET` and `Binder ioctl` appeared in the filtered output.

## Issue Found And Fixed

Issue:

- Initial real-device screenshot after adding Rokuyo showed the Rokuyo line clipped vertically.

Fix:

- Added a fixed Rokuyo line height.
- Kept font padding enabled for the Rokuyo TextView.
- Reserved visit-dot space with `INVISIBLE` instead of `GONE` to avoid row shifts.

Files:

- `app/src/main/res/layout/item_calendar_day.xml`
- `app/src/main/res/values/dimens.xml`

Recheck:

- Unit tests passed.
- `assembleDebug` passed.
- APK was reinstalled.
- Galaxy screenshot confirmed the Rokuyo text is no longer clipped.

## References

- Cabinet Office, National Holidays: https://www8.cao.go.jp/chosei/shukujitsu/gaiyou.html
- National Astronomical Observatory of Japan, Koyomi Yoko: https://eco.mtk.nao.ac.jp/koyomi/yoko/
- National Astronomical Observatory of Japan, Old Calendar FAQ: https://www.nao.ac.jp/faq/a0304.html
- National Diet Library, Rokuyo: https://www.ndl.go.jp/koyomi/chapter3/s3.html
- Rokuyo calculation reference: https://rokuyo.org/rokuyo/calculation.html
