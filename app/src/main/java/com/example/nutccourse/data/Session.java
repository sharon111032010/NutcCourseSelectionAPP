package com.example.nutccourse.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 一個上課時段：星期幾、哪些節次、哪間教室。 */
public class Session {
    public static final String[] DAY_NAMES = {"", "一", "二", "三", "四", "五", "六", "日"};

    public final int day;              // 1 = 星期一 … 7 = 星期日
    public final List<Integer> periods;
    public final String classroom;     // 可能為 null

    public Session(int day, List<Integer> periods, String classroom) {
        this.day = day;
        List<Integer> sorted = new ArrayList<>(periods);
        Collections.sort(sorted);
        this.periods = Collections.unmodifiableList(sorted);
        this.classroom = classroom;
    }

    public static String dayName(int day) {
        return (day >= 1 && day <= 7) ? DAY_NAMES[day] : "?";
    }

    /** [2,3,4,6] → "2-4、6" */
    public static String formatPeriods(List<Integer> ps) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < ps.size()) {
            int start = ps.get(i);
            int end = start;
            while (i + 1 < ps.size() && ps.get(i + 1) == end + 1) {
                end = ps.get(++i);
            }
            if (sb.length() > 0) sb.append("、");
            sb.append(start == end ? String.valueOf(start) : start + "-" + end);
            i++;
        }
        return sb.toString();
    }

    /** "二 5-7" */
    public String shortLabel() {
        return dayName(day) + " " + formatPeriods(periods);
    }

    /** "星期二 第 5-7 節（2703）" */
    public String longLabel() {
        String s = "星期" + dayName(day) + " 第 " + formatPeriods(periods) + " 節";
        if (classroom != null && !classroom.isEmpty() && !"0000".equals(classroom)) {
            s += "（" + classroom + "）";
        }
        return s;
    }
}
