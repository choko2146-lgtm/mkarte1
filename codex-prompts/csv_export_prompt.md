# CSV Export Prompt

## 目的

顧客一覧画面から年賀状ソフト向け住所録CSVを出力する。

## CSV仕様

- 文字コード: UTF-8
- ファイル名: `customer_address_yyyyMMdd.csv`
- 出力列: `顧客名,郵便番号,住所`
- 住所未入力顧客は除外
- 保存先は `context.getCacheDir()`
- 共有は `ACTION_SEND`
- URIはFileProviderの `content://` を使う

## 実装方針

- `Customer` Entityは変更しない。
- DB構造は変更しない。
- `CustomerAddressExport` DTOを使う。
- CSV生成はUtilityに分離する。
- 共有処理はUtilityに分離する。
- 外部ストレージ権限は追加しない。
- 既存写真保存処理に触らない。

## Step例

1. 住所録取得DAO/Repositoryを追加。
2. CSV生成Utilityを追加。
3. FileProvider設定と共有Utilityを追加。
4. 顧客一覧画面に接続。
5. 実機確認。

