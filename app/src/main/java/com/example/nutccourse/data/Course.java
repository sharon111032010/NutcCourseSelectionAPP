package com.example.nutccourse.data;

import java.util.List;

/** 一門課。欄位對應 courses_1151.json（由原專案爬蟲輸出整理而來）。 */
public class Course {
    public String id;              // course_id，例如 D16865
    public int ccid;
    public String name;
    public String className;       // 開課班級，例如 老服一１
    public String dept;            // 由班級名稱推算的系所簡稱，例如 老服
    public String teacher;
    public String type;            // 必 / 選
    public int credits;
    public int hours;
    public String group;           // "---" 代表不分組
    public String category;        // 通識領域，可能為 null
    public String peActivity;      // 體育項目，可能為 null
    public String remark;
    public String mergedNote;
    public boolean yearLong;
    public boolean englishTaught;
    public boolean openForOther;
    public int students;
    public int capacityMin;
    public int capacityMax;
    public String track;           // 五專 / 二技 / 四技 / 碩士班 / 碩專班 / 博士班，可能為 null
    public int grade;              // 0 代表未知
    public String rawTime;
    public List<Session> sessions;
    public boolean noSchedule;
    public boolean parseError;

    public boolean isRequired() {
        return "必".equals(type);
    }

    public boolean hasSchedule() {
        return sessions != null && !sessions.isEmpty();
    }

    /** "二 5-7 · 四 5-6"，沒有時段時回傳說明文字。 */
    public String timeSummary() {
        if (!hasSchedule()) {
            if (parseError) return "時間格式特殊：" + rawTime;
            return "無固定上課時間";
        }
        StringBuilder sb = new StringBuilder();
        for (Session s : sessions) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(s.shortLabel());
        }
        return sb.toString();
    }

    /** "四技 1 年級"；通識／體育／語言等虛擬班級回傳 null。 */
    public String programLabel() {
        if (track == null) return null;
        return grade > 0 ? track + " " + grade + " 年級" : track;
    }

    public String groupLabel() {
        return (group == null || "---".equals(group)) ? null : "第 " + group + " 組";
    }

    public String sourceUrl() {
        return "https://aisap.nutc.edu.tw/public/day/course_details.aspx?sem=1151&ccid=" + ccid;
    }

    /** 用於排序：第一個時段的星期、節次。 */
    public int sortKey() {
        if (!hasSchedule()) return 9999;
        Session s = sessions.get(0);
        return s.day * 100 + (s.periods.isEmpty() ? 0 : s.periods.get(0));
    }
}
