# Architecture

## 全体構成

現在の実装は、厳密なMVVMではなく、以下に近い軽量構成です。

```text
Activity
  -> Repository
    -> Room Dao
      -> Room Entity
```

ViewModelは未導入です。Activityが画面制御を持ち、RepositoryがDBアクセスの非同期化を担当しています。

## Repository構成

### CustomerRepository

担当:

- 顧客登録
- 顧客更新
- 顧客取得
- 顧客一覧
- 顧客検索
- 顧客削除
- CSV住所録用データ取得

### PhotoRepository

担当:

- 写真登録
- 写真更新
- 写真取得
- 顧客別写真一覧
- 日付別写真一覧
- 撮影日一覧
- 全写真一覧
- 写真削除

## Room Entity一覧

### Customer

テーブル:

- `customers`

主な項目:

- `id`
- `name`
- `kana`
- `phone`
- `postalCode`
- `address`
- `memo`
- `createdAt`
- `updatedAt`

### Photo

テーブル:

- `photos`

主な項目:

- `id`
- `customerId`
- `customerName`
- `takenDate`
- `fileName`
- `uri`
- `memo`
- `createdAt`

Relation:

- `Photo.customerId` -> `Customer.id`
- `onDelete = ForeignKey.CASCADE`

## DAO一覧

### CustomerDao

主な処理:

- `insert`
- `update`
- `delete`
- `getById`
- `getAll`
- `getAllOrderByLatestPhoto`
- `search`
- `getCustomerAddressList`

### PhotoDao

主な処理:

- `insert`
- `update`
- `delete`
- `getById`
- `getAllPhotos`
- `getByCustomerId`
- `getByTakenDate`
- `getDistinctTakenDates`
- `countByCustomerId`
- `latestTakenDate`

## Calendar関連

主なファイル:

- `CalendarActivity`
- `VisitHistoryAdapter`

データ取得:

- `PhotoRepository.listTakenDates()`
- `PhotoRepository.listForDate(String takenDate)`

遷移:

- 日付別の来店顧客から `CustomerDetailActivity` へ遷移。
- extra名は `"customerId"`。

## Photo関連

主なファイル:

- `CameraActivity`
- `PhotoCustomerSelectActivity`
- `PhotoDetailActivity`
- `PhotoAdapter`
- `VisitDateAdapter`
- `PhotoFileUtil`

保存方式:

- `getExternalFilesDir(Environment.DIRECTORY_PICTURES)` 配下に保存。
- DBには `Photo.uri` として `file://` URIを保存。

注意:

- MediaStoreには登録していない。
- 通常のギャラリー表示対応は未実装。
- 既存写真保存方式を変える場合は、移行方針を先に決める。

## CSV住所録関連

主なファイル:

- `CustomerAddressExport`
- `CustomerAddressCsvUtil`
- `CsvShareUtil`

取得:

- `CustomerDao.getCustomerAddressList()`
- `CustomerRepository.listCustomerAddresses()`

共有:

- FileProvider
- `ACTION_SEND`
- MIME type: `text/csv`

## Navigation構成

主な導線:

- `MainActivity` -> `CameraActivity`
- `MainActivity` -> `CustomerRegisterActivity`
- `MainActivity` -> `CustomerListActivity`
- `MainActivity` -> `CalendarActivity`
- `CustomerListActivity` -> `CustomerDetailActivity`
- `CustomerDetailActivity` -> `CustomerRegisterActivity`
- `CustomerDetailActivity` -> `PhotoDetailActivity`
- `CalendarActivity` -> `CustomerDetailActivity`
- `CameraActivity` -> `PhotoCustomerSelectActivity`
- `PhotoCustomerSelectActivity` -> `CustomerRegisterActivity`

