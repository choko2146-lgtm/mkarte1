# Current Status

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
