# 09. 来店履歴・カレンダー機能 追加仕様書（CSV住所録出力追加版）

## 1. 目的

「おかんのカルテ」に、美容室向けの来店履歴管理機能とカレンダー機能を段階的に追加する。

現在は「写真単位」で履歴を管理しているが、将来的には「来店単位」で管理できる構成へ拡張する。

初期版のシンプルさを維持しながら、後から安全に機能追加できる設計を目指す。

---

## 2. 段階的な実装方針

### 第一段階

CustomerDetailActivity に、来店日ごとの写真グループ表示を追加する。

### 第二段階

CalendarActivity を追加し、日付ベースで来店履歴を確認できるようにする。

### 第三段階

Visit Entity を導入し、「来店単位」の正式なカルテ管理へ拡張する。

---

## 3. 第一段階：来店日ごとの写真表示

### 対象画面

- CustomerDetailActivity

### 変更後の表示例

```text
山田太郎

2026/05/20
[写真][写真]

2026/04/15
[写真][写真][写真]
```

### 表示仕様

- Photo.takenDate ごとに写真をグループ化する
- 新しい日付順で表示する
- 日付ヘッダーを表示する
- 写真は2〜3列のグリッド表示とする

---

## 4. 第一段階のDB変更

### DB変更

なし。

既存の Photo Entity を利用する。

### DAO追加案

```java
@Query("SELECT DISTINCT takenDate FROM photos WHERE customerId = :customerId ORDER BY takenDate DESC")
List<String> getDistinctTakenDates(long customerId);

@Query("SELECT * FROM photos WHERE customerId = :customerId AND takenDate = :takenDate ORDER BY createdAt DESC")
List<Photo> getPhotosByCustomerIdAndDate(long customerId, String takenDate);
```

---

## 5. RecyclerView構成案

```text
CustomerDetailActivity
 └─ VisitDateAdapter
     ├─ 来店日ヘッダー
     └─ PhotoGridAdapter
```

---

## 6. 第二段階：CalendarActivity追加

### 追加画面

| 画面ID | 画面名 | Activity案 |
|---|---|---|
| SCR-08 | カレンダー画面 | CalendarActivity |

---

## 7. ホーム画面への追加

```text
MainActivity
 ├─ CameraActivity
 ├─ CustomerRegisterActivity
 ├─ CustomerListActivity
 └─ CalendarActivity
```

### ホーム画面イメージ

```text
[ カメラ ]
[ 顧客登録 ]
[ 顧客一覧 ]
[ カレンダー ]
```

---

## 8. カレンダー画面仕様

### 表示内容

- 月間カレンダー
- 来店がある日のマーク表示
- 日付選択
- 選択日の来店履歴一覧
- 日本の祝日表示
- 六曜表示
- 六曜表示ON/OFF設定

---

## 9. カレンダー表示仕様

```text
土曜 → 青系
日曜・祝日 → 赤系
```

### 来店日表示

```text
●
```

---

## 10. 日本祝日表示仕様

### 表示内容

- 日本の祝日を表示する
- 祝日名を表示可能にする
- 日曜・祝日を赤系表示する

### 実装候補

```text
holiday_jp-java
```

---

## 11. 六曜表示仕様

### 表示対象

- 先勝
- 友引
- 先負
- 仏滅
- 大安
- 赤口

### 表示方針

- 小さい文字で表示する
- 設定でON/OFF可能にする

---

## 12. 「今日は何の日」機能

今回の実装対象外とする。

---

## 13. 第二段階のDB方針

### 初期方針

Photo.takenDate を利用する。

### DAO追加案

```java
@Query("SELECT DISTINCT takenDate FROM photos ORDER BY takenDate DESC")
List<String> getAllTakenDates();

@Query("SELECT * FROM photos WHERE takenDate = :takenDate ORDER BY createdAt DESC")
List<Photo> getPhotosByDate(String takenDate);
```

---

## 14. 第三段階：Visit Entity導入

### 現在のER構成

```text
Customer 1 --- N Photo
```

### 将来のER構成

```text
Customer 1 --- N Visit
Visit    1 --- N Photo
```

### Visit Entity案

```text
Visit
- id
- customerId
- visitDate
- menu
- memo
- nextVisitDate
- createdAt
- updatedAt
```

---

## 15. Visit Entity導入メリット

- 来店日管理
- 施術履歴管理
- 次回来店予定
- 使用薬剤履歴
- 来店メモ管理

---

## 16. Photo Entity変更案

### 変更前

```text
Photo
- customerId
```

### 変更後

```text
Photo
- visitId
```

---

## 17. CSV住所録出力機能（年賀状対応）

### 目的

顧客情報をCSV形式で出力し、パソコンの年賀状ソフトやExcelで利用できるようにする。

---

## 18. 対象画面

- CustomerListActivity

---

## 19. UI追加案

```text
[ CSV住所録出力 ]
```

または

```text
右上メニュー
 └─ CSV出力
```

---

## 20. 出力内容

| 項目 | DB項目 |
|---|---|
| 顧客名 | name |
| 郵便番号 | postalCode |
| 住所 | address |

---

## 21. CSV出力例

```csv
顧客名,郵便番号,住所
山田太郎,123-4567,東京都〇〇区〇〇1-2-3
佐藤花子,987-6543,大阪府〇〇市〇〇4-5-6
```

---

## 22. 出力仕様

- UTF-8形式で保存
- .csv形式
- Android共有機能対応
- Excelで開けることを想定

---

## 23. 保存先案

```text
Documents/おかんのカルテ/
```

---

## 24. ファイル名案

```text
customer_address_yyyyMMdd.csv
```

---

## 25. CustomerDao追加案

```java
@Query("""
SELECT name, postalCode, address
FROM customers
WHERE address IS NOT NULL
AND address != ''
ORDER BY name
""")
List<CustomerAddressExport> getCustomerAddressList();
```

---

## 26. DTO案

```java
public class CustomerAddressExport {
    public String name;
    public String postalCode;
    public String address;
}
```

---

## 27. CSV生成フロー

```text
CustomerListActivity
 ↓
CSV Export Utility
 ↓
Documentsへ保存
 ↓
共有Intent
```

---

## 28. 将来追加しやすい機能

- 宛名面直接印刷
- 年賀状ソフト連携
- CSV項目選択
- DM発送履歴
- 誕生日DM

---

## 29. テスト追加項目

| No | 確認内容 | 期待結果 |
|---|---|---|
| 1 | CSV出力ボタン押下 | CSV生成される |
| 2 | Excelで開く | 文字化けしない |
| 3 | 郵便番号あり | 正しく出力 |
| 4 | 住所未入力 | 空欄出力 |
| 5 | 共有機能 | PCへ送信できる |

---

## 30. 実装時の注意点

- まずは既存DBを大きく変えない
- Photo.takenDate を最大限活用する
- CalendarActivityは独立した画面として作る
- CustomerListActivityに機能を詰め込みすぎない
- 六曜表示は後回しでもよい
- 初期版では動作安定性を優先する
