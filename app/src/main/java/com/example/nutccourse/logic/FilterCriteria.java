package com.example.nutccourse.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** 搜尋／篩選條件。可序列化成 JSON 存進 SharedPreferences。 */
public class FilterCriteria {
    public String keyword = "";
    public TreeSet<Integer> days = new TreeSet<>();
    public TreeSet<Integer> periods = new TreeSet<>();
    /** false = 完整符合（預設）；true = 有碰到即可 */
    public boolean matchAny = false;
    public String type = "";        // "" / 必 / 選
    public String dept = "";
    public String track = "";
    public int grade = 0;           // 0 = 不限
    public String category = "";
    public boolean openForOtherOnly = false;
    public boolean englishOnly = false;
    public boolean hideUnscheduled = false;
    public boolean hideConflict = false;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /** 除了關鍵字以外，有沒有設定任何條件。 */
    public boolean hasConditions() {
        return !days.isEmpty() || !periods.isEmpty() || !type.isEmpty() || !dept.isEmpty()
                || !track.isEmpty() || grade > 0 || !category.isEmpty()
                || openForOtherOnly || englishOnly || hideUnscheduled || hideConflict;
    }

    public boolean isEmpty() {
        return !hasKeyword() && !hasConditions();
    }

    public boolean hasTimeCondition() {
        return !days.isEmpty() || !periods.isEmpty();
    }

    /** 給畫面上顯示的條件摘要。 */
    public String describe() {
        List<String> parts = new ArrayList<>();
        if (!days.isEmpty()) {
            StringBuilder sb = new StringBuilder("星期");
            boolean first = true;
            for (int d : days) {
                if (!first) sb.append("、");
                sb.append(com.example.nutccourse.data.Session.dayName(d));
                first = false;
            }
            parts.add(sb.toString());
        }
        if (!periods.isEmpty()) {
            parts.add("第 " + com.example.nutccourse.data.Session.formatPeriods(new ArrayList<>(periods))
                    + " 節（" + (matchAny ? "有碰到" : "完整符合") + "）");
        }
        if (!type.isEmpty()) parts.add(type + "修");
        if (!dept.isEmpty()) parts.add(dept);
        if (!track.isEmpty()) parts.add(track);
        if (grade > 0) parts.add(grade + " 年級");
        if (!category.isEmpty()) parts.add(category);
        if (openForOtherOnly) parts.add("開放外系");
        if (englishOnly) parts.add("全英語");
        if (hideUnscheduled) parts.add("隱藏無時段");
        if (hideConflict) parts.add("隱藏衝堂");
        return String.join(" · ", parts);
    }

    public FilterCriteria copy() {
        return fromJson(toJson());
    }

    // ---------- JSON ----------

    public String toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("keyword", keyword == null ? "" : keyword);
            o.put("days", toArray(days));
            o.put("periods", toArray(periods));
            o.put("matchAny", matchAny);
            o.put("type", type);
            o.put("dept", dept);
            o.put("track", track);
            o.put("grade", grade);
            o.put("category", category);
            o.put("openForOtherOnly", openForOtherOnly);
            o.put("englishOnly", englishOnly);
            o.put("hideUnscheduled", hideUnscheduled);
            o.put("hideConflict", hideConflict);
            return o.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    /** 解析失敗或 null 時回傳空條件。 */
    public static FilterCriteria fromJson(String json) {
        FilterCriteria f = new FilterCriteria();
        if (json == null) return f;
        try {
            JSONObject o = new JSONObject(json);
            f.keyword = o.optString("keyword", "");
            readInts(o.optJSONArray("days"), f.days);
            readInts(o.optJSONArray("periods"), f.periods);
            f.matchAny = o.optBoolean("matchAny", false);
            f.type = o.optString("type", "");
            f.dept = o.optString("dept", "");
            f.track = o.optString("track", "");
            f.grade = o.optInt("grade", 0);
            f.category = o.optString("category", "");
            f.openForOtherOnly = o.optBoolean("openForOtherOnly", false);
            f.englishOnly = o.optBoolean("englishOnly", false);
            f.hideUnscheduled = o.optBoolean("hideUnscheduled", false);
            f.hideConflict = o.optBoolean("hideConflict", false);
        } catch (JSONException | RuntimeException e) {
            return new FilterCriteria();
        }
        return f;
    }

    private static JSONArray toArray(TreeSet<Integer> set) {
        JSONArray a = new JSONArray();
        for (int v : set) a.put(v);
        return a;
    }

    private static void readInts(JSONArray a, TreeSet<Integer> into) throws JSONException {
        if (a == null) return;
        for (int i = 0; i < a.length(); i++) into.add(a.getInt(i));
    }
}
