# Current Status

## Step22 Googleフォト・端末ギャラリーへの写真表示改善

Status:

- Implemented on 2026-07-27.
- Status: completed, build verified, user real-device confirmation OK.
- Scope was limited to `MediaStoreHelper` and docs.
- Room DB structure, Entity, DAO, Repository, existing photo paths, customer-photo links, folder names, file naming rules, UI, Manifest, Gradle, and external libraries were not changed.
- Pixel 6a real-device confirmation remains on hold and is not included in this Step completion.

Existing Photo Storage Checked:

- `CameraActivity` creates a temporary JPEG file with `PhotoFileUtil.createTempPhotoFile()`.
- Temporary and final app-owned files are stored under `getExternalFilesDir(Environment.DIRECTORY_PICTURES)`.
- The app-owned root folder is `Pictures/おかんのカルテ/` inside the app-specific external files area.
- After selecting an existing customer, `PhotoCustomerSelectActivity.link()` calls `PhotoFileUtil.moveTempToCustomer()`.
- After creating a new customer from a captured photo, `CustomerRegisterActivity.linkTempPhoto()` calls `PhotoFileUtil.moveTempToCustomer()`.
- Final app-owned files are saved under `Pictures/おかんのカルテ/<顧客名>/`.
- Final app-owned filenames keep the existing rule: `yyyyMMdd_<顧客名>.jpg`, with `_01`, `_02`, etc. only when needed for uniqueness.
- The DB keeps the app-owned file URI as `Photo.uri = Uri.fromFile(finalFile).toString()`.
- The DB also keeps `Photo.fileName`, `Photo.takenDate`, `Photo.customerId`, and `Photo.customerName`.
- The MediaStore URI returned by `MediaStoreHelper.copyToGallery()` is not saved to DB and is not used by in-app photo display.

Existing MediaStore Registration Checked:

- `MediaStoreHelper.copyToGallery()` is the shared MediaStore registration point.
- Android 10 and later use `ContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)` and copy JPEG bytes with `resolver.openOutputStream(galleryUri)`.
- The gallery copy destination is `RELATIVE_PATH = Pictures/Okannokarte`.
- Existing `DISPLAY_NAME` uses the saved filename and already includes `.jpg`.
- Existing `MIME_TYPE` is `image/jpeg`.
- Existing `IS_PENDING` was set to `1` before writing and updated to `0` after writing.
- Android 9 and earlier keep the existing fallback: `MediaScannerConnection.scanFile()` for the app-owned file.
- `MediaScannerConnection` is not used on Android 10 and later when the MediaStore copy succeeds.

Cause Investigation:

- Galaxy Gallery can display new photos because Android 10 and later already receive a public MediaStore copy under `Pictures/Okannokarte/`, with a JPEG MIME type and `IS_PENDING` cleared after write completion.
- User real-device confirmation showed Google Photos recognizes the local device folder under `コレクション → このデバイス上 → Okannokarte`.
- Google Photos main timeline and cloud-side display depend on Google Photos' own folder backup setting for `Okannokarte`.
- Android official reference describes `DATE_ADDED`, `DATE_MODIFIED`, and `DATE_TAKEN` as read-only MediaStore columns, so app-side `ContentValues` writes for these columns are not kept.
- No evidence was found that the current code writes an incorrect MIME type, removes the `.jpg` extension, uses a non-image `RELATIVE_PATH`, leaves `IS_PENDING = 1`, or stores the MediaStore URI in DB.

Fix:

- Added a guard so 0-byte source files are not registered or scanned as gallery photos.
- Removed the temporary `DATE_ADDED`, `DATE_MODIFIED`, and `DATE_TAKEN` `ContentValues` writes after confirming they are read-only in the Android official reference.
- Kept `DISPLAY_NAME`, `MIME_TYPE`, `RELATIVE_PATH`, `IS_PENDING` behavior, destination folder, file naming, and app-owned storage unchanged.
- MediaStore registration failure still returns `null`, logs a warning, deletes only the incomplete MediaStore row when one was created, and does not delete the app-owned saved photo.
- Existing photos are not re-registered; the fix applies to newly captured/saved photos after this Step.

Verification:

- `$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- No compile errors were reported.
- The build was run again after removing the read-only date column writes.
- No tests were added or run because this change is limited to Android framework MediaStore registration and no directly related local test existed.

User Real-device Confirmation:

- A newly captured photo displayed normally inside the app.
- The photo displayed normally in the Galaxy Gallery `Okannokarte` folder.
- The photo was not found in the main Google Photos photo list.
- The photo displayed normally in Google Photos under `コレクション → このデバイス上 → Okannokarte`.
- No duplicate photo display occurred.
- No 0-byte image occurred.
- No thumbnail loss occurred.
- No crash occurred.

Final Judgment:

- MediaStore registration is working normally.
- Google Photos local device-photo recognition is working normally.
- Google Photos main list and cloud backup behavior depend on the Google Photos folder backup setting for `Okannokarte`.
- No additional app-side implementation is needed for Google Photos backup or main-list display.
- The Step22 fix applies to newly captured/saved photos after this Step. Existing photos are not re-registered.

Remaining:

- Step22 user real-device confirmation is OK.
- Pixel 6a confirmation remains deferred for a later pass.
- `docs/error_notes.md` was not updated because no new crash, build failure, or important investigation failure occurred.
- push was not performed.

## Step21 郵便番号から住所自動入力機能

Status:

- Implemented on 2026-07-27.
- Status: completed, build verified, Galaxy real-device confirmation OK, user real-device confirmation OK.
- Scope was limited to `CustomerRegisterActivity`, ZipCloud API communication, and AndroidManifest internet permission.
- Customer registration and customer editing both use the same implementation through the existing shared screen.

Implemented:

- Added ZipCloud postal-code address lookup using `https://zipcloud.ibsnet.co.jp/api/search`.
- Added postal-code input monitoring to `CustomerRegisterActivity`.
- Address lookup starts only after the user edits the postal-code field and the normalized value becomes 7 digits.
- Initial customer data binding in edit mode does not trigger address lookup, so saved addresses are not overwritten when opening the edit screen.
- Postal-code normalization supports surrounding-space trim, half-width hyphen removal, full-width hyphen removal, and full-width digit conversion to half-width digits.
- Duplicate requests for the same normalized postal code are suppressed, including requests already in flight.
- If the postal code changes while a request is running, the older result is ignored and is not applied to the address field.
- One returned address is applied directly to the existing address input field.
- Multiple returned addresses are shown with the Android standard `AlertDialog` title `住所を選択`; cancel leaves the address unchanged.
- Not-found responses show `該当する住所が見つかりませんでした`.
- Communication, timeout, JSON, and API status errors show `住所を取得できませんでした。通信状況を確認してください`, while details are logged to Logcat.
- Activity destruction dismisses any address-selection dialog and shuts down the address lookup executor; finished activities do not update UI from returned lookup results.
- API failure does not clear manually entered addresses, so registration and editing can continue by hand input.
- `AndroidManifest.xml` now has `android.permission.INTERNET`.

Galaxy Real-device Confirmation:

- New customer registration: postal-code address auto-fill works.
- Existing customer editing: postal-code address auto-fill works.
- Hyphenless postal codes work.
- Hyphenated postal codes work.
- Full-width digit postal codes work after normalization.
- After address auto-fill, house number and building name can still be added manually.
- Opening the edit screen does not overwrite an existing saved address.
- When no matching address exists, the existing address field is not cleared.
- When communication fails, the address can still be entered manually and saved.
- No crash occurred.
- Existing customer registration, editing, and save flows were not affected.

Address Auto-fill UX Decision:

- Step21 keeps the current behavior: address lookup runs automatically when postal-code input reaches 7 normalized digits.
- This was chosen because automatic lookup is a common implementation style, reduces operation count, and keeps the Master UI unchanged.
- Adding a success Toast such as `郵便番号から住所を入力しました`, adding an `住所検索` button, or re-evaluating automatic versus manual lookup remains only a future UI/operation review item, not a confirmed task.

Not Changed:

- `activity_customer_register.xml` layout and Master UI design were not changed.
- `Customer` Entity fields were not changed.
- Room DB version, schema, DAO, Repository save/update behavior, and migrations were not changed.
- No Retrofit, Volley, OkHttp, or other new external communication library was added.
- No automatic large-volume live ZipCloud API test was run.

Verification:

- `$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- No compile errors were reported.
- Galaxy real-device confirmation is OK.
- Step21 is complete.

## Step20 Final Completion

Status:

- Step20A through Step20B-2 are complete.
- User Galaxy real-device confirmation is OK.
- Calendar display and operation are accepted by the user on Galaxy SC-03L.
- Pixel 6a final display confirmation remains a future check only.

Completed Scope:

- Step20A: Migrated the calendar screen to Kizitonwose Calendar View `2.10.1`.
- Step20B: Added Rokuyo and Japanese holiday display.
- Step20B-1: Fixed the visit-day dot clipping issue after Rokuyo was added.
- Step20B-2: Adjusted the calendar/history vertical area balance and week-to-week spacing.

Step20A Summary:

- `CalendarActivity` remains Java.
- Kizitonwose Calendar View `2.10.1` is used.
- Core Library Desugaring was added for `java.time` on `minSdk 24`.
- Month display, previous/next month movement, month swipe, date selection, today background, Sunday/Saturday colors, visit-day dots, and shooting-history updates were reproduced on the new calendar.
- Existing MaterialCalendarView-related replacement scope was completed; the actual previous implementation in this repository was Android standard `CalendarView`, not an external MaterialCalendarView dependency.
- Galaxy real-device confirmation is OK.

Step20B Summary:

- Rokuyo display was implemented with local Java helper classes.
- Japanese national holiday detection was implemented with a local Java helper class.
- Rokuyo is calculated locally from an internal Japanese lunisolar-calendar table.
- Holidays are detected locally on device; no external runtime API or network call is used.
- Holiday date numbers are displayed in red.
- Rokuyo/holiday unit tests passed.
- Galaxy real-device confirmation is OK.

Step20B-1 Summary:

- Fixed the issue where visit-day dots were clipped at the bottom of the date cell after Rokuyo was added.
- Data loading, visit-date judgment, and Binder visibility logic were confirmed to be normal.
- The issue was fixed by adjusting date-cell dimensions and spacing.
- Visit dots were visually confirmed on dates with existing visit records.
- Dot persistence was confirmed after selection, after selecting another date, and after month navigation.
- Galaxy real-device confirmation is OK.

Step20B-2 Summary:

- Increased the calendar display area.
- Reduced the shooting-history area to an appropriate size while keeping the title and at least one visible row.
- Changed 5-week months so an unnecessary empty 6th row is not reserved.
- Kept the date number, Rokuyo, and visit-dot sizes from Step20B-1.
- Added vertical spacing so week-to-week distance is closer to the Master UI.
- 5-week and 6-week displays were both confirmed.
- User real-device confirmation for visual balance and operation is OK.

Test And Build:

- `testDebugUnitTest --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- Debug APK was installed on Galaxy SC-03L with `adb install -r`.
- Filtered Logcat showed no app crash or exception stack.
- DB structure was not changed.
- No Migration was added.
- Kotlin and Compose were not introduced.

Future Checks:

- Pixel 6a final display confirmation is planned for a later pass.
- Device-specific layout adjustment should be done only if the Pixel check finds an actual issue.

## Step20B-2 Calendar Vertical Spacing Adjustment

Status:

- Implemented and Galaxy SC-03L checked on 2026-07-27.
- Status: implemented, Galaxy real-device checked, user visual/operation review OK.
- Scope was limited to calendar/history vertical area allocation and date-cell vertical spacing.
- CalendarActivity, Binder logic, Rokuyo calculation, Japanese holiday calculation, visit-date detection, DB, Gradle, Kotlin, and Compose were not changed.

Symptom:

- After Rokuyo, holiday, and visit-dot display were working, the Galaxy SC-03L calendar still looked vertically tight.
- Week-to-week spacing was narrow, and the calendar had less breathing room than the Master UI.
- The shooting-history card could safely be smaller, while still keeping its title and at least one visible history row.

Fix:

- Increased the calendar card weight from `1.35` to `1.55`.
- Reduced the shooting-history card weight from `1` to `0.85`.
- Changed Kizitonwose `cv_outDateStyle` from `endOfGrid` to `endOfRow`, so 5-week months do not reserve an empty 6th row and can distribute the calendar height across the visible weeks.
- Kept date, Rokuyo, and dot sizes from Step20B-1.
- Added 2dp vertical gaps between the date number/Rokuyo and Rokuyo/visit dot.
- Card corner radius, border, colors, bottom navigation, and history row UI were not changed.

Before/After Metrics On Galaxy:

- Before Step20B-2: CalendarView bounds were `[63,406][1017,1149]` and 2026-07-08 cell bounds were `[471,529][607,653]`.
- After Step20B-2: CalendarView bounds are `[63,406][1017,1275]` and 2026-07-08 cell bounds are `[471,579][607,753]`.
- CalendarView height increased from 743px to 869px, about 48dp at density 420.
- 2026-07-08 visible cell height increased from 124px to 174px.
- Shooting-history card bounds changed to `[42,1338][1038,1964]`, keeping the title and one visible row.

Verified:

- `testDebugUnitTest --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Galaxy `RF8M50DL2NA`: `device`
- Device: Samsung SC-03L / Android 12 / density 420
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeded with existing data preserved.
- 2026-07 5-week display: rows are visually more open, no extra empty 6th row is reserved, and there is no overlap with the shooting-history card or bottom navigation.
- 2026-07 visible visit dots: existing data showed dots on 2026-07-02, 2026-07-08, and 2026-07-09. 2026-07-16 had no existing visit record, so no dot was expected there.
- 2026-07-08 selected state: dot remained visible under Rokuyo, with bounds `[532,720][545,733]`.
- 2026-07-20 holiday display: content description included `海の日`, and the date was visually red.
- 2026-07-08 shooting-history card showed 1 row, and the row remained tappable.
- History row opened `CustomerDetailActivity`; Android Back returned to `CalendarActivity`.
- 2026-08 6-week display: all rows including 2026-08-31 were fully visible. 2026-08-31 bounds were `[199,1130][335,1275]`.
- Previous/next month buttons worked.
- Month swipe to 2026-08 and back to 2026-07 worked.
- After swipe back, 2026-07 showed 3 visible visit-dot nodes and the 2026-07-08 dot stayed visible.
- Screenshots were saved outside Git-managed paths and opened for visual confirmation.
- Filtered Logcat for the app PID showed no app crash or exception stack.

Remaining:

- Pixel device confirmation if needed.

## Step20B-1 Visit Dot Display Fix

Status:

- Implemented and Galaxy SC-03L checked on 2026-07-27.
- Scope was limited to the visit-day dot display bug.
- CalendarActivity remains Java.
- Kizitonwose Calendar View remains `2.10.1`.
- No Kotlin, Compose, DB schema, Entity, DAO, Migration, photo-storage, CSV, calendar-library, Rokuyo calculation, or Japanese holiday calculation changes were added.

Symptom:

- On 2026-07-08, the shooting history card showed 1 history row, but the black visit-day dot was not visible in the calendar date cell.

Cause:

- The Kizitonwose date cell row height on Galaxy SC-03L was about 47dp.
- The previous vertical layout used a 36dp date circle, 16dp Rokuyo line, 6dp dot, 2dp dot top margin, and 2dp top/bottom cell padding.
- Date number and Rokuyo consumed the visible cell height, so the dot was pushed to the bottom edge/outside the cell and clipped.
- The visit-date data and Binder judgment were correct; the issue was layout height/positioning.

Fix:

- Compacted only the date-cell dimensions so the order remains date number, Rokuyo, visit dot.
- `calendar_day_circle_size`: 36dp -> 28dp.
- `calendar_rokuyo_text`: 11sp -> 10sp.
- `calendar_rokuyo_line_height`: 16dp -> 14dp.
- `calendar_visit_dot_size`: 6dp -> 5dp.
- `calendar_day_vertical_padding`: 2dp -> 0dp.
- `viewCalendarVisitDot` top margin: 2dp -> 0dp.
- `textCalendarRokuyo` uses `includeFontPadding=false` to keep the 14dp line stable.
- Binder logic still resets the dot every bind and uses `visitDot.setVisibility(hasVisitHistory ? View.VISIBLE : View.INVISIBLE);`.

Verified:

- `testDebugUnitTest --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Galaxy `RF8M50DL2NA`: `device`
- Device: Samsung SC-03L / Android 12 / SDK 31 / override size 1080x2280 / density 420
- `adb install -r app/build/outputs/apk/debug/app-debug.apk` succeeded with existing data preserved.
- 2026-07-08: shooting history row count was 1.
- 2026-07-08 Binder/accessibility state: `来店記録あり`.
- 2026-07-08 cell bounds were `[471,529][607,653]`.
- 2026-07-08 visit-dot bounds were `[532,640][545,653]`, so the actual dot size was 13px x 13px and fully inside the cell.
- Screenshots were saved outside Git-managed paths and opened for visual confirmation.
- 2026-07 full screen: black dot was visible under 2026-07-08.
- 2026-07-08 selected state: black dot remained visible under Rokuyo and did not overlap the selection background.
- Selecting 2026-07-07 after 2026-07-08 kept the 2026-07-08 dot visible.
- Negative target 2026-07-07: no shooting history, no visit-dot node, empty-history message shown.
- Previous-month button to 2026-06 and next-month button back to 2026-07 kept the 2026-07-08 dot visible.
- Month swipe to 2026-08 and swipe back to 2026-07 kept the 2026-07-08 dot visible.
- 2026-07 5-week display showed no overlap with the shooting-history card or bottom navigation.
- 2026-08 6-week display showed no overlap with the shooting-history card or bottom navigation. Existing data had no August visit dots.
- History row from 2026-07-08 opened `CustomerDetailActivity`; Android Back returned to `CalendarActivity` with the selected date and dot still visible.
- Filtered Logcat for the app PID showed no app crash or exception stack.
- commit / push were not performed.

Remaining:

- Pixel device confirmation if needed.

## Step20B Rokuyo And Japanese Holiday Display

Status:

- Implemented and Galaxy SC-03L checked on 2026-07-26.
- Calendar screen remains Java.
- Kizitonwose Calendar View remains `2.10.1`.
- No Kotlin, Compose, DB schema, Entity, DAO, Migration, photo-storage, CSV, or external runtime API changes were added.

Implemented:

- Added local Java Rokuyo helpers: `JapaneseLunarDate`, `JapaneseLunarCalendarConverter`, `Rokuyo`, `RokuyoCalculator`.
- Added local Java Japanese holiday helper: `JapaneseHolidayCalculator`.
- Calendar date cells now display date number, Rokuyo, and visit dot in a vertical layout.
- Holiday dates are rendered red. Holiday names are included in accessibility text but are not shown inside cells.
- Date text color priority is holiday, Sunday, Saturday, normal weekday.
- Selected date background has priority over today background. Today background returns when another date is selected.
- Binder reset covers date text, Rokuyo text, text colors, backgrounds, content description, clickability, and dot visibility.
- Rokuyo clipping found on the first Galaxy screenshot was fixed by giving the Rokuyo TextView a fixed line height and keeping font padding.

Verified:

- `testDebugUnitTest --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Galaxy `RF8M50DL2NA`: `device`
- Device: Samsung SC-03L / Android 12 / SDK 31 / override size 1080x2280 / density 420
- `adb install -r` succeeded with existing data preserved.
- App launched and CalendarActivity focus was confirmed.
- Initial display, 7 columns, month title, previous/next buttons, month swipe, date selection, selected background movement, today background, Sunday red, Saturday blue, holiday red, Rokuyo display, 5-week month, 6-week month, 6th-row selection, and no overlap with the history card/bottom navigation were confirmed by screenshots.
- Checked months did not show visible visit dots or visit history rows, so customer-detail transition from calendar history was not confirmed in this pass.
- Visit-dot persistence and calendar-history-to-customer-detail navigation were confirmed in later Step20B-1 / Step20B-2 checks using existing device data.
- Filtered Logcat did not show app crash or exception stack.

Documentation:

- `docs/calendar_rokuyo_holiday_implementation.md` added.
- `docs/error_notes.md` was not updated because no new crash, build error, or calculation runtime failure occurred.

Remaining:

- Pixel 6a confirmation if needed.
- Future review for official holiday-law changes and exceptional old-calendar/Rokuyo source differences.

## Maintenance Note

作業終了ごとに、このファイルを更新すること。

更新する内容:

- 完了した機能
- 作業途中の機能
- 現在のブランチ
- 実装済みStep
- 次に再開する位置
- 実機確認状況

作業終了時は、あわせて以下も確認・更新すること。

- `docs/error_notes.md`
- `docs/next_tasks.md`

## Project

「おかんのカルテ」は、顧客情報と写真を紐づけて管理するAndroidアプリです。

開発環境:

- Android Studio
- VS Code
- Codex
- Java
- Room

現在のブランチ:

- `main`

## 完了済み機能

- カメラ撮影
- 撮影後の顧客選択
- 撮影後の顧客新規登録
- 顧客登録
- 顧客一覧
- 顧客検索
- 顧客詳細表示
- 顧客編集
- 顧客削除
- 顧客別写真表示
- 写真詳細表示
- 写真メモ編集
- 写真削除
- 撮影履歴カレンダー
- 日付別撮影顧客表示
- CSV住所録出力
- アプリ内写真一覧

## 作業途中機能

- なし

進捗:

- Step 1完了: `photos` テーブルから全写真を取得するDAO/Repositoryメソッドを追加済み。
- Step 2完了: `PhotoListActivity` と `activity_photo_list.xml` を追加済み。
- `AndroidManifest.xml` への画面登録済み。
- Step 3完了: `PhotoListAdapter` と `item_photo_list.xml` を追加済み。
- 写真一覧画面でサムネイル、顧客名、撮影日、メモを表示。
- Step 4完了: 写真一覧の行タップで `CustomerDetailActivity` へ遷移。
- Step 5完了: `MainActivity` から写真一覧導線を追加済み。
- Step 6-1 実機確認A対応済み: 写真一覧起動時の「写真がありません」空表示チラつきを修正。
- Step 6-2 実機確認C記録済み: 「河上」写真タップ時に顧客情報を開けない問題は、古い不整合データまたは不正`customerId`を持つ`Photo`レコードが原因と推測。ユーザー操作で該当顧客を削除済み。`Photo.customerId`は`Customer.id`へCASCADE設定されているため、関連`Photo`レコードも削除される想定。今後、再発有無を実機で確認する。
- Step 6-3 実機確認B対応済み: 顧客名編集時に関連写真の`Photo.customerName`を同期する修正を追加。
- Step 6-4 実機確認D記録済み: 顧客一覧で「河上」の行のみ選択状態のような背景が残っていたが、該当顧客削除後に現象は解消したように見える。現時点では他顧客で再発なし。コード修正は行わず、実機確認記録として残す。
- Step 7完了・実機確認済み: 写真一覧タップ時の遷移先を`PhotoDetailActivity`へ変更。
- Step 8完了・実機確認済み: 写真一覧に顧客名・メモ検索、並び順変更を追加。
- Step 8-2完了: 写真一覧の検索debounce、同一条件時の不要更新回避、Adapter反映の軽量化を追加。
- Step 9完了: 写真削除後の前画面復帰・一覧再読み込みを確認し、削除確認文言を改善。
- Step 10完了・実機確認済み: 参考UIの雰囲気を反映し、全体の余白、配色、カードUI、一覧表示、写真詳細、カレンダー表示を美容室カルテアプリ向けに改善。
- Step 11 MediaStore実機確認済み: 新規撮影写真がGalaxyギャラリーに表示されることを確認済み。
- Step 11写真詳細/プレビュー導線最終修正済み: 顧客詳細の写真タップ先を編集用`PhotoDetailActivity`へ戻し、`PhotoDetailActivity`内の写真タップで閲覧専用`PhotoPreviewActivity`を開く導線に変更。プレビュー画面にピンチズーム、ドラッグ移動、ダブルタップ拡大/リセットを追加。
- Step 12実装済み: `PhotoDetailActivity`で同一顧客の写真一覧を保持し、左矢印で新しい写真、右矢印で古い写真へ移動できるように対応。写真、顧客名、撮影日、ファイル名、メモ、プレビュー対象、削除対象を現在表示中の写真に同期。
- Step 12実機確認OK: 写真詳細の左右移動、切り替え後のプレビュー、メモ保存、削除の確認完了。
- Step 13実装済み: 写真一覧と顧客詳細写真グリッドのサムネイル読み込みを、直接`ImageView.setImageURI()`せず`PhotoImageLoader`の縮小decodeへ変更。
- Step 13追加対応済み: 一覧サムネイルのスクロール負荷対策としてGlideを導入し、写真一覧と顧客詳細写真グリッドのサムネイル読み込みを非同期・キャッシュ対応に変更。`PhotoImageLoader`は写真詳細・プレビュー用として残す。
- Step 13実機確認OK: 写真一覧の正常表示、Glide導入後のスクロール改善、クラッシュなし、高速スクロール時の画像残りなし、検索、並び順、写真詳細遷移、顧客詳細写真グリッド、Step12左右移動、MediaStore登録への影響なしを確認済み。
- 次候補: 空状態表示・文言整理、写真ファイル欠損時の表示改善、CSV出力改善、バックアップ機能検討。

## Room構成

Database:

- `AppDatabase`
- DB名: `okannokarte.db`
- version: `1`
- `exportSchema = false`

Entities:

- `Customer`
- `Photo`

DAOs:

- `CustomerDao`
- `PhotoDao`

Repositories:

- `CustomerRepository`
- `PhotoRepository`

## Calendar実装状況

実装済み:

- `CalendarActivity`
- `VisitHistoryAdapter`
- `PhotoDao.getDistinctTakenDates()`
- `PhotoDao.getByTakenDate(String takenDate)`
- `PhotoRepository.listTakenDates()`
- `PhotoRepository.listForDate(String takenDate)`

画面遷移:

- カレンダーの日付選択
- 日付別の撮影顧客表示
- 顧客タップで `CustomerDetailActivity` へ遷移

## CSV出力状況

実装済み:

- `CustomerAddressExport`
- `CustomerDao.getCustomerAddressList()`
- `CustomerRepository.listCustomerAddresses()`
- `CustomerAddressCsvUtil`
- `CsvShareUtil`
- `file_paths.xml` の `cache-path`
- `CustomerListActivity` の「CSV住所録出力」ボタン

CSV仕様:

- UTF-8
- ファイル名: `customer_address_yyyyMMdd.csv`
- ヘッダー: `顧客名,郵便番号,住所`
- 保存先: `context.getCacheDir()`
- 共有: `ACTION_SEND`
- URI: FileProviderの `content://` URI
- 住所未入力顧客は除外

## 写真管理状況

写真保存先:

- `context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)`

実機上の想定パス:

```text
/storage/emulated/0/Android/data/com.example.mkarte1/files/Pictures/<APP_FOLDER>/<顧客名>/
```

保存ファイル名:

```text
yyyyMMdd_<顧客名>.jpg
yyyyMMdd_<顧客名>_01.jpg
```

ギャラリー用コピー先:

```text
Pictures/Okannokarte/
```

注意:

- Step 11修正版以降に新規保存する写真はMediaStoreへ明示コピーする。
- アプリ内保存ファイルは従来通り残す。
- 既存写真の一括MediaStore登録は未実施。
- アプリ内では `Photo.uri` に保存した `file://` URIで表示している。

## Step14 顧客一覧の最終撮影日表示

実装済み:

- `CustomerWithLatestDate` を追加し、`Customer` と `latestTakenDate` を一覧表示用にまとめて扱う。
- `CustomerDao` に `photos.takenDate` の `MAX()` を顧客ごとに取得する一覧/検索用Queryを追加。
- `CustomerRepository.listWithLatestDate()` を追加し、既存の登録・編集・削除処理には影響しない形で一覧専用の取得経路を追加。
- `CustomerListActivity` は最終撮影日付き一覧を取得して `CustomerAdapter` に渡す。
- `CustomerAdapter` は顧客一覧画面のみ、顧客名の下に `最終撮影日：yyyy/MM/dd` または `最終撮影日：未登録` を表示する。
- 撮影後の顧客選択画面では既存表示を維持するため、同じAdapterの最終撮影日表示を無効にしている。

確認済み:

- `assembleDebug` 成功。
- 実機確認OK。
- 顧客一覧に最終撮影日が表示される。
- 写真あり顧客は `yyyy/MM/dd` 表示、写真なし顧客は `未登録` 表示。
- 検索後も最終撮影日が表示される。
- 顧客詳細への遷移、写真一覧、写真詳細、左右移動、カレンダーが正常。
- Entityのカラム追加なし。
- 写真保存処理、MediaStore関連、Step12左右移動、Step13画像読み込み改善には未変更。

## Step15 撮影履歴への名称統一

実装済み:

- 顧客一覧の表示文言を `最終撮影日` へ変更。
- 顧客詳細画面とカレンダー画面を `撮影履歴` 表現へ変更。
- データ取得方法は引き続き `MAX(Photo.takenDate)` を使用し、DB構造やEntity追加は行わない。
- `Visit` Entity / Visitテーブルは採用見送り。

確認済み:

- 実機確認OK。
- 顧客一覧で `最終撮影日` が正常表示される。
- 写真未登録の顧客は `最終撮影日：未登録` と表示される。
- カレンダー画面が `撮影履歴` 表記になっている。
- 写真がない日は `この日の撮影履歴はありません` と表示される。
- 顧客詳細画面が `撮影履歴・写真` 表記になっている。
- 既存の写真一覧、検索、写真詳細遷移に問題なし。
- クラッシュなし。

採用見送り理由:

- 美容室での実運用では、写真撮影時のみ自動的に記録される方が操作がシンプル。
- 来店ごとの手動記録操作を増やさないことで、操作忘れを防ぎやすい。
- 将来、写真なしの来店管理が必要になった場合は、その時点でVisitテーブルを追加可能。

## Step16A UIブラッシュアップ

実装内容:

- 「おかんのカルテA」パターンAとして、白背景・やわらかいアクセントカラー・カードUI中心の見た目へ調整。
- `colors.xml` / `dimens.xml` にUI用カラー、共通余白、入力欄高さ、文字サイズを追加・整理。
- ホーム画面を主要機能カードに変更し、写真を撮る、顧客一覧、カレンダー、写真一覧、新規顧客登録を大きく押しやすい導線に整理。
- 顧客一覧をカード表示として強化し、顧客名、最終撮影日、写真枚数、補足情報が見やすい構成に変更。
- 顧客詳細を基本情報カードと写真・来店履歴カードに整理。
- 写真一覧は写真サムネイルを大きくし、写真が主役に見えるカード構成へ変更。
- 写真詳細は写真、顧客名、撮影日、ファイル名、メモ、左右移動ボタンが見やすい配置になるよう調整。
- カレンダー画面はカレンダーと撮影履歴リストをカード表示に近づけた。
- 顧客登録・編集画面は入力欄と保存ボタンのサイズ・余白・色を統一。
- Room DB構造、写真保存処理、MediaStore処理、productFlavors は変更なし。

確認:

- `assembleDebug` 成功。
- commit / push は未実施。

## Step16A-2 パターンA モックアップ忠実再現

実装内容:

- 添付モックアップのパターンAを優先し、ホームを挨拶カード、2列機能カード、横長新規登録カード、下部ナビ構成へ変更。
- カード角丸を18dpへ寄せ、白ベース＋淡いベージュ/ピンク系の配色、弱い影、広めの余白に再調整。
- Material Symbols相当のベクターアイコンを追加し、ホーム主要カードでは40dp級の大きなアイコン表示へ変更。
- 顧客一覧カードを、丸い初期アイコン、名前、フリガナ、最終来店日、写真枚数、右矢印の配置に変更。
- 顧客詳細を、プロフィールカード、基本情報カード、写真・来店履歴カードに整理。
- 写真一覧を2列グリッド化し、写真領域がカードの大半を占める構成に変更。
- 写真詳細を、上部カウンター、大きな写真、メモ、編集、削除、プレビューの構成へ整理。
- カレンダー画面に下部ナビを追加し、カレンダー/履歴エリアのカード感と余白を強化。
- 顧客登録・編集画面は入力欄を高くし、保存ボタンをアイコン付きに変更。
- Room DB構造、写真保存処理、MediaStore処理は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step16A-3 一般向けホーム設計への修正

実装内容:

- ホームを「機能一覧」から「今日使う情報を整理するダッシュボード」へ変更。
- 下部ナビと重複していたホームの大きな導線カード（顧客一覧、カレンダー、写真一覧）を削除。
- ホームの主導線を「写真を撮る」に絞り、最近来店したお客様3件、今日の予定カード、控えめな新規顧客登録導線を追加。
- 最近来店したお客様は `CustomerRepository.listWithLatestDate()` を使い、顧客名、最終来店日、右矢印を表示して顧客詳細へ遷移。
- 今日の予定カードは現状の予定管理テーブルを追加せず、当日の撮影履歴件数を表示してカレンダーへ遷移。
- 下部ナビの「その他」画面を追加し、住所録CSV出力を顧客一覧から移動。
- 未実装のその他メニュー（バックアップ・復元、データ出力、アプリ情報、設定、ヘルプ・お問い合わせ）はToastで「未実装です」と表示。
- 顧客一覧上部の大きなCSVボタンを削除。
- カレンダー画面の固定年月タイトルを削除し、CalendarView内部の年月表示と重複しないよう修正。
- 顧客一覧の丸アイコン、名前、フリガナ、最終来店日、写真枚数、右矢印の構成は維持し、写真枚数タグを控えめに調整。
- 写真一覧の2列グリッド方向は維持。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step16A-4 最終調整

実装内容:

- 全画面の茶色いActionBarを非表示にするため、`Theme.Mkarte1` を `NoActionBar` ベースへ変更。
- ステータスバーとナビゲーションバーは淡い背景色に合わせ、各画面タイトルは画面内見出しとして維持。
- ホームから「今日の予定」カードを完全に削除。
- ホームはスクロール前提をやめ、タイトル、挨拶カード、写真を撮るカード、最近来店したお客様3件、新規顧客登録ボタン、下部ナビが1画面に収まるよう高さと余白を圧縮。
- ホームの余計な説明文と不要な余白を削除。
- `colors.xml` を淡いコーラル/ピンクベージュ系へ調整し、濃い茶色の印象を弱めた。
- 下部ナビの選択色を淡いコーラル、非選択色をサブテキスト色、背景を白、境界線を淡い線色に統一。
- 写真詳細の編集ボタンを淡いアクセント背景、プレビューボタンを白カード＋枠線、削除ボタンを薄い赤背景＋赤系テキストへ調整。
- 顧客一覧、写真一覧、カレンダー、その他画面の方向性は維持し、全体の色味は新カラーへ統一。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step16A-5 実機確認指摘対応

実装内容:

- ホームの「最近来店したお客様」を「最近撮影したお客様」へ変更し、最終撮影日がある顧客だけを新しい順に最大3件表示するよう調整。
- ホームの表示バランスを微調整し、今日の予定カードは復活させず、スクロールなしで収まる構成を維持。
- 顧客一覧に並び替えSpinnerを追加し、初期表示を「最終撮影日 新しい順」にした。
- 顧客一覧の並び替えは「最終撮影日 新しい順」「最終撮影日 古い順」「名前順」「写真枚数 多い順」に対応。
- 顧客一覧カードの文言を「最終撮影日」に統一。
- 顧客詳細の下部ナビで「顧客一覧」を押すと `CustomerListActivity` へ戻れるよう、下部ナビ共通処理に現在タブクリック許可の切り替えを追加。
- 顧客詳細の「写真・来店履歴」表記を「撮影履歴・写真」へ変更。
- 写真詳細に下部ナビを追加し、「写真一覧」タブを選択状態にしたうえで写真一覧へ遷移できるようにした。
- 写真詳細の編集ボタン文字色、削除/プレビューボタン、左右矢印背景を調整し、濃い塗りつぶしを減らして文字の読みやすさを改善。
- 写真詳細の左右矢印は写真表示エリア内の左右中央に配置し、1枚のみの場合は非表示、複数枚の場合のみ表示される既存挙動を維持。
- カレンダーの二重枠感を減らすため、外側カード1枚にCalendarViewを載せ、内部の過剰な枠と影を削除。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step16A-6 色味最終調整

実装内容:

- `colors.xml` をモックアップ寄りの白ベース、暖かい背景、淡いピンクベージュ、ラベンダー、ミント、クリームの補助色へ再調整。
- メインアクセントを #E9A391、薄アクセントを #F8DDD6、薄ピンクを #FCEDEA、線色を #EDE2DD、危険色を #D85C4A / #FCE7E4 へ寄せた。
- 顧客一覧、ホーム最近撮影、顧客詳細の丸アイコンを薄いコーラル背景にし、文字色を #C77766 に変更して濃い塗りつぶしを削減。
- カード、入力欄、チップ、選択カード、補助ボタンの枠線を弱め、ピンク面積が強く出すぎないよう調整。
- 主要ボタンは淡いアクセント塗りを維持しつつ、文字色と保存アイコンを濃い本文色に変更して読みやすさを優先。
- 写真詳細の編集、プレビュー、削除、左右矢印は既存配置を維持し、薄い背景でも文字が読める色へ統一。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理、顧客一覧ソート機能は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step16 パターンA UIブラッシュアップ完了

完了内容:

- パターンAのモックアップを基準に、一般向け美容室アプリとしてのUIブラッシュアップを実施。
- 下部ナビを導入し、ホーム、顧客一覧、カレンダー、写真一覧、その他の主要導線を整理。
- ホームは機能一覧からダッシュボード構成へ再設計し、写真を撮る導線、最近撮影したお客様、新規顧客登録を中心に整理。
- 住所録CSV出力を顧客一覧からその他画面へ移動。
- 写真一覧を2列グリッド化し、写真が主役に見えるカード表示へ変更。
- 顧客一覧、顧客詳細、写真詳細、カレンダー、登録/編集画面の余白、カード、ボタン、文言、下部ナビ状態を調整。
- Step16A-6で白ベース、淡いピンクベージュ、ラベンダー、ミント、クリームを使う方向へ色味を再調整。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理、productFlavors は変更なし。

残課題:

- 色味、余白、情報の強弱はまだ改善余地あり。
- Step17でデザインシステム整理として継続対応する。

## Step17 UIデザインシステム整理・Visual Hierarchy改善

実装内容:

- `dimens.xml` に `space_xs/sm/md/lg/xl`、`radius_small/medium/large/pill`、`text_screen_title/section_title/card_title/body/caption`、`icon_bottom_nav/card/hero` を追加し、画面横断の余白・角丸・文字・アイコンサイズの基準を整理。
- 既存の `mkarte_*` dimens は互換として残しつつ、主要画面では新しい共通dimenへ寄せた。
- カード背景、機能カード、入力欄、チップ、補助ボタン、下部ナビ、丸アイコン背景の線・角丸・色の使い方を整理し、弱い線と弱い影を基本にした。
- ホームは「写真を撮る」を最重要カードとして淡いアクセント背景と大きめアイコンで強調し、最近撮影したお客様と新規顧客登録は控えめな階層へ調整。
- 顧客一覧は顧客名を最も目立たせ、最終撮影日、写真枚数タグ、フリガナ/電話番号の順に情報の強弱が出るようAdapter側の表示順と文字サイズを調整。
- 顧客詳細は顧客名カード、基本情報カード、撮影履歴・写真カードの余白と文字サイズを整理し、編集/削除ボタンのサイズを共通ルールへ寄せた。
- 写真一覧は2列グリッドを維持しつつ、写真サムネイル領域を広げ、顧客名と撮影日/メモを補助情報として整理。
- 写真詳細は写真表示エリアの比重を上げ、メモ欄と編集ボタン、削除/プレビューボタンを読みやすいサイズと間隔へ調整。
- カレンダーは外側カード1枚の見え方を維持し、撮影履歴カードと履歴行の余白・文字サイズ・チップ表示を整理。
- その他画面はCSV出力を主項目、未実装項目を補助項目として見えるよう行の濃淡と余白を調整。
- 下部ナビはアイコンサイズと文字サイズを共通dimenへ寄せ、現在タブだけアクセント表示になる既存挙動を維持。
- Room DB構造、Entity構造、写真保存処理、MediaStore登録処理、顧客一覧ソート機能、写真詳細左右移動、メモ保存、写真削除機能は変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step18 Android 15 / Pixel対応 + 写真プレビュー改善 + ボタン画像反映

実装内容:

- Android 15 / Pixel 6a相当のEdge-to-Edge環境で、ステータスバーやナビゲーションバーに画面内容が被らないよう、共通の `EdgeToEdgeUtil` を追加した。
- ホーム、顧客一覧、顧客詳細、顧客登録/編集、写真一覧、写真詳細、カレンダー、その他、カメラ、撮影後顧客選択の主要画面で、status bar / navigation bar / display cutout / IME Insets をルートレイアウトのpaddingへ反映するようにした。
- 顧客登録/編集兼用画面に `windowSoftInputMode="adjustResize"` を設定し、`ScrollView` の下端paddingと組み合わせてキーボード表示中も登録/修正ボタンまでスクロールできるようにした。
- 写真プレビュー画面の左上に戻るボタンを追加し、タップで `finish()` して写真詳細へ戻れるようにした。
- 写真プレビューの既存ピンチズーム、ドラッグ移動、ダブルタップ拡大/リセット処理は維持した。
- 添付PNGを参考に、顧客登録時は「登録する」、顧客編集時は「修正する」のボタン表示を調整した。後続の追加修正2で、PNG画像ボタンではなく通常Button + drawable背景へ移行済み。
- 顧客登録/編集の保存処理、入力チェック、既存画面遷移は変更なし。
- Room DB構造、Entity構造、DAO、Repository、写真保存処理、MediaStore登録処理、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain --no-daemon` 成功。
- 事前の通常 `assembleDebug --console=plain` は、既存Gradle daemonが存在しない `jlink.exe` を参照して失敗したため、Android Studio JBRを指定し直して `--no-daemon` で再実行した。
- commit / push は未実施。

## Step18追加修正 写真詳細・写真プレビューUI改善

実装内容:

- 写真詳細画面のメモ保存ボタン文言を「編集」から「メモ追加」へ変更した。
- 写真詳細画面の「プレビュー」ボタンを「拡大表示」へ変更し、削除ボタンと配置を入れ替えた。
- 写真詳細画面の削除ボタンは、赤系の意味合いを残しつつ、背景・枠線・文字色を調整して読みやすくした。
- 写真タップでプレビューへ遷移する動作を廃止し、拡大表示は「拡大表示」ボタンからのみ開くようにした。
- 写真プレビュー画面の上部に、戻るボタン、顧客名、撮影日、現在枚数 / 総枚数を表示するヘッダーを追加した。
- 写真プレビュー画面でも `PhotoRepository.listForCustomer()` の既存順を使い、同一顧客の写真を左右スワイプで切り替えられるようにした。
- 写真プレビューのスワイプ切り替え時は、表示画像、撮影日、枚数表示を更新し、ズーム状態を初期表示へ戻すようにした。
- 写真プレビューのピンチズーム、ドラッグ移動、ダブルタップ拡大/リセット、戻るボタンは維持した。
- 顧客詳細、顧客登録/編集、カメラ画面の戻るボタンを、登録/修正ボタンに近い丸みと色味へ軽く調整した。
- Room DB構造、Entity構造、DAO、Repository仕様、写真保存処理、MediaStore登録処理、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step18追加修正2 写真プレビュー被り修正 + ボタンDrawable統一

原因調査:

- 写真プレビューのヘッダー被りは、`activity_photo_preview.xml` のルートが `FrameLayout` で、写真 `ImageView` を全画面表示した上にヘッダーを重ねていたことが原因。
- 顧客登録/編集画面は `CustomerRegisterActivity` と `activity_customer_register.xml` を共用しており、実際の戻るボタンIDは `buttonBack`、保存ボタンIDは `buttonSave`。
- 前回の戻るボタンは `buttonBack` に `bg_button_back.xml` を指定済みだったが、登録/修正側が `ImageButton` + `btn_register.png` / `btn_edit.png` のままだったため、同じ設計ルールに見えにくかった。

実装内容:

- 写真プレビュー画面を縦方向レイアウトに変更し、上部ヘッダーと写真表示エリアを分離した。
- 写真プレビューの戻るボタン、顧客名、撮影日、枚数表示は上部固定の黒背景ヘッダー内に表示し、写真と重ならないようにした。
- 写真プレビューの `imagePhotoPreview` は維持し、ピンチズーム、ドラッグ移動、ダブルタップ拡大/リセット、左右スワイプ切り替えの既存処理を維持した。
- 顧客登録/編集画面の `buttonSave` を `ImageButton` から通常 `Button` へ戻し、登録時は `bg_button_register.xml`、編集時は `bg_button_edit.xml` を使うようにした。
- `btn_register.png` / `btn_edit.png` は直接使用しない方針に合わせ、未使用リソースとして削除した。
- 登録、修正、戻るボタンの高さ・角丸・余白を揃え、戻るボタンは `bg_button_back.xml` の控えめな枠線ボタンへ調整した。
- 顧客登録処理、顧客編集処理、入力チェック、写真保存処理、MediaStore登録処理、写真表示処理、写真スワイプ切り替え処理は変更なし。
- Room DB構造、Entity構造、DAO、Repository仕様、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step19A 新規顧客登録/編集画面 詳細モックアップ再現

実装内容:

- 添付の「⑦ 新規顧客登録」詳細モックアップを基準に、顧客登録/編集共用画面を白いカード内のアイコン付き1行フォームへ変更。
- 画面上部に戻る矢印とタイトルを配置し、登録時は「新規顧客登録」、編集時は「顧客情報」と表示するように調整。
- 顧客名、フリガナ、電話番号、郵便番号、住所、メモを、左アイコン、項目名、例文hint、下線区切りの構成へ変更。
- PNGは使わず、person/text/phone/postal/location/note相当のVector Drawableを追加してフォーム左側アイコンとして使用。
- 保存/登録ボタンは通常Button + `bg_button_register.xml` のPrimary Button、編集時の修正ボタンは通常Button + `bg_button_edit.xml` のSecondary Buttonへ統一。
- 戻る/キャンセルボタンは通常Button + `bg_button_back.xml` を使用し、保存/修正ボタンと高さ・角丸・余白を揃えた。
- `ScrollView` 構成と `EdgeToEdgeUtil.apply(this)` は維持し、キーボード表示時のスクロール、ステータスバー/ナビゲーションバー被り対策を継続。
- メールアドレス欄は現行 `Customer` Entity に項目がなく、DB構造変更禁止のため追加していない。
- 顧客登録処理、顧客編集処理、入力チェック処理は変更なし。
- Room DB構造、Entity構造、DAO、Repository仕様、写真保存処理、MediaStore登録処理、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step19B 新規顧客登録/編集画面 Master UI最終調整

実装内容:

- 添付の「⑦ 新規顧客登録」詳細モックアップをMaster UIとして、顧客登録/編集共用画面の再現度を再調整。
- 上部戻るを文字記号から矢印アイコンへ変更し、タイトルの大きさ、余白、カード位置をモックアップ寄りに調整。
- フォームカードの横余白、内側余白、角丸、線色を調整し、影は外して軽いカード表現へ変更。
- 顧客名アイコンを小さくし、その他フォームアイコンも小さめの薄いグレージュ系へ統一。
- フリガナアイコンを三本線から文字入力を示すアイコンへ変更。
- 郵便番号アイコンを〒単体から封筒系アイコンへ変更。
- 入力例/hintをすべて削除し、表示要素をアイコン、項目名、入力値、下線に整理。
- 項目名は `sans-serif-medium`、小さめの文字サイズへ変更し、太すぎない見え方に調整。
- 下線は右端に余白を持たせ、カードいっぱいまで伸びない長さへ調整。
- 保存/修正ボタンをMaster UIに寄せ、コーラル背景、同一角丸、左側の白い角丸背景付き顧客追加アイコン、中央揃えテキストの構成へ変更。
- キャンセルボタンを白背景、細いグレー系枠線、黒文字、保存ボタンと同じ高さ・角丸へ変更。
- 編集時の「修正する」も同じPrimary Button構成に変更。
- メールアドレス欄は現行 `Customer` Entity に項目がなく、DB/Entity変更禁止のため追加していない。
- 顧客登録処理、顧客編集処理、入力チェック処理は変更なし。
- Room DB構造、Entity構造、DAO、Repository仕様、写真保存処理、MediaStore登録処理、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- commit / push は未実施。

## Step19C 新規顧客登録/編集画面 Master UI最終仕上げ

実装内容:

- 保存/修正ボタンの見た目を再調整し、コーラル背景、Master UI寄りの角丸、左側の白い角丸背景付き顧客追加アイコン、中央テキストの構成を維持。
- Android標準Buttonの背景/compound drawable tintで保存アイコンや白背景が上書きされないよう、XMLと `CustomerRegisterActivity` 側でtintを明示的に解除。
- キャンセルボタンは白背景、細いグレー枠、黒文字、保存ボタンと同じ高さ・角丸へ再調整。
- フリガナアイコンを、Master UIの「あア」に近い文字アイコン表示へ変更。
- 郵便番号アイコンを、薄いグレージュの丸背景に白い封筒アイコンを載せる構成へ変更。
- 顧客名アイコンと各フォームアイコンをさらに小さめにし、主張を抑えた。
- 項目名と入力値の文字サイズを少し下げ、フォーム行の高さもやや詰めてMaster UIの密度に近づけた。
- 下線は右端に余白を持たせたまま、カードいっぱいまで伸びない長さを維持。
- 保存ボタンとキャンセルボタンの間隔を18dpへ調整。
- 顧客登録処理、顧客編集処理、入力チェック処理は変更なし。
- Room DB構造、Entity構造、DAO、Repository仕様、写真保存処理、MediaStore登録処理、Calendarロジックは変更なし。

確認:

- `assembleDebug --console=plain` 成功。
- ADBフルパス `C:\Users\YRhei\AppData\Local\Android\Sdk\platform-tools\adb.exe` で実機 `RF8M50DL2NA` を認識。
- 最新 `app-debug.apk` を実機へ上書きインストール済み。
- 実機で `CustomerRegisterActivity` を表示し、スクリーンショット取得済み。
- 保存ボタンの左アイコンが実機スクリーンショット上で表示されることを確認。
- キャンセルボタンが白背景・細い枠線で表示されることを確認。
- Step19C 実機確認OK。
- 残差異として、保存ボタン左アイコンがMaster UIより少し大きく左寄りに見えるが、軽微な追加調整候補として `docs/next_tasks.md` に記録済み。
- push は未実施。

## Step20 Kizitonwose Calendar導入時の実装言語選定調査

調査内容:

- 現在のAndroid標準`CalendarView`からKizitonwose Calendar View版へ移行する前に、カレンダー画面をJavaのまま実装する案と、カレンダー画面だけKotlinにする案を比較した。
- 公式GitHub、公式View docs、Maven Central、Android Developers、Kotlin公式情報を確認した。
- 調査時点のKizitonwose Calendar View版は`com.kizitonwose.calendar:view:2.10.1`を対象にした。
- `javap`で`CalendarView`、`ViewContainer`、`MonthDayBinder`、`CalendarDay`、`CalendarMonth`、`DayPosition`のJava向けシグネチャを確認した。
- リポジトリ外の一時ディレクトリで、Javaの最小コードが`MonthDayBinder`、`setup(...)`、`scrollToMonth(...)`、`notifyDateChanged(...)`、`notifyMonthChanged(...)`、月スクロールリスナーを呼び出してコンパイルできることを確認した。
- 現在の`minSdk`は`24`のため、Kizitonwose Calendar移行時には`java.time`向けCore Library Desugaringが必要。

採用方針:

- `CalendarActivityはJavaのまま実装する`を推奨案として採用。
- Javaから必要APIを呼び出せること、既存コードがJava中心であること、Kotlin Android plugin追加を避けて移行差分を小さくできることを重視した。
- Kotlin案は公式サンプルに近い利点はあるが、現状のプロジェクトではGradle変更とJava/Kotlin混在の管理負担が増えるため不採用。

確認:

- ベースラインとして `assembleDebug --console=plain --no-daemon` 成功。
- アプリ本体のソース、Gradle設定、レイアウトXML、DB構造は変更していない。
- Kizitonwose Calendarのライブラリ移行実装はまだ行っていない。
- 調査詳細は `docs/calendar_kizitonwose_language_investigation.md` に記録済み。

## Step20A Kizitonwose Calendar View版への移行

実装内容:

- カレンダー画面をJavaのまま維持し、Kizitonwose Calendar View版 `com.kizitonwose.calendar:view:2.10.1` へ移行した。
- 現行コード上は外部MaterialCalendarView依存ではなくAndroid標準`CalendarView`を使用していたため、MaterialCalendarView依存関係やDecoratorの削除対象は存在しなかった。
- `gradle/libs.versions.toml` と `app/build.gradle.kts` にKizitonwose Calendar View版を追加した。
- `minSdk 24`で`java.time.LocalDate` / `YearMonth`を使うため、Core Library Desugaringを追加した。
- `activity_calendar.xml` のカレンダー領域をKizitonwose `CalendarView`へ置き換え、月タイトル、前月/翌月ボタン、曜日行、撮影履歴カード、下部ナビゲーションを維持した。
- `item_calendar_day.xml` を追加し、日付数字、今日の淡い水色背景、選択日の淡いピンク背景、撮影日ドットを表示できる日付セルにした。
- `CalendarActivity.java` にJavaの`MonthDayBinder` / `ViewContainer`実装を追加し、セル再利用時に日付文字、文字色、背景、ドット、クリック可否、表示状態を毎回初期化するようにした。
- 表示期間は現在月を基準に過去20年から未来10年までに設定した。既存写真データと今後の利用を妨げにくく、極端に広すぎない範囲として採用。
- 日曜日始まりで、日曜は赤、土曜は青、平日は通常色にした。
- 撮影日ドットは既存の`PhotoRepository.listTakenDates()` / `PhotoDao.getDistinctTakenDates()` / `Photo.takenDate`を使用し、DB構造や保存形式は変更していない。
- 日付選択時は旧選択日と新選択日だけを`notifyDateChanged(...)`で再描画し、選択日の撮影履歴を既存の`PhotoRepository.listForDate(...)`で取得する。
- `onResume()`時に撮影日ドットと選択日の撮影履歴を再取得するようにした。
- 六曜表示、新しい祝日計算処理、新しい祝日ライブラリ、外部API、Kotlin plugin、Kotlinファイル、Composeは追加していない。
- 既存コードには祝日判定ロジックがなかったため、このStepでは新しい祝日表示を追加していない。
- Room DB構造、Entity、DAOの既存仕様、Migration、写真保存処理、MediaStore処理、CSV処理、ManifestのActivity名は変更していない。

確認:

- `assembleDebug --console=plain --no-daemon` 成功。
- `compileDebugKotlin NO-SOURCE`を確認し、Kotlinファイル追加なし。
- MaterialCalendarView参照なし。
- Kizitonwose Calendar参照あり。
- Debug APK生成確認: `app/build/outputs/apk/debug/app-debug.apk`
- ADBフルパス `C:\Users\YRhei\AppData\Local\Android\Sdk\platform-tools\adb.exe` を確認。
- `adb devices`でGalaxy `RF8M50DL2NA` は検出されたが、状態が`unauthorized`だった。
- USBデバッグ許可完了の連絡後にADB接続を再確認し、PC側で`adb kill-server` / `adb start-server`も実行したが、`RF8M50DL2NA` は引き続き`unauthorized`だった。
- その後、`adb devices`でGalaxy `RF8M50DL2NA` が`device`状態になったことを確認した。
- 端末情報: Samsung SC-03L / Android 12 / SDK 31 / device `SC-03L`。
- 既存データを保持したまま `adb install -r app/build/outputs/apk/debug/app-debug.apk` を実行し、`Success`を確認した。
- Logcatをクリア後、Launcher Activityからアプリを起動し、下部ナビゲーションでカレンダー画面へ移動した。
- `dumpsys window`で `com.example.mkarte1/.ui.calendar.CalendarActivity` がフォーカスされていることを確認した。
- Galaxy実機で、カレンダー初期表示、日曜始まりの7列表示、当月日付の欠けなし、年月タイトル、前月/翌月ボタン、月スワイプ、日付選択、選択背景の移動、今日の淡い水色背景、日曜赤色、土曜青色を確認した。
- 撮影日ドット、日付選択後もドットが消えないこと、選択日の撮影履歴更新、撮影履歴ありの日付から顧客詳細へ遷移して戻れることを確認した。
- 2026年7月で5週表示、2026年8月で6週表示を確認した。
- 主要状態のスクリーンショットを一時保存し、画像を開いてカレンダー、撮影履歴カード、下部ナビゲーションに重なりがないことを確認した。
- 対象アプリのLogcatでクラッシュや例外は確認されなかった。
- Kizitonwose移行に直接関係する不具合は見つからず、追加修正は行っていない。
- Pixel実機は未確認。
- ユーザーによるGalaxy実機確認で、見た目、余白、文字サイズ、タップ感、月移動、Master UIとのバランスはOK。
- commit / push は未実施。
