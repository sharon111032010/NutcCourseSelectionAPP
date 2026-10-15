package com.example.nutccourse.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nutccourse.logic.FilterCriteria;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 使用者資料（對應網頁版 storage.js 的 localStorage）：
 * 課表、收藏、篩選條件、顯示設定。全部存在 SharedPreferences。
 * 讀到壞掉的 JSON 一律當成空值，不讓 App 因此閃退。
 */
public class UserStore {
    private static final String PREFS = "nutc_course_store";
    private static final String KEY_SCHEDULE = "schedule_ids";
    private static final String KEY_FAVORITES = "favorite_ids";
    private static final String KEY_FILTER = "filter_json";
    private static final String KEY_SHOW_WEEKEND = "show_weekend";
    private static final String KEY_DEFAULT_ANY = "default_match_any";

    private final SharedPreferences prefs;

    public UserStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------- 課表 ----------

    public LinkedHashSet<String> scheduleIds() {
        return readIds(KEY_SCHEDULE);
    }

    public boolean inSchedule(String id) {
        return scheduleIds().contains(id);
    }

    public void addToSchedule(String id) {
        LinkedHashSet<String> ids = scheduleIds();
        ids.add(id);
        writeIds(KEY_SCHEDULE, ids);
    }

    public void removeFromSchedule(String id) {
        LinkedHashSet<String> ids = scheduleIds();
        ids.remove(id);
        writeIds(KEY_SCHEDULE, ids);
    }

    public void clearSchedule() {
        prefs.edit().remove(KEY_SCHEDULE).apply();
    }

    // ---------- 收藏 ----------

    public LinkedHashSet<String> favoriteIds() {
        return readIds(KEY_FAVORITES);
    }

    public boolean isFavorite(String id) {
        return favoriteIds().contains(id);
    }

    /** 切換收藏狀態，回傳切換後是否為收藏。 */
    public boolean toggleFavorite(String id) {
        LinkedHashSet<String> ids = favoriteIds();
        boolean nowFav;
        if (ids.contains(id)) {
            ids.remove(id);
            nowFav = false;
        } else {
            ids.add(id);
            nowFav = true;
        }
        writeIds(KEY_FAVORITES, ids);
        return nowFav;
    }

    public void clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply();
    }

    // ---------- 篩選條件 ----------

    public FilterCriteria loadFilter() {
        String json = prefs.getString(KEY_FILTER, null);
        FilterCriteria f = FilterCriteria.fromJson(json);
        if (json == null) f.matchAny = defaultMatchAny();
        return f;
    }

    public void saveFilter(FilterCriteria f) {
        prefs.edit().putString(KEY_FILTER, f.toJson()).apply();
    }

    public void resetFilter() {
        prefs.edit().remove(KEY_FILTER).apply();
    }

    // ---------- 顯示設定 ----------

    public boolean showWeekend() {
        return prefs.getBoolean(KEY_SHOW_WEEKEND, false);
    }

    public void setShowWeekend(boolean v) {
        prefs.edit().putBoolean(KEY_SHOW_WEEKEND, v).apply();
    }

    public boolean defaultMatchAny() {
        return prefs.getBoolean(KEY_DEFAULT_ANY, false);
    }

    public void setDefaultMatchAny(boolean v) {
        prefs.edit().putBoolean(KEY_DEFAULT_ANY, v).apply();
    }

    // ---------- 內部 ----------

    private LinkedHashSet<String> readIds(String key) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        String raw = prefs.getString(key, null);
        if (raw == null) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
        } catch (JSONException | RuntimeException e) {
            // 壞掉的資料直接當空的
        }
        return out;
    }

    private void writeIds(String key, LinkedHashSet<String> ids) {
        JSONArray arr = new JSONArray();
        for (String id : ids) arr.put(id);
        prefs.edit().putString(key, arr.toString()).apply();
    }

    public static List<String> toList(LinkedHashSet<String> set) {
        return new ArrayList<>(set);
    }
}
