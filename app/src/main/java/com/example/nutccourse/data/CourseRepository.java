package com.example.nutccourse.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 課程資料來源。115-1 的資料寫死在 assets/courses_1151.json，
 * 啟動時讀一次放在記憶體，之後所有畫面共用。
 */
public class CourseRepository {
    public static final String ASSET_FILE = "courses_1151.json";
    public static final String SEMESTER_LABEL = "115-1";

    private static volatile CourseRepository instance;

    private final List<Course> courses;
    private final Map<String, Course> byId = new HashMap<>();
    private final String semester;
    private final String updatedAt;

    private CourseRepository(List<Course> courses, String semester, String updatedAt) {
        this.courses = Collections.unmodifiableList(courses);
        this.semester = semester;
        this.updatedAt = updatedAt;
        for (Course c : courses) byId.put(c.id, c);
    }

    /** 取得資料；第一次呼叫會同步讀檔（約 2MB），建議在 Splash 的背景執行緒先呼叫。 */
    public static CourseRepository get(Context context) {
        if (instance == null) {
            synchronized (CourseRepository.class) {
                if (instance == null) {
                    try {
                        String json = readAsset(context.getApplicationContext(), ASSET_FILE);
                        instance = fromJson(json);
                    } catch (IOException | JSONException e) {
                        throw new IllegalStateException("課程資料讀取失敗：" + e.getMessage(), e);
                    }
                }
            }
        }
        return instance;
    }

    public static boolean isLoaded() {
        return instance != null;
    }

    private static String readAsset(Context ctx, String name) throws IOException {
        try (InputStream in = ctx.getAssets().open(name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16 * 1024];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    // ---------- JSON 解析 ----------

    public static CourseRepository fromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray arr = root.getJSONArray("courses");
        List<Course> list = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            list.add(parseCourse(arr.getJSONObject(i)));
        }
        return new CourseRepository(list, str(root, "semester"), str(root, "updated_at"));
    }

    private static Course parseCourse(JSONObject o) throws JSONException {
        Course c = new Course();
        c.id = o.getString("course_id");
        c.ccid = o.optInt("ccid");
        c.name = str(o, "course_name");
        c.className = str(o, "class_name");
        c.dept = str(o, "dept");
        c.teacher = str(o, "teacher");
        c.type = str(o, "type");
        c.credits = o.optInt("credits");
        c.hours = o.optInt("hours");
        c.group = str(o, "group");
        c.category = str(o, "category");
        c.peActivity = str(o, "pe_activity");
        c.remark = str(o, "remark");
        c.mergedNote = str(o, "merged_note");
        c.yearLong = o.optBoolean("year_long");
        c.englishTaught = o.optBoolean("english_taught");
        c.openForOther = o.optBoolean("open_for_other_departments");
        c.students = o.optInt("students");
        c.capacityMin = o.optInt("capacity_min");
        c.capacityMax = o.optInt("capacity_max");
        c.rawTime = str(o, "raw_time");
        c.noSchedule = o.optBoolean("no_schedule");
        c.parseError = o.optBoolean("parse_error");

        if (!o.isNull("program")) {
            JSONObject p = o.getJSONObject("program");
            c.track = str(p, "track");
            c.grade = p.isNull("grade") ? 0 : p.optInt("grade");
        }

        List<Session> sessions = new ArrayList<>();
        if (!o.isNull("sessions")) {
            JSONArray ss = o.getJSONArray("sessions");
            for (int i = 0; i < ss.length(); i++) {
                JSONObject s = ss.getJSONObject(i);
                JSONArray ps = s.getJSONArray("periods");
                List<Integer> periods = new ArrayList<>();
                for (int j = 0; j < ps.length(); j++) periods.add(ps.getInt(j));
                sessions.add(new Session(s.getInt("day"), periods, str(s, "classroom")));
            }
        }
        c.sessions = Collections.unmodifiableList(sessions);
        return c;
    }

    /** org.json 的 optString 遇到 null 會回傳字串 "null"，這裡改成真正的 null。 */
    private static String str(JSONObject o, String key) {
        if (!o.has(key) || o.isNull(key)) return null;
        return o.optString(key, null);
    }

    // ---------- 查詢 ----------

    public List<Course> all() {
        return courses;
    }

    public Course byId(String id) {
        return byId.get(id);
    }

    public List<Course> byIds(Iterable<String> ids) {
        List<Course> out = new ArrayList<>();
        for (String id : ids) {
            Course c = byId.get(id);
            if (c != null) out.add(c);
        }
        return out;
    }

    public String semester() {
        return semester;
    }

    /** "2026-10-08T14:47:54+08:00" → "2026-10-08 14:47" */
    public String updatedAtLabel() {
        if (updatedAt == null || updatedAt.length() < 16) return "未知";
        return updatedAt.substring(0, 10) + " " + updatedAt.substring(11, 16);
    }

    /** 系所 → 課程數，依課程數由多到少。 */
    public LinkedHashMap<String, Integer> departmentCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Course c : courses) counts.merge(c.dept, 1, Integer::sum);
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue() != 0
                ? b.getValue() - a.getValue() : a.getKey().compareTo(b.getKey()));
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) out.put(e.getKey(), e.getValue());
        return out;
    }

    public List<String> departments() {
        return new ArrayList<>(departmentCounts().keySet());
    }

    /** 某系所的所有班級名稱，依學制、年級排序。 */
    public List<String> classesOf(String dept) {
        Map<String, Course> sample = new LinkedHashMap<>();
        for (Course c : courses) {
            if (dept.equals(c.dept) && !sample.containsKey(c.className)) sample.put(c.className, c);
        }
        List<String> names = new ArrayList<>(sample.keySet());
        names.sort((a, b) -> {
            Course ca = sample.get(a), cb = sample.get(b);
            int ta = trackOrder(ca.track), tb = trackOrder(cb.track);
            if (ta != tb) return ta - tb;
            if (ca.grade != cb.grade) return ca.grade - cb.grade;
            return a.compareTo(b);
        });
        return names;
    }

    public List<Course> coursesOfClass(String className) {
        List<Course> out = new ArrayList<>();
        for (Course c : courses) if (className.equals(c.className)) out.add(c);
        out.sort((a, b) -> a.sortKey() - b.sortKey());
        return out;
    }

    public List<String> tracks() {
        return new ArrayList<>(Arrays.asList("五專", "二技", "四技", "碩士班", "碩專班", "博士班"));
    }

    public List<String> categories() {
        TreeSet<String> set = new TreeSet<>();
        for (Course c : courses) if (c.category != null) set.add(c.category);
        return new ArrayList<>(set);
    }

    public static int trackOrder(String track) {
        if (track == null) return 99;
        switch (track) {
            case "五專": return 0;
            case "二技": return 1;
            case "四技": return 2;
            case "碩士班": return 3;
            case "碩專班": return 4;
            case "博士班": return 5;
            default: return 50;
        }
    }

    // 統計（關於頁使用）
    public int countParseErrors() {
        int n = 0;
        for (Course c : courses) if (c.parseError) n++;
        return n;
    }

    public int countNoSchedule() {
        int n = 0;
        for (Course c : courses) if (c.noSchedule) n++;
        return n;
    }

    public int countClasses() {
        TreeSet<String> set = new TreeSet<>();
        for (Course c : courses) set.add(c.className);
        return set.size();
    }
}
