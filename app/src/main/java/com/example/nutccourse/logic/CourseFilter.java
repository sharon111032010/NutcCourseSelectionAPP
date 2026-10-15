package com.example.nutccourse.logic;

import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 篩選邏輯（對應網頁版 filter.js）。純函式，不碰畫面。
 *
 * 節次判斷是整個專案最核心的需求：
 *  - 完整符合：課程佔用的節次必須全部落在使用者勾選的節次內（subset）
 *  - 有碰到即可：任一節在勾選範圍內就算
 */
public final class CourseFilter {
    /** 一次最多顯示的筆數，避免 RecyclerView 塞進三千多筆。 */
    public static final int RESULT_LIMIT = 300;

    private CourseFilter() {
    }

    public static List<Course> apply(List<Course> courses, FilterCriteria f, List<Course> schedule) {
        String[] tokens = f.hasKeyword()
                ? f.keyword.trim().toLowerCase(Locale.ROOT).split("\\s+")
                : new String[0];
        List<Course> out = new ArrayList<>();
        for (Course c : courses) {
            if (matches(c, f, tokens, schedule)) out.add(c);
        }
        return out;
    }

    static boolean matches(Course c, FilterCriteria f, String[] tokens, List<Course> schedule) {
        if (tokens.length > 0 && !matchesKeyword(c, tokens)) return false;
        if (!f.type.isEmpty() && !f.type.equals(c.type)) return false;
        if (!f.dept.isEmpty() && !f.dept.equals(c.dept)) return false;
        if (!f.track.isEmpty() && !f.track.equals(c.track)) return false;
        if (f.grade > 0 && f.grade != c.grade) return false;
        if (!f.category.isEmpty() && !f.category.equals(c.category)) return false;
        if (f.openForOtherOnly && !c.openForOther) return false;
        if (f.englishOnly && !c.englishTaught) return false;
        if (f.hideUnscheduled && !c.hasSchedule()) return false;
        if (f.hasTimeCondition() && !matchesTime(c, f.days, f.periods, f.matchAny)) return false;
        if (f.hideConflict && schedule != null && !schedule.isEmpty()
                && ScheduleLogic.conflictsWithAny(c, schedule)) return false;
        return true;
    }

    static boolean matchesKeyword(Course c, String[] tokens) {
        StringBuilder hay = new StringBuilder();
        append(hay, c.name);
        append(hay, c.teacher);
        append(hay, c.className);
        append(hay, c.id);
        append(hay, c.dept);
        append(hay, c.category);
        append(hay, c.peActivity);
        if (c.sessions != null) {
            for (Session s : c.sessions) append(hay, s.classroom);
        }
        String h = hay.toString().toLowerCase(Locale.ROOT);
        for (String t : tokens) {
            if (!h.contains(t)) return false;
        }
        return true;
    }

    private static void append(StringBuilder sb, String s) {
        if (s != null) sb.append(s).append('\n');
    }

    /**
     * 時間條件。days 或 periods 其中一個是空的代表該維度不限。
     * 沒有固定時段的課在設定時間條件時一律不符合。
     */
    public static boolean matchesTime(Course c, Set<Integer> days, Set<Integer> periods, boolean matchAny) {
        if (!c.hasSchedule()) return false;
        if (matchAny) {
            for (Session s : c.sessions) {
                if (!days.isEmpty() && !days.contains(s.day)) continue;
                if (periods.isEmpty()) return true;
                for (int p : s.periods) {
                    if (periods.contains(p)) return true;
                }
            }
            return false;
        } else {
            for (Session s : c.sessions) {
                if (!days.isEmpty() && !days.contains(s.day)) return false;
                if (!periods.isEmpty() && !periods.containsAll(s.periods)) return false;
            }
            return true;
        }
    }
}
