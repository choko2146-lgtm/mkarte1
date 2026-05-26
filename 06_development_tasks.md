# 06. 開発タスク一覧

## Phase 1: プロジェクト準備

- [ ] Android StudioでJavaプロジェクトを作成
- [ ] アプリ名を「おかんのカルテ」に設定
- [ ] 最小SDKを決定
- [ ] Room Database依存関係を追加
- [ ] 必要権限をAndroidManifest.xmlに追加

## Phase 2: ホーム画面

- [ ] MainActivity.javaを作成
- [ ] activity_main.xmlを作成
- [ ] アプリ名を表示
- [ ] カメラボタンを作成
- [ ] 顧客登録ボタンを作成
- [ ] 顧客一覧ボタンを作成
- [ ] 各画面へのIntent遷移を実装

## Phase 3: DB実装

- [ ] Customer.javaを作成
- [ ] Photo.javaを作成
- [ ] CustomerDao.javaを作成
- [ ] PhotoDao.javaを作成
- [ ] AppDatabase.javaを作成
- [ ] Repositoryを作成

## Phase 4: 顧客登録

- [ ] CustomerRegisterActivity.javaを作成
- [ ] activity_customer_register.xmlを作成
- [ ] 顧客名入力欄
- [ ] フリガナ入力欄
- [ ] 電話番号入力欄
- [ ] 郵便番号入力欄
- [ ] 住所入力欄
- [ ] メモ入力欄
- [ ] 登録ボタン
- [ ] 入力チェック
- [ ] DB保存

## Phase 5: 顧客一覧

- [ ] CustomerListActivity.javaを作成
- [ ] activity_customer_list.xmlを作成
- [ ] RecyclerViewを配置
- [ ] 顧客一覧Adapterを作成
- [ ] 検索欄を作成
- [ ] 顧客名検索
- [ ] フリガナ検索
- [ ] 電話番号検索
- [ ] 最終撮影日順表示

## Phase 6: カメラ撮影

- [ ] CameraActivity.javaを作成
- [ ] 撮影ボタンを作成
- [ ] カメラ権限チェック
- [ ] Camera Intent起動
- [ ] 一時保存ファイル作成
- [ ] 撮影後顧客選択画面へ遷移

## Phase 7: 撮影後顧客選択

- [ ] PhotoCustomerSelectActivity.javaを作成
- [ ] 撮影写真プレビュー表示
- [ ] 顧客検索
- [ ] 顧客一覧表示
- [ ] 新規顧客登録ボタン
- [ ] 紐づけ保存ボタン
- [ ] 正式ファイル名生成
- [ ] Photo Entity保存

## Phase 8: 顧客詳細・写真表示

- [ ] CustomerDetailActivity.javaを作成
- [ ] 顧客情報表示
- [ ] 写真グリッド表示
- [ ] PhotoDetailActivity.javaを作成
- [ ] 写真詳細表示
- [ ] 写真メモ表示

## Phase 9: 削除処理

- [ ] 写真削除確認ダイアログ
- [ ] Photo削除
- [ ] 端末写真ファイル削除
- [ ] 顧客削除確認ダイアログ
- [ ] 顧客削除
- [ ] 関連Photo削除
- [ ] 関連写真ファイル削除
