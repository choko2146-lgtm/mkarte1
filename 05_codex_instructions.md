# 05. Codex向け実装指示

## 1. 前提

このプロジェクトはAndroid Studioで作成したJavaベースのAndroidアプリです。

アプリ名は「おかんのカルテ」です。

## 2. 実装方針

- Javaで実装する
- XMLレイアウトを使用する
- Room Databaseを使用する
- 写真はMediaStoreまたはFileProviderを使って端末内に保存する
- 初期版ではCamera Intent方式を優先する
- 画面遷移はActivityベースで実装する
- まず動く最小構成を優先する

## 3. パッケージ構成案

```text
app/src/main/java/com/example/okannokarte/
 ├─ MainActivity.java
 ├─ ui/
 │   ├─ camera/
 │   │   ├─ CameraActivity.java
 │   │   └─ PhotoCustomerSelectActivity.java
 │   ├─ customer/
 │   │   ├─ CustomerRegisterActivity.java
 │   │   ├─ CustomerListActivity.java
 │   │   └─ CustomerDetailActivity.java
 │   └─ photo/
 │       └─ PhotoDetailActivity.java
 ├─ data/
 │   ├─ AppDatabase.java
 │   ├─ Customer.java
 │   ├─ Photo.java
 │   ├─ CustomerDao.java
 │   └─ PhotoDao.java
 ├─ repository/
 │   ├─ CustomerRepository.java
 │   └─ PhotoRepository.java
 └─ util/
     ├─ DateUtil.java
     ├─ FileNameUtil.java
     └─ PermissionUtil.java
```

## 4. 実装の優先順位

### Step 1
ホーム画面を作る。

- アプリ名表示
- カメラボタン
- 顧客登録ボタン
- 顧客一覧ボタン

### Step 2
Room Databaseを導入する。

- Customer Entity
- Photo Entity
- CustomerDao
- PhotoDao
- AppDatabase

### Step 3
顧客登録機能を作る。

- 顧客名
- フリガナ
- 電話番号
- 郵便番号
- 住所
- メモ

### Step 4
顧客一覧機能を作る。

- 一覧表示
- 検索
- 最終撮影日順

### Step 5
カメラ撮影機能を作る。

- Camera Intent
- 一時保存
- 撮影後顧客選択

### Step 6
写真と顧客の紐づけを作る。

- 顧客選択
- 未登録時の新規登録
- 正式保存
- Photo登録

### Step 7
顧客詳細画面を作る。

- 顧客情報表示
- 写真グリッド表示

### Step 8
削除機能を作る。

- 写真削除
- 顧客削除
- 確認ダイアログ

## 5. 注意点

- UIはシンプルでよい
- まずはエラーが少なく動くことを優先する
- 顧客情報と写真は個人情報として扱う
- 削除時は必ず確認ダイアログを表示する
- 写真ファイル名に使えない文字は `_` に置換する
