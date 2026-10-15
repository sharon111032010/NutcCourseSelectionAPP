package com.example.nutccourse.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.Session;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 畫面 10：我的課表（週課表格子）。
 *
 * 重要：每個節次固定一列，不用 rowspan 合併。原網頁版測試發現合併儲存格時，
 * 衝堂的兩門課後畫的會蓋掉先畫的，使用者會以為課不見了。
 * 這裡衝堂時兩門課疊在同一格裡各自顯示，格子底色變紅。
 */
public class ScheduleActivity extends BaseActivity {
    private static final int LABEL_W_DP = 40;
    private static final int COL_W_DP = 84;
    private static final int ROW_H_DP = 56;

    private LinearLayout llGrid, llUnscheduled;
    private TextView tvSummary, tvEmpty, tvUnscheduledTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setupToolbar(getString(R.string.title_schedule), true);
        llGrid = findViewById(R.id.llGrid);
        llUnscheduled = findViewById(R.id.llUnscheduled);
        tvSummary = findViewById(R.id.tvSummary);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvUnscheduledTitle = findViewById(R.id.tvUnscheduledTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_conflicts) {
            startActivity(new Intent(this, ConflictActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_selected) {
            startActivity(new Intent(this, SelectedCoursesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void render() {
        List<Course> mine = scheduleCourses();
        ScheduleLogic.Stats st = ScheduleLogic.stats(mine);
        int conflicts = ScheduleLogic.allConflicts(mine).size();
        String summary = "共 " + st.count + " 門 · " + st.credits + " 學分（必修 " + st.requiredCredits
                + " / 選修 " + st.electiveCredits + "）";
        summary += conflicts > 0 ? "\n⚠ 有 " + conflicts + " 處衝堂，紅色格子是衝堂的節次" : "\n✓ 沒有衝堂";
        tvSummary.setText(summary);
        tvEmpty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);

        int lastDay = (store.showWeekend() || ScheduleLogic.hasWeekend(mine)) ? 7 : 5;
        int maxPeriod = ScheduleLogic.maxPeriod(mine);

        // grid[day][period] → 該節次的課程
        List<List<List<Course>>> grid = new ArrayList<>();
        for (int d = 0; d <= 7; d++) {
            List<List<Course>> col = new ArrayList<>();
            for (int p = 0; p <= maxPeriod; p++) col.add(new ArrayList<>());
            grid.add(col);
        }
        List<Course> unscheduled = new ArrayList<>();
        for (Course c : mine) {
            if (!c.hasSchedule()) {
                unscheduled.add(c);
                continue;
            }
            for (Session s : c.sessions) {
                for (int p : s.periods) {
                    if (s.day >= 1 && s.day <= 7 && p >= 1 && p <= maxPeriod) grid.get(s.day).get(p).add(c);
                }
            }
        }

        llGrid.removeAllViews();
        // 表頭
        LinearLayout header = row();
        header.addView(headerCell("節", LABEL_W_DP));
        for (int d = 1; d <= lastDay; d++) header.addView(headerCell(Session.dayName(d), COL_W_DP));
        llGrid.addView(header);
        // 每個節次一列
        for (int p = 1; p <= maxPeriod; p++) {
            LinearLayout r = row();
            r.addView(headerCell(String.valueOf(p), LABEL_W_DP));
            for (int d = 1; d <= lastDay; d++) r.addView(cell(grid.get(d).get(p), d, p));
            llGrid.addView(r);
        }

        // 沒有固定時段的課
        llUnscheduled.removeAllViews();
        tvUnscheduledTitle.setVisibility(unscheduled.isEmpty() ? View.GONE : View.VISIBLE);
        for (Course c : unscheduled) {
            TextView tv = new TextView(this);
            tv.setText("• " + c.name + "（" + c.className + "，" + c.credits + " 學分）— " + c.timeSummary());
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tv.setPadding(0, dp(6), 0, dp(6));
            tv.setOnClickListener(v -> showCourseDialog(c));
            llUnscheduled.addView(tv);
        }
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        return r;
    }

    private TextView headerCell(String text, int widthDp) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(widthDp), LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(1, 1, 1, 1);
        tv.setLayoutParams(lp);
        tv.setMinHeight(dp(32));
        tv.setGravity(Gravity.CENTER);
        tv.setText(text);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tv.setBackgroundColor(ContextCompat.getColor(this, R.color.grid_header));
        return tv;
    }

    private View cell(List<Course> courses, int day, int period) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(COL_W_DP), LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(1, 1, 1, 1);
        cell.setLayoutParams(lp);
        cell.setMinimumHeight(dp(ROW_H_DP));
        cell.setPadding(dp(2), dp(2), dp(2), dp(2));
        boolean conflict = courses.size() > 1;
        cell.setBackgroundColor(ContextCompat.getColor(this, conflict ? R.color.conflict_bg : R.color.grid_cell));
        for (Course c : courses) cell.addView(block(c, conflict, day, period));
        return cell;
    }

    private View block(Course c, boolean conflict, int day, int period) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(2);
        tv.setLayoutParams(lp);
        String room = classroomAt(c, day);
        tv.setText((conflict ? "⚠" : "") + c.name + (room != null ? "\n" + room : ""));
        tv.setMaxLines(3);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setTextSize(11);
        tv.setTextColor(0xFF1A1F26);
        tv.setPadding(dp(4), dp(3), dp(4), dp(3));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(6));
        bg.setColor(UiUtil.blockColor(c));
        if (conflict) bg.setStroke(dp(2), ContextCompat.getColor(this, R.color.conflict));
        tv.setBackground(bg);
        tv.setOnClickListener(v -> showCourseDialog(c));
        tv.setContentDescription(c.name + " 星期" + Session.dayName(day) + " 第" + period + "節");
        return tv;
    }

    private static String classroomAt(Course c, int day) {
        for (Session s : c.sessions) {
            if (s.day == day && s.classroom != null && !"0000".equals(s.classroom)) return s.classroom;
        }
        return null;
    }

    private void showCourseDialog(Course c) {
        String msg = c.className + " · " + (c.teacher == null ? "" : c.teacher) + "\n"
                + c.credits + " 學分 · " + (c.isRequired() ? "必修" : "選修") + "\n" + c.timeSummary();
        new MaterialAlertDialogBuilder(this)
                .setTitle(c.name)
                .setMessage(msg)
                .setPositiveButton("查看詳情", (d, w) -> openDetail(c))
                .setNegativeButton(R.string.detail_remove, (d, w) -> {
                    store.removeFromSchedule(c.id);
                    toast("已移除：" + c.name);
                    render();
                })
                .setNeutralButton("關閉", null)
                .show();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
