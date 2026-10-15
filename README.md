# NUTC 智慧選課助手（Android 版）

原本 Flask + 網頁版專案的 Android 移植版。Java + XML Layout，課程資料寫死 115-1 學期，
打包在 `app/src/main/assets/courses_1151.json`，**不需要後端、不需要網路**。

## 開啟方式

1. Android Studio → **File › Open** → 選這個資料夾（`NUTCCourseApp`）
2. 等 Gradle Sync 完成（第一次會下載 Gradle 8.7、AGP 8.5.2、AndroidX 套件）
3. 選模擬器或手機 → ▶ Run

需求：Android Studio Koala（2024.1）以上、JDK 17+（Android Studio 內建即可）。
App 最低支援 Android 8.0（API 26）。

## 畫面（15 個 Activity）

| # | 畫面 | 檔案 | 說明 |
|---|------|------|------|
| 1 | 啟動畫面 | `SplashActivity` | 背景執行緒讀取課程 JSON |
| 2 | 首頁 | `MainActivity` | 統計列（課程數／已選／學分／衝堂）＋ 10 個功能入口 |
| 3 | 課程搜尋 | `SearchActivity` | 關鍵字（課名、教師、班級、代碼、教室），結果上限 300 筆 |
| 4 | 進階篩選 | `FilterActivity` | 星期、節次（**完整符合／有碰到即可**）、修別、系所、學制、年級、通識領域、開關 |
| 5 | 課程詳情 | `CourseDetailActivity` | 時段、完整資訊、原始時間字串、加入課表（衝堂確認）、收藏 |
| 6 | 依系所瀏覽 | `DepartmentListActivity` | 40 個系所／開課單位 |
| 7 | 選擇班級 | `ClassListActivity` | 依學制、年級排序 |
| 8 | 班級課程 | `ClassCoursesActivity` | 一鍵把整班必修加入課表 |
| 9 | 通識／體育／語言 | `GeneralEdActivity` | 依領域、體育項目、全英語、開放外系切換 |
| 10 | 我的課表 | `ScheduleActivity` | 週課表格子，**每節固定一列，衝堂課疊在同格**（不用 rowspan） |
| 11 | 衝堂檢查 | `ConflictActivity` | 每處衝突的重疊節次，可直接移除 |
| 12 | 已選課程與學分 | `SelectedCoursesActivity` | 總學分、必修／選修、時數，移除可復原 |
| 13 | 我的收藏 | `FavoritesActivity` | 找出可直接加入、不衝堂的收藏 |
| 14 | 設定 | `SettingsActivity` | 週末欄位、預設節次模式、清除資料 |
| 15 | 關於 | `AboutActivity` | 資料統計、已知限制 |

## 程式結構（對應原網頁版）

```
app/src/main/java/com/example/nutccourse/
├── data/
│   ├── Course.java, Session.java    課程模型
│   ├── CourseRepository.java        讀 assets JSON、查詢（≈ course_service.py）
│   └── UserStore.java               SharedPreferences（≈ storage.js）
├── logic/
│   ├── FilterCriteria.java          篩選條件 + JSON 序列化
│   ├── CourseFilter.java            篩選純函式（≈ filter.js）
│   └── ScheduleLogic.java           衝堂、學分統計（≈ schedule.js）
└── ui/                              15 個 Activity + 3 個 Adapter
```

## 更新課程資料

用原專案匯出的 JSON 重新產生 asset（會順便從班級名稱推算 `dept` 系所欄位、去掉用不到的欄位）：

```
python3 tools/prep_courses.py courses_export.json app/src/main/assets/courses_1151.json
```
