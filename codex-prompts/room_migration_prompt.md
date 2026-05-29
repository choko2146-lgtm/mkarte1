# Room Migration Prompt

## 目的

RoomのDB構造変更が必要になった場合に、安全にMigrationを行う。

## 原則

- Entity変更前に現在のDB versionを確認する。
- 既存データを消さない。
- versionを上げる。
- Migrationを明示的に追加する。
- destructive migrationは原則使わない。
- 実機の既存データで確認する。

## 実装前に確認すること

- 変更するEntity
- 追加、削除、変更するカラム
- 既存データへの影響
- 初期値が必要か
- DAOの影響
- Repositoryの影響
- UIの影響

## 報告内容

- DB変更内容
- Migration SQL
- version変更
- 既存データ保持方針
- assembleDebug結果
- 実機確認ポイント

