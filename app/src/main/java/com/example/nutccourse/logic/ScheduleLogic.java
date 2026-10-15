package com.example.nutccourse.logic;

import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.Session;

import java.util.ArrayList;
import java.util.List;

/** 衝堂判斷與學分統計（對應網頁版 schedule.js）。 */
public final class ScheduleLogic {
    private ScheduleLogic() {
    }

    /** 兩門課之間的一處衝突：哪一天、哪些節次重疊。 */
    public static class Conflict {
        public final Course a;
        public final Course b;
        public final int day;
        public final List<Integer> periods;

        Conflict(Course a, Course b, int day, List<Integer> periods) {
            this.a = a;
            this.b = b;
            this.day = day;
            this.periods = periods;
        }

        public String timeLabel() {
            return "星期" + Session.dayName(day) + " 第 " + Session.formatPeriods(periods) + " 節";
        }
    }

    public static class Stats {
        public int count;
        public int credits;
        public int hours;
        public int requiredCount;
        public int requiredCredits;
        public int electiveCount;
        public int electiveCredits;
        public int unscheduledCount;
    }

    /** 同一天且節次有交集才算衝堂。 */
    public static List<Integer> overlap(Session a, Session b) {
        List<Integer> out = new ArrayList<>();
        if (a.day != b.day) return out;
        for (int p : a.periods) {
            if (b.periods.contains(p)) out.add(p);
        }
        return out;
    }

    public static boolean sessionsOverlap(Session a, Session b) {
        return !overlap(a, b).isEmpty();
    }

    /** 新課程 c 跟課表裡其他課的所有衝突（兩兩比對每個 session）。 */
    public static List<Conflict> conflictsWith(Course c, List<Course> schedule) {
        List<Conflict> out = new ArrayList<>();
        if (!c.hasSchedule()) return out;
        for (Course other : schedule) {
            if (other.id.equals(c.id) || !other.hasSchedule()) continue;
            for (Session s1 : c.sessions) {
                for (Session s2 : other.sessions) {
                    List<Integer> ov = overlap(s1, s2);
                    if (!ov.isEmpty()) out.add(new Conflict(c, other, s1.day, ov));
                }
            }
        }
        return out;
    }

    public static boolean conflictsWithAny(Course c, List<Course> schedule) {
        return !conflictsWith(c, schedule).isEmpty();
    }

    /** 課表內所有衝突（每對課程只比一次）。 */
    public static List<Conflict> allConflicts(List<Course> schedule) {
        List<Conflict> out = new ArrayList<>();
        for (int i = 0; i < schedule.size(); i++) {
            Course a = schedule.get(i);
            if (!a.hasSchedule()) continue;
            for (int j = i + 1; j < schedule.size(); j++) {
                Course b = schedule.get(j);
                if (!b.hasSchedule()) continue;
                for (Session s1 : a.sessions) {
                    for (Session s2 : b.sessions) {
                        List<Integer> ov = overlap(s1, s2);
                        if (!ov.isEmpty()) out.add(new Conflict(a, b, s1.day, ov));
                    }
                }
            }
        }
        return out;
    }

    public static Stats stats(List<Course> schedule) {
        Stats s = new Stats();
        for (Course c : schedule) {
            s.count++;
            s.credits += c.credits;
            s.hours += c.hours;
            if (c.isRequired()) {
                s.requiredCount++;
                s.requiredCredits += c.credits;
            } else {
                s.electiveCount++;
                s.electiveCredits += c.credits;
            }
            if (!c.hasSchedule()) s.unscheduledCount++;
        }
        return s;
    }

    /** 課表上最大節次（至少 10）。 */
    public static int maxPeriod(List<Course> schedule) {
        int max = 10;
        for (Course c : schedule) {
            if (!c.hasSchedule()) continue;
            for (Session s : c.sessions) {
                for (int p : s.periods) max = Math.max(max, p);
            }
        }
        return max;
    }

    public static boolean hasWeekend(List<Course> schedule) {
        for (Course c : schedule) {
            if (!c.hasSchedule()) continue;
            for (Session s : c.sessions) {
                if (s.day >= 6) return true;
            }
        }
        return false;
    }
}
