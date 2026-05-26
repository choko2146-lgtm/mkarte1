# 03. Room Database設計

## 1. 採用DB

Room Databaseを採用する。

## 2. 採用理由

- Android公式系のDBライブラリである
- 顧客情報と写真情報を関連付けて管理しやすい
- 検索・並び替え・履歴表示を実装しやすい
- SQLiteを直接扱うよりコードの見通しが良い

## 3. Entity構成

- Customer
- Photo

## 4. Customer Entity

| フィールド | 型 | 必須 | 内容 |
|---|---|---|---|
| id | long | 必須 | 主キー・自動採番 |
| name | String | 必須 | 顧客名 |
| kana | String | 任意 | フリガナ |
| phone | String | 任意 | 電話番号 |
| postalCode | String | 任意 | 郵便番号 |
| address | String | 任意 | 住所 |
| memo | String | 任意 | 顧客メモ |
| createdAt | long | 必須 | 登録日時 |
| updatedAt | long | 必須 | 更新日時 |

## 5. Photo Entity

| フィールド | 型 | 必須 | 内容 |
|---|---|---|---|
| id | long | 必須 | 主キー・自動採番 |
| customerId | long | 必須 | 顧客ID |
| customerName | String | 必須 | 撮影時点の顧客名 |
| takenDate | String | 必須 | yyyyMMdd |
| fileName | String | 必須 | 写真ファイル名 |
| uri | String | 必須 | 保存Uri |
| memo | String | 任意 | 写真メモ |
| createdAt | long | 必須 | 登録日時 |

## 6. リレーション

```text
Customer 1 --- N Photo
```

1人の顧客に複数写真を紐づける。

## 7. CustomerDaoに必要な処理

- 顧客登録
- 顧客更新
- 顧客削除
- ID指定取得
- 全件取得
- 顧客名・フリガナ・電話番号検索
- 最終撮影日順取得

## 8. PhotoDaoに必要な処理

- 写真登録
- 写真削除
- 顧客ID指定で写真一覧取得
- 写真ID指定取得
- 顧客ID指定で写真件数取得
- 顧客ID指定で最新撮影日取得
