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
- 来店履歴カレンダー
- 日付別来店顧客表示
- CSV住所録出力

## 作業途中機能

- アプリ内写真一覧

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
- Step 7完了: 写真一覧タップ時の遷移先を`PhotoDetailActivity`へ変更。
- 次はStep 7: 実機確認。

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
- 日付別の来店顧客表示
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

注意:

- MediaStore登録はしていない。
- 通常のギャラリーには基本的に表示されない。
- アプリ内では `Photo.uri` に保存した `file://` URIで表示している。
