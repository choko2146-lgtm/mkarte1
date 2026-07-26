# Kizitonwose Calendar導入時の実装言語選定調査

調査日:

- 2026-07-25

結論:

- 推奨案: `CalendarActivityはJavaのまま実装する`
- Kizitonwose CalendarのView版はKotlin製だが、移行に必要な主要APIはJavaから呼び出し可能。
- `MonthDayBinder`、`ViewContainer`、`CalendarView.setup(...)`、`scrollToMonth(...)`、`notifyDateChanged(...)`、`notifyMonthChanged(...)`、月スクロールリスナーの最小Javaコードはコンパイルできた。
- 公式サンプルがKotlin中心であることは判断材料にとどめ、現在のプロジェクト構成ではKotlinプラグインを追加しないJava案の方が移行時の安全性と管理しやすさが高い。

## 調査概要

現在のカレンダー画面をKizitonwose CalendarのView版へ移行する前に、次の2案を比較した。

- 選択肢A: 既存の`CalendarActivity`をJavaのまま維持し、JavaからKizitonwose Calendarを利用する。
- 選択肢B: カレンダー画面だけKotlinへ変更し、他の既存Javaコードは維持する。

この調査ではアプリ本体のソース、Gradle設定、レイアウトXML、DB構造は変更していない。変更したのは指定された`docs`のみ。

## 現在のプロジェクト構成

| 項目 | 現在値 |
| --- | --- |
| Android Gradle Plugin | `9.2.1` |
| Gradle | `9.4.1` |
| `compileSdk` | `36.1` |
| `targetSdk` | `36` |
| `minSdk` | `24` |
| Java `sourceCompatibility` | `JavaVersion.VERSION_11` |
| Java `targetCompatibility` | `JavaVersion.VERSION_11` |
| JDK | JetBrains JDK `21.0.10` |
| Kotlin Android plugin | 明示的な導入なし |
| Kotlin標準ライブラリ | `debugRuntimeClasspath`上に`org.jetbrains.kotlin:kotlin-stdlib:2.2.10`あり |
| Core Library Desugaring | 未導入 |
| ViewBinding | 未使用 |
| DataBinding | 未使用 |
| 現在のカレンダー | Android標準`android.widget.CalendarView` |
| 現在のCalendarActivity | Java |

確認した主なファイル:

- `build.gradle.kts`
- `app/build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle.properties`
- `app/src/main/java/com/example/mkarte1/ui/calendar/CalendarActivity.java`
- `app/src/main/res/layout/activity_calendar.xml`
- `app/src/main/java/com/example/mkarte1/ui/calendar/VisitHistoryAdapter.java`
- `app/src/main/java/com/example/mkarte1/data/PhotoDao.java`
- `app/src/main/java/com/example/mkarte1/repository/PhotoRepository.java`
- `app/src/main/AndroidManifest.xml`

補足:

- 依頼文では現在のMaterialCalendarView移行とあるが、実際の現行コードでは外部`MaterialCalendarView`ではなくAndroid標準`CalendarView`を使用している。
- `CalendarActivity`は`PhotoRepository`、`VisitHistoryAdapter`、`EdgeToEdgeUtil`、`MkarteBottomNav`、`CustomerDetailActivity`、`Photo`に依存している。

## Kizitonwose Calendar調査対象バージョン

調査対象:

- `com.kizitonwose.calendar:view:2.10.1`

確認根拠:

- Kizitonwose Calendar公式GitHub Releasesで`2.10.1`を最新リリースとして確認。
- Maven Centralで`com.kizitonwose.calendar:view:2.10.1`の成果物を確認。
- 公式READMEと`docs/View.md`でView版の導入方法、XML、`CalendarView`、`MonthDayBinder`、`ViewContainer`、`setup(...)`の利用形を確認。

注意:

- このリポジトリにはKizitonwose Calendarを導入していない。
- Maven Centralから一時ディレクトリへAAR/JARを取得し、JVMシグネチャとJavaコンパイルだけを検証した。

## Core Library Desugaring

Kizitonwose CalendarのView版は`java.time.LocalDate`、`java.time.YearMonth`、`java.time.DayOfWeek`を使う。

現在の`minSdk`は`24`で、Android API 26未満を含むため、移行時にはCore Library Desugaringが必要。

現在の状態:

- `compileOptions`に`isCoreLibraryDesugaringEnabled = true`は未設定。
- `dependencies`に`coreLibraryDesugaring(...)`は未設定。

移行時に必要なGradle変更:

- `app/build.gradle.kts`の`compileOptions`へ`isCoreLibraryDesugaringEnabled = true`を追加。
- `gradle/libs.versions.toml`へ`desugar_jdk_libs`のバージョンとライブラリエントリを追加。
- `app/build.gradle.kts`へ`coreLibraryDesugaring(libs.desugar.jdk.libs)`を追加。

この変更はJava案でもKotlin案でも必要。

## 現在のカレンダー機能

| 機能 | 現在の実装 |
| --- | --- |
| 当月の初期表示 | `CalendarView`標準表示、`CalendarActivity.onCreate()`で`loadVisitHistories(formatTakenDate(calendarView.getDate()))` |
| 前月・翌月への移動 | `CalendarView`標準UI |
| 月表示タイトル | `CalendarView`内部表示 |
| 曜日表示 | `CalendarView`内部表示 |
| 日付選択 | `calendarView.setOnDateChangeListener(...)` |
| 今日の日付表示 | `CalendarView`標準表示 |
| 土曜日の文字色 | `CalendarView`標準表示に依存、独自制御なし |
| 日曜日の文字色 | `CalendarView`標準表示に依存、独自制御なし |
| 祝日の文字色 | 未実装 |
| 来店日ドット | 未実装。`PhotoRepository.listTakenDates()`と`PhotoDao.getDistinctTakenDates()`は利用可能 |
| 選択日の撮影履歴取得 | `PhotoRepository.listForDate(String takenDate)` |
| 顧客名と写真枚数の集約 | `CalendarActivity.groupByCustomer(List<Photo>)` |
| 撮影履歴タップ | `VisitHistoryAdapter`のクリックコールバック |
| 顧客詳細への遷移 | `Intent(this, CustomerDetailActivity.class)`に`customerId`を付与 |
| Activity再表示時の更新 | `onResume()`での再読込は未実装。`onCreate()`と日付選択時のみ |
| 写真保存後の来店日反映 | 写真保存時に`Photo.takenDate`へ当日`yyyyMMdd`を保存。カレンダー再表示・日付選択時に反映 |
| 下部ナビゲーション | `MkarteBottomNav.bind(this, R.id.navCalendar)` |
| Edge-to-Edge / Insets | `EdgeToEdgeUtil.apply(this)` |

現在の撮影履歴データの流れ:

1. 写真保存時に`Photo.takenDate`へ`DateUtil.todayYmd()`を保存。
2. `PhotoDao.getByTakenDate(String takenDate)`で日付別写真を取得。
3. `PhotoRepository.listForDate(...)`でUIスレッドへ結果を返す。
4. `CalendarActivity.groupByCustomer(...)`で顧客ごとの写真枚数に集約。
5. `VisitHistoryAdapter`で顧客名と`写真n枚`を表示。

## Javaから利用する場合の検証結果

`javap`で確認した主要シグネチャ:

```text
CalendarView extends androidx.recyclerview.widget.RecyclerView
void setup(YearMonth startMonth, YearMonth endMonth, DayOfWeek firstDayOfWeek)
void scrollToMonth(YearMonth month)
void notifyDateChanged(LocalDate date)
void notifyDateChanged(LocalDate date, DayPosition position)
void notifyMonthChanged(YearMonth month)
void notifyCalendarChanged()
CalendarMonth findFirstVisibleMonth()
void setDayBinder(MonthDayBinder<?> binder)
void setMonthScrollListener(Function1<? super CalendarMonth, Unit> listener)
```

```text
ViewContainer(View view)
View getView()

interface Binder<Data, Container extends ViewContainer> {
    Container create(View view);
    void bind(Container container, Data data);
}

interface MonthDayBinder<Container extends ViewContainer>
    extends Binder<CalendarDay, Container>
```

```text
CalendarDay(LocalDate date, DayPosition position)
LocalDate getDate()
DayPosition getPosition()

CalendarMonth
YearMonth getYearMonth()
List<List<CalendarDay>> getWeekDays()

DayPosition.InDate
DayPosition.MonthDate
DayPosition.OutDate
```

Java最小コード検証:

- `ViewContainer`を継承した`DayContainer`をJavaで作成できた。
- `MonthDayBinder<DayContainer>`をJavaの匿名クラスで実装できた。
- `CalendarView.setDayBinder(...)`を呼び出せた。
- `CalendarView.setup(...)`を`YearMonth`と`DayOfWeek`で呼び出せた。
- `CalendarView.scrollToMonth(...)`を呼び出せた。
- 月スクロールリスナーをJavaラムダで設定できた。
- `CalendarView.notifyDateChanged(LocalDate)`を呼び出せた。
- `CalendarView.notifyMonthChanged(YearMonth)`を呼び出せた。
- `javac`でコンパイル成功。

Javaで必要になる補足:

- `monthScrollListener`はKotlin関数型のため、Javaラムダでは`return Unit.INSTANCE;`が必要。
- 公式サンプルの`firstDayOfWeekFromLocale()`のようなKotlin拡張関数は使わず、Javaでは`WeekFields.of(Locale.getDefault()).getFirstDayOfWeek()`で代替できる。
- `object : Interface`はJavaの匿名クラスで代替できる。
- 名前付き引数やデフォルト引数はJavaからは使わない。必要な値を通常の引数で渡す。
- `notifyDateChanged(LocalDate)`のJava向けオーバーロードがあり、標準的に呼び出せる。

結論:

- JavaからのAPI利用は可能。
- Java案で著しく不自然な箇所は、月スクロールリスナーの`Unit.INSTANCE`程度。
- 日付セルBinder、クリック処理、選択状態更新、月移動、部分更新はJavaで十分実装できる。

## Kotlinを導入する場合の必要変更

カレンダー画面だけKotlinにする場合でも、アプリ全体をKotlin化する必要はない。

ただし、現在のプロジェクトでは明示的なKotlin Android pluginがないため、次の変更が必要になる。

- `gradle/libs.versions.toml`へKotlin pluginバージョンを追加。
- ルート`build.gradle.kts`へ`org.jetbrains.kotlin.android` plugin aliasを`apply false`で追加。
- `app/build.gradle.kts`へKotlin Android pluginを適用。
- `app/build.gradle.kts`へKotlin JVM targetをJava 11と整合するよう設定。
- Kizitonwose Calendar依存関係を追加。
- Core Library Desugaringを追加。

既存Javaとの連携:

- Kotlinから`PhotoRepository`、`PhotoDao`、`VisitHistoryAdapter`、`CustomerDetailActivity`など既存Javaクラスは呼び出し可能。
- `CalendarActivity.kt`として同じ完全修飾名を維持すれば、基本的にManifestのActivity名は変更不要。
- ViewBindingは未使用のため、Kotlin化しても現状どおり`findViewById`で実装可能。

Kotlin化の選択肢:

1. `CalendarActivity.java`を`CalendarActivity.kt`へ置き換える。
2. `CalendarActivity`はJavaのままにし、Binderなどライブラリ依存部分だけKotlinクラスへ分離する。

評価:

- 1は公式サンプルに近いが、Activity全体の変換が必要。
- 2はJava/Kotlinの分割境界が増え、今の小規模な画面ではかえって複雑になりやすい。

## 六曜・祝日・来店日ドットへの適性

将来の日付セルでは以下を想定する。

```text
日付数字
六曜
来店日ドット
```

比較:

| 項目 | Javaのまま | カレンダーだけKotlin |
| --- | --- | --- |
| 日付セルBinderの可読性 | 匿名クラスでやや長いが明確 | 公式サンプルに近く短い |
| セル再利用時の状態初期化 | `bind()`で明示的に全状態を戻せば安全 | 同じく`bind()`で明示初期化が必要 |
| 六曜文字の表示 | Javaの補助メソッドで十分可能 | Kotlinの式表現で少し簡潔 |
| ドット表示 | `Set<LocalDate>`または`Set<String>`で十分可能 | 同等 |
| 祝日判定 | Javaの判定クラスで十分可能 | 同等 |
| 選択状態更新 | `selectedDate`保持と`notifyDateChanged`で可能 | 同等 |
| 部分更新 | Javaから`notifyDateChanged(LocalDate)`を呼べる | 同等 |
| 月変更処理 | リスナーに`Unit.INSTANCE`が必要 | 自然 |
| バグ調査 | 既存Javaコードと同じ言語で追いやすい | 言語混在分だけ確認箇所が増える |
| 機種差対策 | View層の問題なので言語差は小さい | 同等 |
| 将来の機能追加 | Javaで十分可能 | サンプル移植はやや楽 |

六曜・祝日・ドット対応で重要なのは言語よりも、日付セルの状態初期化を毎回確実に行うこと。

Java案でも次の形なら保守しやすい。

- `selectedDate`を`LocalDate`で保持。
- `visitDates`を`Set<LocalDate>`で保持。
- 祝日判定を小さなJava helperに分離。
- 六曜判定を小さなJava helperに分離。
- `bind()`内で日付、六曜、ドット、文字色、背景、表示/非表示を毎回全て設定。
- 選択変更時は旧選択日と新選択日に`notifyDateChanged(...)`を呼ぶ。

## 比較評価

5が良い評価。

| 評価項目 | Javaのまま | 理由 | カレンダーだけKotlin | 理由 |
| --- | ---: | --- | ---: | --- |
| 初期実装量 | 4 | 既存Activityを維持できる | 3 | Activity変換またはKotlin補助クラス追加が必要 |
| Gradle変更の少なさ | 4 | Kizitonwoseとdesugaring追加のみ | 2 | Kotlin plugin設定も必要 |
| 既存コードへの影響の少なさ | 5 | 既存Java呼び出しを維持 | 3 | Activity変換時に差分が増える |
| 公式サンプルとの近さ | 3 | 構造は同じだがJava化が必要 | 5 | Kotlinサンプルを読み替えやすい |
| API利用の自然さ | 4 | 月リスナーだけ`Unit.INSTANCE`が必要 | 5 | Kotlin関数型を自然に扱える |
| コードの可読性 | 4 | 少し冗長だが既存言語に揃う | 4 | 短いが言語混在になる |
| 六曜等の追加しやすさ | 4 | helper分離で十分対応可能 | 4 | 同等か少し簡潔 |
| デバッグのしやすさ | 5 | 既存Java画面と同じ流れで追える | 3 | Java/Kotlin混在の確認が増える |
| 将来の保守性 | 4 | 現行チーム/コードベースに合う | 3 | Kotlinをカレンダーだけ維持する負担がある |
| 移行時の安全性 | 5 | 変更範囲が小さい | 3 | plugin追加とActivity変換でリスクが増える |
| Codexでの修正しやすさ | 5 | 既存パターンに沿って差分を抑えやすい | 4 | Kotlinも可能だが文脈切替がある |
| ユーザー側の管理しやすさ | 5 | Android Studio上の確認対象が増えにくい | 3 | Kotlin設定・混在を意識する必要がある |

## 推奨案

推奨:

- `CalendarActivityはJavaのまま実装する`

推奨理由:

- 必要なKizitonwose Calendar APIをJavaから呼び出せることを`javap`と`javac`で確認できた。
- 既存のアプリ本体はJava中心で、カレンダー画面もJavaのため、移行差分を小さくできる。
- Kotlin pluginを追加しないため、Gradle変更とビルド設定のリスクを抑えられる。
- 六曜・祝日・来店日ドットは、Javaでも`bind()`の状態初期化と小さなhelper分離で十分読みやすく実装できる。
- 公式サンプルとの差はあるが、View版API自体はXML + Binder構造なのでJava移植しやすい。

不採用案:

- `カレンダー画面だけKotlinにする`

不採用理由:

- 現状はKotlin Android pluginが明示導入されておらず、カレンダー移行に加えてビルド設定変更が増える。
- Activity全体の変換またはJava/Kotlin補助クラス分割が必要になり、今回の移行目的に対して差分が大きい。
- Javaでも最小APIを扱えるため、Kotlin化で得られる主な利点は公式サンプルに近いことと記述量の少なさに留まる。
- 既存の`PhotoRepository`、`VisitHistoryAdapter`、`MkarteBottomNav`、`EdgeToEdgeUtil`との連携はJavaのままの方が追いやすい。

## 移行Stepの予定変更ファイル

次Stepで変更予定:

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/example/mkarte1/ui/calendar/CalendarActivity.java`
- `app/src/main/res/layout/activity_calendar.xml`
- `docs/current_status.md`
- `docs/next_tasks.md`

次Stepで新規追加予定:

- `app/src/main/res/layout/item_calendar_day.xml`

必要に応じて追加候補:

- `app/src/main/java/com/example/mkarte1/ui/calendar/CalendarDayContainer.java`
- `app/src/main/java/com/example/mkarte1/util/CalendarDateUtil.java`

次Stepでは追加しない:

- 来店日ドット表示
- 六曜計算helper
- 新しい祝日判定helper
- Visit Entity / Visitテーブル
- DB Migration

## 既存Javaコードへの影響

Java案では、既存Javaコードへの影響はカレンダー画面周辺に限定できる。

- `PhotoRepository.listForDate(...)`は継続利用。
- `PhotoRepository.listTakenDates(...)`は来店日ドット再現時に利用可能。
- `VisitHistoryAdapter`は継続利用可能。
- `CustomerDetailActivity`へのIntent遷移は継続。
- `MkarteBottomNav`と`EdgeToEdgeUtil`は継続。
- DB、Entity、DAO、Repositoryの仕様変更は不要。

## 想定リスクと対処

| リスク | 対処 |
| --- | --- |
| `java.time`が`minSdk 24`で動かない | Core Library Desugaringを導入する |
| セル再利用で色・背景・ドットが残る | `bind()`で全View状態を毎回明示的に初期化する |
| 選択日変更時に旧選択日の表示が残る | 旧選択日と新選択日の両方に`notifyDateChanged(...)`を呼ぶ |
| 月変更時にタイトルや履歴がずれる | `monthScrollListener`で表示月を保持し、必要な更新を集中させる |
| 公式サンプルとの差で実装ミスが起きる | KizitonwoseのView版構造に沿って、Java版の最小Binderから小さく移植する |
| 祝日・六曜を同時追加して差分が大きくなる | 次Stepでは現行機能の再現のみ行い、六曜・新祝日は後続Stepへ分離する |

## 次Stepの実装範囲

次Step:

- Kizitonwose Calendar View版を導入し、現行カレンダー機能の再現だけを行う。

含める:

- Kizitonwose Calendar依存関係追加。
- Core Library Desugaring追加。
- Android標準`CalendarView`からKizitonwose `CalendarView`へ置き換え。
- 当月初期表示。
- 前月・翌月相当の月スクロール。
- 月表示タイトル。
- 曜日表示。
- 日付選択。
- 今日表示。
- 選択日表示。
- 選択日の撮影履歴取得。
- 顧客名と写真枚数の集約。
- 撮影履歴タップから顧客詳細へ遷移。
- Activity再表示時の再読み込み。
- 下部ナビとInsets維持。

含めない:

- 来店日ドット表示。
- 六曜表示。
- 新しい祝日処理。
- 祝日データ追加。
- DB構造変更。
- Entity追加。
- Migration追加。
- Photo保存仕様変更。
- CSV変更。

次Stepをさらに分割する必要:

- 推奨は2分割。
- Step20A: Kizitonwose導入、現行カレンダー機能の再現。
- Step20B: 実機確認後、来店日ドット、六曜、祝日表示の設計と実装を分けて検討。

## 検証で使用したコマンド

ベースラインビルド:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug --console=plain --no-daemon
```

結果:

- `BUILD SUCCESSFUL`

補足:

- 事前に`--no-daemon`なしで実行した通常ビルドは、既存Gradle daemonが存在しないVS Code拡張内JREの`jlink.exe`を参照して失敗した。
- Android Studio JBRを指定し、`--no-daemon`で再実行して成功した。

依存関係確認:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :app:buildEnvironment --console=plain --no-daemon
```

結果:

- app buildscript classpathは`No dependencies`。
- Daemon JVMはJetBrains JDK `21.0.10`。

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath --console=plain --no-daemon | Select-String -Pattern 'kotlin|desugar|calendar|materialcalendar|core-ktx'
```

結果:

- `androidx.core:core-ktx:1.18.0`あり。
- `org.jetbrains.kotlin:kotlin-stdlib:2.2.10`あり。
- Kizitonwose Calendarなし。
- Core Library Desugaringなし。

Java API検証:

```powershell
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.view.CalendarView
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.view.ViewContainer
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.view.MonthDayBinder
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.core.CalendarDay
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.core.CalendarMonth
javap -classpath <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -public com.kizitonwose.calendar.core.DayPosition
```

Java最小コンパイル:

```powershell
javac -cp <一時取得したKizitonwose/AndroidX/Android/Kotlin classpath> -d <temp classes> KizitonwoseJavaProbe.java
```

結果:

- `javac OK elevated`
- `KizitonwoseJavaProbe.class`
- `KizitonwoseJavaProbe$DayContainer.class`
- `KizitonwoseJavaProbe$1.class`

## 参考にした公式情報

- Kizitonwose Calendar GitHub: `https://github.com/kizitonwose/Calendar`
- Kizitonwose Calendar View docs: `https://github.com/kizitonwose/Calendar/blob/main/docs/View.md`
- Kizitonwose Calendar Releases: `https://github.com/kizitonwose/Calendar/releases`
- Maven Central `com.kizitonwose.calendar:view:2.10.1`: `https://central.sonatype.com/artifact/com.kizitonwose.calendar/view/2.10.1`
- Android Developers Kotlin overview: `https://developer.android.com/kotlin/overview`
- Android Developers Java 8+ API desugaring: `https://developer.android.com/studio/write/java8-support`
- Kotlin Java interoperability: `https://kotlinlang.org/docs/java-interop.html`
- Kotlin calling Kotlin from Java: `https://kotlinlang.org/docs/java-to-kotlin-interop.html`

## 未確認事項

- 次Stepで実際にKizitonwose Calendarを導入した後の実機表示確認。
- Android API 24実機またはエミュレータ上での`java.time` + desugaring実行確認。
- 日付セルXMLの最終デザイン。
- 六曜表示の計算方式。
- 祝日データの持ち方。
- 横スクロール/縦スクロール、月境界日の表示方針。
