# Photo Feature Prompt

## 目的

アプリ内の写真管理機能を拡張する。

## 現在の写真保存方式

- 保存先: `getExternalFilesDir(Environment.DIRECTORY_PICTURES)`
- DB保存: `Photo.uri` に `file://` URI
- MediaStore登録なし
- 通常ギャラリー表示なし

## 注意

- 既存の写真保存方式は勝手に変更しない。
- MediaStore対応は別タスクとして扱う。
- ギャラリー保存対応は別タスクとして扱う。
- 写真ファイルが存在しない場合もクラッシュさせない。
- 顧客詳細への遷移は `photo.customerId` を使う。

## アプリ内写真一覧 Step例

1. `PhotoDao` / `PhotoRepository` に全写真取得処理を追加。
2. `PhotoListActivity` とレイアウトを追加。
3. `PhotoListAdapter` でサムネイル表示。
4. 写真タップで `CustomerDetailActivity` へ遷移。
5. `MainActivity` に写真一覧ボタンを追加。
6. 実機確認。

