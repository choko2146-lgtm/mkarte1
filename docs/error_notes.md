# Error Notes

## 新規顧客登録ができない

発生日:

- CSV住所録出力実装後、顧客編集対応を追加した後の実機確認時。

エラー内容:

- 写真撮影後に「顧客新規登録」をタップし、顧客情報を入力して「登録する」を押しても、顧客一覧に表示されない。

原因:

- `CustomerRegisterActivity` の編集対応で、新規登録時にも `customer.id = customerId` を実行していた。
- 写真撮影後の新規登録では `customerId` が渡されないため、初期値 `-1` が主キーに入っていた。
- Roomの `@PrimaryKey(autoGenerate = true)` は、新規登録時にIDを未設定のまま渡す必要がある。

解決方法:

- 編集モードのときだけ `customer.id` を設定するように修正。

```java
if (isEditMode()) {
    customer.id = customerId;
}
```

解決後の効果:

- 写真撮影後の新規登録でRoomの自動採番が復旧。
- 写真なし新規登録も従来通り動作。
- 登録済み顧客編集では既存IDを使った更新が維持される。

## 登録済み顧客の再編集ができない

発生日:

- CSV住所録出力機能の実機確認中。

エラー内容:

- 顧客一覧から登録済み顧客の再編集ができない。

原因:

- `CustomerDao.update()` は存在していたが、Repository経由の更新メソッド、詳細画面の編集ボタン、登録画面の編集モードが未実装だった。
- CSV住所録出力ボタンによって顧客行クリックが壊れたわけではなかった。

解決方法:

- `CustomerRepository.update()` を追加。
- `CustomerDetailActivity` に編集ボタン導線を追加。
- `CustomerRegisterActivity` で `customerId` が渡された場合に既存顧客を読み込み、保存時に `update()` するように修正。

解決後の効果:

- 顧客詳細画面から既存顧客を編集できる。
- 新規登録と編集が同じ画面で分岐できる。

## 写真保存場所が分からない

調査内容:

- 写真は `getExternalFilesDir(Environment.DIRECTORY_PICTURES)` 配下に保存されている。

想定パス:

```text
/storage/emulated/0/Android/data/com.example.mkarte1/files/Pictures/<APP_FOLDER>/<顧客名>/
```

原因:

- アプリ専用外部領域のため、通常のギャラリーには基本的に表示されない。
- Android 11以降では端末のファイルアプリから `Android/data` 配下が見えにくい場合がある。

確認方法:

- PC接続で `Android/data/com.example.mkarte1/files/Pictures/` を確認する。
- アプリ内では `Photo.uri` に保存されたURIから表示される。

