# おかんのカルテ Codex開発資料

このフォルダは、Android Studio + VS Code Codexで「おかんのカルテ」を開発するためのMarkdown資料一式です。

## アプリ概要

「おかんのカルテ」は、美容室・個人サロン向けのAndroid顧客カルテアプリです。

主な目的は、顧客情報と施術写真を端末内で管理し、撮影した写真に「顧客名」「撮影日」「メモ」を紐づけることです。

## 開発方針

- Android Studioで開発する
- 言語はJava
- UIはXMLレイアウト
- DBはRoom Database
- 写真は端末内のPictures配下に保存
- 撮影後に顧客を選択して紐づける
- 顧客未登録の場合は、撮影後に新規登録できるようにする

## ファイル構成

| ファイル | 内容 |
|---|---|
| 01_spec.md | アプリ全体仕様 |
| 02_screen_design.md | 画面設計 |
| 03_database_design.md | Room Database設計 |
| 04_photo_flow.md | 撮影・保存フロー |
| 05_codex_instructions.md | Codex向け実装指示 |
| 06_development_tasks.md | 開発タスク一覧 |
| 07_test_plan.md | テスト観点 |
| 08_future_extensions.md | 将来拡張案 |

## Codexでの使い方

1. Android StudioでJavaのAndroidプロジェクトを作成する
2. VS Codeでプロジェクトフォルダを開く
3. このMarkdown一式をプロジェクト直下または `docs/` 配下に配置する
4. Codexに `05_codex_instructions.md` を読ませる
5. `06_development_tasks.md` の順番に実装を進める
