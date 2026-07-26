# Next Tasks

## Step22 Googleフォト・端末ギャラリーへの写真表示改善

Status:

- 完了 / ユーザー実機確認OK。
- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`
- Galaxyギャラリー確認OK。
- Googleフォトの端末内フォルダ確認OK。
- Pixel 6a確認は後日確認項目として継続。

Implemented:

- 写真保存方式とMediaStore登録方式を調査済み。
- 既存の本体保存先はアプリ専用外部領域 `Pictures/おかんのカルテ/<顧客名>/` のまま維持。
- DBに保存するURIは既存どおり app-owned file の `file://` URIを維持。
- Android 10以降のMediaStoreコピー先は既存どおり `Pictures/Okannokarte/` を維持。
- `DISPLAY_NAME`、`MIME_TYPE = image/jpeg`、`RELATIVE_PATH`、`IS_PENDING`解除の既存処理は維持。
- 0バイトのソースファイルはMediaStore登録またはメディアスキャンしないガードを追加。
- `DATE_ADDED`、`DATE_MODIFIED`、`DATE_TAKEN` はAndroid公式仕様上読み取り専用のため、`ContentValues`への設定は残していない。
- 既存写真の一括MediaStore再登録、DB更新、保存先変更、命名規則変更は実施なし。

Real-device Confirmation:

- アプリ内で新規撮影写真が正常に表示された。
- Galaxyギャラリーの `Okannokarte` フォルダに正常表示された。
- Googleフォトのメイン写真一覧では確認できなかった。
- Googleフォトの `コレクション → このデバイス上 → Okannokarte` では正常に表示された。
- 写真の重複表示なし。
- 0バイト画像なし。
- サムネイル欠損なし。
- クラッシュなし。
- MediaStoreへの写真登録とGoogleフォトによる端末内写真認識は正常と判断。
- Googleフォトのメイン一覧やクラウドバックアップはGoogleフォト側の `Okannokarte` フォルダのバックアップ設定に依存するため、アプリ側の追加実装は行わない。
- 既存写真は今回の修正対象外であり、表示状態が変わらない可能性があること。

Remaining:

- Pixel 6aは後日、必要なタイミングで確認する。

Notes:

- `docs/error_notes.md` は更新なし。Step22作業中に新しいクラッシュ、ビルド失敗、重大な調査失敗は発生していない。
- push は未実施。

## Step21 郵便番号から住所自動入力機能

Status:

- 完了 / Galaxy実機確認OK / ユーザー実機確認OK
- 顧客登録・顧客編集共通の `CustomerRegisterActivity` で対応済み。
- 既存の次候補タスクは勝手に確定せず、この下の既存記載を維持する。

Implemented:

- ZipCloud郵便番号検索APIを使用した住所自動入力。
- 郵便番号7桁入力後の自動検索。
- ハイフンあり・なし対応。
- 全角数字の正規化。
- 複数住所時のAndroid標準 `AlertDialog` 選択。
- 通信失敗・該当なし時も住所欄を消さず、手入力を継続可能。
- DB構造、Roomバージョン、`Customer` Entity、登録/編集保存処理、Master UIレイアウトは変更なし。

Build:

- `assembleDebug --console=plain --no-daemon`: `BUILD SUCCESSFUL`

Real-device Confirmation:

- 新規顧客登録で郵便番号から住所が自動入力されることを確認済み。
- 既存顧客編集でも住所自動入力が利用できることを確認済み。
- ハイフンなし、ハイフンあり、全角数字の郵便番号を確認済み。
- 住所自動入力後に番地・建物名を追加入力できることを確認済み。
- 編集画面を開いただけでは既存住所が上書きされないことを確認済み。
- 該当住所なし、通信失敗時も住所欄を消さず、手入力保存できることを確認済み。
- クラッシュなし、既存の顧客登録・編集・保存機能への影響なし。

Future UI / Operation Review:

- Step21では郵便番号7桁入力後の自動住所入力方式を維持する。
- 成功時Toastの追加、郵便番号欄付近への `住所検索` ボタン追加、自動検索と手動検索の再評価は、確定タスクではなく将来のUI・操作性検討事項として残す。

## Step20 Calendar Migration / Rokuyo / Holiday / Visit Dot

Status:

- Step20: カレンダー基盤移行・六曜・祝日・来店日ドット対応
- 状態: 完了
- Galaxy実機確認: OK
- ユーザー実機確認: OK
- Commit対象として整理済み。

Completed:

- Step20A: Kizitonwose Calendar View `2.10.1` migration with `CalendarActivity` kept in Java.
- Step20B: Rokuyo and Japanese holiday display.
- Step20B-1: Visit-day dot clipping fix.
- Step20B-2: Calendar/history vertical spacing adjustment.
- Unit tests and Debug APK build passed in the implementation pass.
- Galaxy SC-03L APK overwrite install, Logcat check, and user visual/operation review are OK.

Next Calendar Checks:

- Provide a confirmation APK for Pixel 6a if needed.
- Check calendar display on Pixel 6a.
- Adjust only if a Pixel-specific issue is found.

Next Candidate Tasks:

- Empty-state text and display cleanup.
- Missing photo-file display improvement.
- CSV export improvement.
- Backup feature planning.

## Step20B-2 Calendar Vertical Spacing Adjustment

Status:

- 実装完了・Galaxy実機確認済み・ユーザー見た目確認OK。
- Unit tests passed.
- `assembleDebug --console=plain --no-daemon` passed.
- Galaxy SC-03L `RF8M50DL2NA` real-device check completed with APK overwrite install.
- Scope was limited to calendar/history vertical area allocation and date-cell vertical spacing.
- No CalendarActivity, Binder logic, Rokuyo calculation, Japanese holiday calculation, visit-date detection, DB, Gradle, Kotlin, or Compose changes were added.

Confirmed on Galaxy:

- Calendar display area is taller than Step20B-1.
- Shooting-history card area is smaller but still shows the title and at least one history row when data exists.
- 2026-07 5-week display has wider week-to-week vertical spacing.
- 2026-07 does not reserve an unnatural empty 6th row.
- 2026-07 existing visit dots are visible on 7/2, 7/8, and 7/9.
- 2026-07-08 selected state keeps the visit dot visible.
- 2026-07-20 holiday display remains red and includes `海の日`.
- 2026-08 6-week display fully fits, including the final row.
- Previous/next month buttons work.
- Month swipe works.
- History row opens customer detail and Back returns to the calendar.
- No overlap with the shooting-history card or bottom navigation.
- No app crash or exception stack in filtered Logcat.

Notes:

- 2026-07-16 had no existing visit record in the checked device data, so no dot was expected or forced.
- `docs/error_notes.md` was not updated for Step20B-2 because no new display bug, crash, build error, or runtime exception occurred during this adjustment.

Next:

- Pixel device confirmation if needed.
- After Pixel confirmation if needed, adjust only if a device-specific issue is found.

## Step20B-1 Visit Dot Display Fix

Status:

- Completed on 2026-07-27.
- Unit tests passed.
- `assembleDebug --console=plain --no-daemon` passed.
- Galaxy SC-03L `RF8M50DL2NA` real-device check completed with APK overwrite install.
- Scope was limited to the visit-day dot layout fix.
- No Kotlin, Compose, DB, Entity, DAO, Migration, photo-storage, CSV, calendar-library, Rokuyo calculation, or Japanese holiday calculation changes were added.

Confirmed on Galaxy:

- 2026-07-08 has 1 shooting-history row in the existing device data.
- 2026-07-08 Binder/accessibility state is `来店記録あり`.
- 2026-07-08 visit dot is visible in the real screenshot.
- 2026-07-08 visit dot actual bounds are 13px x 13px and inside the date cell.
- Dot is under Rokuyo and does not overlap Rokuyo.
- Dot remains visible when 2026-07-08 is selected.
- Dot remains visible after selecting another date.
- Dot remains visible after previous/next month button navigation.
- Dot remains visible after month swipe navigation.
- Negative target 2026-07-07 has no shooting history and no visit dot.
- 2026-07 5-week display is normal.
- 2026-08 6-week display is normal. Existing data had no August visit dots.
- Shooting-history row opens `CustomerDetailActivity`; Back returns to `CalendarActivity`.
- No overlap with the shooting-history card or bottom navigation.
- No app crash or exception stack in filtered Logcat.

Next:

- Pixel device confirmation if needed.
- Step20B-1 is complete.

## Step20B Rokuyo And Japanese Holiday Display

Status:

- Implemented.
- Unit tests passed.
- `assembleDebug --console=plain --no-daemon` passed.
- Galaxy SC-03L `RF8M50DL2NA` real-device check completed with APK overwrite install.
- No Kotlin, Compose, DB, Entity, DAO, Migration, photo-storage, CSV, or external runtime API changes were added.

Confirmed on Galaxy:

- Initial calendar display
- Sunday-to-Saturday 7 columns
- In-month dates present
- Month title
- Previous/next month buttons
- Month swipe
- Date selection and selected background movement
- Today background after selecting another date
- Sunday red, Saturday blue, holiday red
- Rokuyo display in all in-month cells
- Visit-dot persistence confirmed in Step20B-1 using 2026-07-08 existing data
- Calendar-history-to-customer-detail navigation confirmed in Step20B-1
- 5-week display
- 6-week display and 6th-row date selection
- No overlap with the shooting-history card or bottom navigation
- No app crash or exception stack in filtered Logcat

Not confirmed in this pass:

- Pixel 6a device confirmation.
- Final user review of visual balance, tap feel, and Master UI fit was completed after Step20B-2.

Next:

- If needed, provide the same APK for Pixel 6a confirmation.
- Step20B is complete.

## Step20A Kizitonwose Calendar移行・Galaxy実機確認済み

状態:

- `CalendarActivity`はJavaのまま、Kizitonwose Calendar View版 `2.10.1` へ移行済み。
- `assembleDebug --console=plain --no-daemon` 成功。
- Galaxy `RF8M50DL2NA` がADBで`device`状態になったことを確認済み。
- 既存データを保持したままDebug APKを上書きインストール済み。
- Galaxy実機でカレンダー基本操作、5週表示、6週表示、撮影履歴更新、顧客詳細遷移、戻る操作、Logcat確認まで完了。
- Kizitonwose移行に直接関係する不具合は見つからず、追加修正なし。

実装済み内容:

- Kizitonwose Calendar View版の依存関係追加。
- `minSdk 24`対応のためCore Library Desugaringを追加。
- Android標準`CalendarView`をKizitonwose `CalendarView`へ置き換え。
- 月タイトル、前月/翌月ボタン、曜日行、日付選択、今日表示、選択日表示を実装。
- 撮影日ドットを既存の`Photo.takenDate` / `PhotoRepository.listTakenDates()`から表示。
- 選択日の撮影履歴取得、顧客名と写真枚数の集約、履歴タップで顧客詳細へ遷移する既存仕様を維持。
- Activity再表示時に撮影日ドットと撮影履歴が更新されるようにした。
- 下部ナビゲーションとEdge-to-Edge / Insets対応を維持。

確認済み:

- Galaxy実機へのAPK上書きインストール。
- カレンダー初期表示。
- 日曜から土曜までの7列表示。
- 当月日付の欠けなし。
- 年月タイトル。
- 前月/翌月移動。
- 月スワイプ。
- 日付選択と選択背景の移動。
- 今日の淡い水色背景。
- 日曜日の赤色、土曜日の青色。
- 撮影日ドットと日付選択後のドット維持。
- 選択日の撮影履歴更新。
- 撮影履歴行から顧客詳細への遷移と戻る操作。
- 5週表示と6週表示。
- 撮影履歴カード、下部ナビゲーションとの重なりなし。
- Logcatで対象アプリ由来のクラッシュ/例外なし。

未確認:

- Pixel実機確認。

次に行うこと:

- 必要ならPixel実機でも最終確認する。
- Pixelで問題が出た場合のみ機種差調整を行う。

実機確認後の次候補:

- Pixel実機最終確認。

引き続き含めない:

- 六曜表示。
- 新しい祝日処理。
- 祝日データの追加。
- Visit Entity / Visitテーブル。
- DB Migration。
- 写真保存仕様の変更。
- CSV仕様の変更。

## Step19C 完了

完了内容:

- ADBフルパスで実機 `RF8M50DL2NA` を認識。
- 最新 `app-debug.apk` を実機へ上書きインストール済み。
- 顧客登録画面を実機表示し、スクリーンショット取得済み。
- 保存/修正ボタンの左アイコンが実機で表示されること。
- キャンセルボタンが白背景・細い枠線で表示されること。
- フリガナアイコンが「あア」に近い文字アイコン表示になっていること。
- 郵便番号アイコンが丸背景付きの封筒系アイコンになっていること。

追加調整候補:

- Master UIと比べると、保存ボタン左アイコンのサイズと位置はまだやや強く見えるため、必要ならさらに小さくして文字側へ寄せる。
- 顧客登録/編集画面でキーボード表示中でも下までスクロールでき、保存/修正/キャンセル操作ができることを確認する。
- 既存顧客の編集時に、登録済みの名前、フリガナ、電話番号、郵便番号、住所、メモが正しく表示されることを確認する。
- 新規登録、編集保存、必須入力チェック、キャンセル/戻る動作が従来どおり動くことを確認する。
- 写真保存、MediaStore表示、写真詳細左右移動、メモ保存、写真削除、顧客一覧ソートに回帰がないことを確認する。

注意:

- Step19Cは最新APKを実機へインストールして画面確認済み。完了扱い。
- メールアドレス欄は現行 `Customer` Entity に項目がなく、DB構造変更禁止のため今回追加していない。
- Room DB構造、Entity / DAO / Repository、写真保存処理、MediaStore処理、Calendarロジックは変更しない。
- push は未実施。

## Maintenance Note

作業終了ごとに、このファイルを更新すること。

更新する内容:

- 次に実装するStep
- レビュー待ちの内容
- 保留中の課題
- 実機確認で残っている確認項目
- 優先度が変わったタスク

作業終了時は、あわせて以下も確認・更新すること。

- `docs/current_status.md`
- `docs/error_notes.md`

## 優先度高

### アプリ内写真一覧

目的:

- アプリ内に保存されている写真を一覧表示する。
- 写真をタップすると、写真詳細画面へ遷移する。

進め方:

1. Step 1: 全写真取得処理追加済み。
2. Step 2: `PhotoListActivity` とレイアウト追加済み。
3. Step 3: `PhotoListAdapter` を追加し、サムネイル表示済み。
4. Step 4: 写真タップで `CustomerDetailActivity` へ遷移済み。
5. Step 5: `MainActivity` に「写真一覧」導線を追加済み。
6. Step 6-1: 実機確認A対応済み。写真一覧起動時の空表示チラつきを修正済み。
7. Step 6-2: 実機確認C記録済み。河上削除後、該当写真が残らないこととToast再発有無を確認する。
8. Step 6-3: 実機確認B対応済み。顧客名編集時に関連写真の`Photo.customerName`を同期する修正済み。
9. Step 6-4: 実機確認D記録済み。河上削除後、選択状態のような表示は解消したように見える。コード修正なし。
10. Step 7: 写真一覧タップ時の遷移先を`PhotoDetailActivity`へ変更済み。実機確認済み。
11. Step 8: 写真一覧に顧客名・メモ検索、並び順変更を追加済み。実機確認済み。
12. Step 8-2: 写真一覧の検索debounce、同一条件時の不要更新回避、Adapter反映の軽量化を追加済み。
13. Step 9: 写真削除後の前画面復帰・一覧再読み込みを確認し、削除確認文言を改善済み。
14. Step 10: 参考UIの雰囲気を反映し、余白、配色、カードUI、一覧表示、写真詳細、カレンダー表示を改善済み。実機確認済み。
15. Step 11: 実機確認で、Galaxyギャラリーに表示されない問題と顧客詳細写真タップ時に拡大表示されない問題を確認。
16. Step 11修正済み: 写真タップ導線を補強し、MediaStoreへ`ContentResolver.insert()` + `OutputStream`コピーする方式へ変更済み。
17. Step 11 MediaStore実機確認済み: 新規撮影写真がGalaxyギャラリーに表示されることを確認済み。
18. Step 11写真拡大表示再修正済み: 顧客詳細写真タップ処理と`PhotoDetailActivity`の写真表示領域を修正済み。
19. Step 11写真拡大表示追加修正済み: 閲覧専用`PhotoPreviewActivity`を追加し、顧客詳細の写真タップ先を全画面プレビューへ変更。Exif向き補正も追加済み。MediaStore周りは変更なし。
20. Step 11最終修正済み: 顧客詳細の写真タップ先を編集用`PhotoDetailActivity`へ戻し、`PhotoDetailActivity`の写真タップで`PhotoPreviewActivity`を開く導線へ変更。プレビューにピンチズーム、ドラッグ移動、ダブルタップ拡大/リセットを追加済み。MediaStore周りは変更なし。
21. Step 12実装済み: `PhotoDetailActivity`に同一顧客写真の左右移動を追加。左矢印は新しい写真、右矢印は古い写真へ移動し、写真情報・メモ・プレビュー対象・削除対象を現在写真に同期する。
22. Step 12実機確認OK: 写真詳細の左右移動、切り替え後のプレビュー、メモ保存、削除の確認完了。
23. Step 13実装済み: 写真一覧と顧客詳細写真グリッドで、サムネイルを表示サイズに合わせて縮小decodeするように変更済み。
24. Step 13実機確認結果: 写真一覧表示、クラッシュなし、高速スクロール時の別写真表示なし、検索、並び順、写真欠損時の安定性はOK。スクロールの重さは改善せず。
25. Step 13追加対応済み: Glideを導入し、写真一覧と顧客詳細写真グリッドの一覧サムネイルを非同期・キャッシュ対応に変更済み。写真詳細・プレビューの`PhotoImageLoader`は維持。
26. Step 13追加対応・実機確認OK: 写真一覧の正常表示、Glide導入後のスクロール改善、クラッシュなし、高速スクロール時の画像残りなし、検索、並び順、写真詳細遷移、顧客詳細写真グリッド、Step12左右移動、MediaStore登録への影響なしを確認済み。
27. アプリ内写真一覧はStep13まで完了扱い。

次候補タスク:

- 空状態表示・文言整理
- 写真ファイル欠損時の表示改善
- CSV出力改善
- バックアップ機能検討

注意:

- 既存の写真保存方式は変更しない。
- Step 11修正版以降に保存する写真は`Pictures/Okannokarte/`へMediaStoreコピー登録する。
- 既存写真の一括MediaStore登録は今回は行わない。
- DB構造は変更しない。

## 優先度中

### CSV出力改善

候補:

- 出力前の確認ダイアログ
- 出力件数のToast表示
- CSV出力対象件数の画面表示
- 年賀状ソフト向けの列追加検討

注意:

- `Customer` Entityは安易に変更しない。
- DB変更が必要な場合はMigration計画を先に作る。

### バックアップ機能

候補:

- DBバックアップ
- 写真ファイルバックアップ
- CSV以外のエクスポート

注意:

- Androidのストレージ制限を確認してから実装する。
- 外部ストレージ権限はむやみに追加しない。

### UI改善

候補:

- 文字化けしている表示文言の整理
- 顧客詳細画面の情報整理
- 写真一覧と顧客一覧の導線整理
- 空状態表示

注意:

- 既存機能を壊さない範囲で小さく進める。

## 優先度低

### 検索機能改善

候補:

- 住所検索
- メモ検索
- 撮影日の絞り込み
- 写真メモ検索

### 写真管理改善

候補:

- 写真ファイル欠損時の表示
- 写真一覧での撮影日グループ表示
- 写真削除時の確認文言改善

## Step14 完了

内容:

- 顧客一覧に、`Photo.takenDate` から取得した顧客ごとの最新撮影日を `最終撮影日：yyyy/MM/dd` 形式で表示する機能を実装済み。
- 写真がない顧客は `最終撮影日：未登録` と表示する。
- 検索結果でも同じ表示になるよう対応済み。
- `Customer` / `Photo` Entityのカラム追加なし。
- `assembleDebug` 成功。
- 実機確認OK。

次候補タスク:

- 空状態表示・文言整理などのUI改善。
- 写真ファイル欠損時の表示改善。
- CSV出力改善。
- バックアップ機能検討。

## Step15 完了

内容:

- アプリ全体の考え方を、写真データにもとづく `撮影履歴` に統一。
- 顧客一覧の表示を `最終撮影日` に変更。
- 顧客詳細画面とカレンダー画面の表示を `撮影履歴` に変更。
- データ取得方法、Calendarロジック、DB構造、Entity、DAO、Repository、Migrationは変更なし。
- `Visit` Entity / Visitテーブルは今回は採用見送り。
- 実機確認OK。

Visit Entityを採用しない理由:

- 美容室での実運用では、写真撮影時のみ記録する方が操作がシンプル。
- 来店時に毎回ボタンを押す運用を増やさないことで、操作忘れを防げる。
- 将来必要になれば、既存設計への影響を抑えながらVisitテーブルを追加できる。
