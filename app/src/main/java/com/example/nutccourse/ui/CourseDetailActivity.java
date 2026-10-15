package com.example.nutccourse.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.Session;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/** 畫面 5：課程詳情。可加入／移除課表、收藏、開啟學校頁面。 */
public class CourseDetailActivity extends BaseActivity {
    public static final String EXTRA_ID = "course_id";

    private Course course;
    private MaterialButton btnSchedule, btnFavorite;
    private TextView tvConflictWarn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        course = repo.byId(getIntent().getStringExtra(EXTRA_ID));
        if (course == null) {
            toast("找不到這門課");
            finish();
            return;
        }
        setupToolbar(getString(R.string.title_detail), true);
        setToolbarSubtitle(course.id);

        ((TextView) findViewById(R.id.tvName)).setText(course.name);
        ((TextView) findViewById(R.id.tvSub)).setText(course.className + " · " + nz(course.teacher, "未定教師"));
        UiUtil.bindBadges(this, course, findViewById(R.id.tvType), findViewById(R.id.tvCredits),
                findViewById(R.id.tvProgram), findViewById(R.id.tvExtra));

        TextView tvRemark = findViewById(R.id.tvRemark);
        if (course.remark != null) {
            tvRemark.setVisibility(View.VISIBLE);
            tvRemark.setText("📌 " + course.remark);
        }

        bindSessions();
        bindInfo();
        ((TextView) findViewById(R.id.tvRaw)).setText(
                course.rawTime == null || course.rawTime.isEmpty() ? "（空白：機動排課）" : course.rawTime);

        tvConflictWarn = findViewById(R.id.tvConflictWarn);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnSchedule.setOnClickListener(v -> {
            if (store.inSchedule(course.id)) {
                store.removeFromSchedule(course.id);
                toast("已從課表移除");
                refreshButtons();
            } else {
                confirmAddToSchedule(course, this::refreshButtons);
            }
        });
        btnFavorite.setOnClickListener(v -> {
            boolean fav = store.toggleFavorite(course.id);
            toast(fav ? "已收藏" : "已取消收藏");
            refreshButtons();
        });
        findViewById(R.id.btnSameClass).setOnClickListener(v -> {
            Intent i = new Intent(this, ClassCoursesActivity.class);
            i.putExtra(ClassCoursesActivity.EXTRA_CLASS, course.className);
            startActivity(i);
        });
        findViewById(R.id.btnOpenWeb).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(course.sourceUrl())));
            } catch (ActivityNotFoundException e) {
                toast("找不到可以開啟網頁的 App");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (course != null) refreshButtons();
    }

    private void refreshButtons() {
        boolean inSchedule = store.inSchedule(course.id);
        btnSchedule.setText(inSchedule ? R.string.detail_remove : R.string.detail_add);
        btnFavorite.setText(store.isFavorite(course.id) ? "★ " + getString(R.string.detail_unfav)
                : "☆ " + getString(R.string.detail_fav));

        // 若還沒加入，先提示會不會衝堂
        List<ScheduleLogic.Conflict> cf = ScheduleLogic.conflictsWith(course, scheduleCourses());
        if (cf.isEmpty()) {
            tvConflictWarn.setVisibility(View.GONE);
        } else {
            StringBuilder sb = new StringBuilder(inSchedule ? "⚠ 這門課在課表中有衝堂：" : "⚠ 加入的話會跟課表衝堂：");
            for (ScheduleLogic.Conflict c : cf) {
                sb.append("\n• ").append(c.b.name).append("　").append(c.timeLabel());
            }
            tvConflictWarn.setText(sb);
            tvConflictWarn.setVisibility(View.VISIBLE);
        }
    }

    private void bindSessions() {
        LinearLayout ll = findViewById(R.id.llSessions);
        if (!course.hasSchedule()) {
            ll.addView(sessionView(course.parseError ? "時間格式特殊，請看下方原始時間字串" : "無固定上課時間（機動排課）"));
            return;
        }
        for (Session s : course.sessions) ll.addView(sessionView("🕘  " + s.longLabel()));
    }

    private TextView sessionView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextColor(ContextCompat.getColor(this, R.color.brand));
        int pad = dp(6);
        tv.setPadding(0, pad, 0, pad);
        return tv;
    }

    private void bindInfo() {
        LinearLayout ll = findViewById(R.id.llInfo);
        addRow(ll, "課程代碼", course.id);
        addRow(ll, "開課班級", course.className);
        addRow(ll, "系所", course.dept);
        addRow(ll, "授課教師", nz(course.teacher, "—"));
        addRow(ll, "修別", course.isRequired() ? "必修" : "選修");
        addRow(ll, "學分／時數", course.credits + " 學分 · 每週 " + course.hours + " 小時");
        addRow(ll, "學制／年級", course.programLabel() != null ? course.programLabel() : "跨系選修代號（不標示）");
        if (course.groupLabel() != null) addRow(ll, "組別", course.groupLabel());
        addRow(ll, "修課人數", course.students + " 人（人數限制 " + course.capacityMin + "～" + course.capacityMax + "）");
        if (course.category != null) addRow(ll, "通識領域", course.category);
        if (course.peActivity != null) addRow(ll, "體育項目", course.peActivity);
        addRow(ll, "學年課", course.yearLong ? "是（上下學期連續）" : "否");
        addRow(ll, "全英語", course.englishTaught ? "是" : "否");
        addRow(ll, "開放外系", course.openForOther ? "是" : "否");
        if (course.mergedNote != null) addRow(ll, "併班", course.mergedNote);
    }

    private void addRow(LinearLayout parent, String label, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_info_row, parent, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value);
        parent.addView(row);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String nz(String s, String fallback) {
        return s == null || s.isEmpty() ? fallback : s;
    }
}
