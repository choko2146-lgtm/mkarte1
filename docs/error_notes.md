# Error Notes

## Maintenance Note

作業終了ごとに、エラー・不具合・詰まった点があれば、このファイルへ追記すること。

蓄積対象:

- Android Studioエラー
- Gradleエラー
- Room migration問題
- Emulator問題
- Git問題
- 実機確認で見つかった不具合
- Codex作業中の原因調査メモ

作業終了時は、あわせて以下も確認・更新すること。

- `docs/current_status.md`
- `docs/next_tasks.md`

## Error Categories

### Android Studio

記録する内容:

- エラー内容
- 発生した操作
- 原因
- 解決方法
- 解決後の効果

### Gradle

記録する内容:

- 失敗したタスク
- エラーメッセージ
- 原因
- 実行したコマンド
- 解決方法

### Room Migration

記録する内容:

- 変更したEntity
- DB version
- Migration内容
- 既存データへの影響
- 実機確認結果

### Emulator

記録する内容:

- 端末/API
- 症状
- 原因
- 解決方法
- 実機との差分

### Git

記録する内容:

- 実行したGitコマンド
- エラー内容
- 原因
- 解決方法
- push/commit状況

## Error Note Template

```md
## タイトル

分類:

- Android Studio / Gradle / Room Migration / Emulator / Git / 実機不具合 / その他

発生日:

- yyyy-MM-dd

エラー内容:

- 

発生条件:

- 

原因:

- 

解決方法:

- 

解決後の効果:

- 

関連ファイル:

- 
```

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

## assembleDebug実行時にJDK参照が不正になる

分類:

- Gradle

発生日:

- 2026-06-02

エラー内容:

- `JAVA_HOME is not set and no 'java' command could be found in your PATH.`
- `jlink executable C:\Users\YRhei\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64\bin\jlink.exe does not exist.`

発生条件:

- 写真一覧機能Step 5の確認で `assembleDebug` を実行した時。

原因:

- シェル環境で `JAVA_HOME` が未設定だった。
- 既存のGradle daemonがVS Code拡張内のJREを参照していた。

解決方法:

- Android Studio同梱JBRを `JAVA_HOME` に指定した。
- Gradle daemonを停止してから再度 `assembleDebug` を実行した。

解決後の効果:

- `assembleDebug` が成功した。

関連ファイル:

- なし

## 写真一覧で特定顧客の詳細を開けない

分類:

- 実機不具合 / 写真一覧 / データ不整合

発生日:

- 2026-06-02

エラー内容:

- 写真一覧で「河上」の写真をタップすると「顧客情報を開けません」と表示された。

発生条件:

- 写真一覧画面で、過去データに紐づく可能性がある「河上」の写真をタップした時。

原因推測:

- 過去データに不正な`customerId`を持つ`Photo`レコードが存在していた可能性。
- 顧客削除・再登録などで`Photo.customerId`と`Customer.id`の整合が崩れていた可能性。

確認した既存仕様:

- `Photo.customerId`は`Customer.id`へのForeignKeyで、`onDelete = ForeignKey.CASCADE`が設定されている。
- `PhotoListActivity`では`photo == null`または`customerId <= 0`の場合にToastを表示し、クラッシュを回避している。

対応:

- ユーザー操作により該当顧客「河上」を削除。
- `Photo.customerId`は`Customer.id`へCASCADE設定されているため、関連`Photo`レコードも削除される想定。
- 既存の`customerId`不正時Toastによりクラッシュは回避済み。

今後確認:

- 写真一覧に該当写真が残らないこと。
- 「顧客情報を開けません」Toastが再発しないこと。

関連ファイル:

- `app/src/main/java/com/example/mkarte1/data/Photo.java`
- `app/src/main/java/com/example/mkarte1/data/Customer.java`
- `app/src/main/java/com/example/mkarte1/data/PhotoDao.java`
- `app/src/main/java/com/example/mkarte1/PhotoListActivity.java`
- `app/src/main/java/com/example/mkarte1/PhotoListAdapter.java`

## 顧客一覧で特定顧客の行が選択状態のように見える

分類:

- 実機不具合 / 顧客一覧 / 表示状態

発生日:

- 2026-06-02

エラー内容:

- 顧客一覧で「河上」の行のみ、選択状態またはアクティブ状態のような水色〜緑色の枠が常時表示されていた。

発生条件:

- 顧客一覧画面で「河上」の行を表示した時。

原因推測:

- 既存不整合データの影響。
- RecyclerViewのViewHolder再利用時にselected / activated状態が残っていた可能性。
- 既存コードでは`selected` / `activated`は明示的に使っていないが、`CustomerAdapter`に`selectedId`一致時だけ薄い緑背景にする処理がある。

対応:

- ユーザー操作により該当顧客「河上」を削除。
- 削除後、現象は解消したように見える。
- 現時点では他顧客で再発なし。
- 今回はコード修正せず、実機確認記録として残す。

今後確認:

- 他の顧客で同様の表示状態が再発しないか確認。
- 再発した場合は`CustomerAdapter`の`onBindViewHolder()`でselected / activated / 背景状態の明示リセットを検討する。

関連ファイル:

- `app/src/main/java/com/example/mkarte1/ui/customer/CustomerAdapter.java`
- `app/src/main/java/com/example/mkarte1/ui/customer/CustomerListActivity.java`
