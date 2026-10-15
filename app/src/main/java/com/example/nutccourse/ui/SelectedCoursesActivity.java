package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/** 畫面 12：已選課程清單與學分統計。 */
public class SelectedCoursesActivity extends BaseActivity {
    private CourseAdapter adapter;
    private TextView tvTotal, tvBreakdown, tvEmpty;
    private MaterialButton btnConflicts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected);
        setupToolbar(getString(R.string.title_selected), true);
        tvTotal = findViewById(R.id.tvTotalCredits);
        tvBreakdown = findViewById(R.id.tvBreakdown);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnConflicts = findViewById(R.id.btnConflicts);
        btnConflicts.setOnClickListener(v -> startActivity(new Intent(this, ConflictActivity.class)));

        adapter = new CourseAdapter(this, store, this::openDetail);
        adapter.setAction(getString(R.string.remove), c -> {
            store.removeFromSchedule(c.id);
            render();
            Snackbar.make(findViewById(R.id.rv), "已移除：" + c.name, Snackbar.LENGTH_LONG)
                    .setAction("復原", v -> {
                        store.addToSchedule(c.id);
                        render();
                    })
                    .show();
        });
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        List<Course> mine = scheduleCourses();
        mine.sort((a, b) -> a.sortKey() - b.sortKey());
        ScheduleLogic.Stats st = ScheduleLogic.stats(mine);
        tvTotal.setText(String.valueOf(st.credits));
        String text = "共 " + st.count + " 門課\n"
                + "必修 " + st.requiredCount + " 門 · " + st.requiredCredits + " 學分\n"
                + "選修 " + st.electiveCount + " 門 · " + st.electiveCredits + " 學分\n"
                + "每週上課 " + st.hours + " 小時";
        if (st.unscheduledCount > 0) text += "\n其中 " + st.unscheduledCount + " 門無固定時段";
        tvBreakdown.setText(text);

        int conflicts = ScheduleLogic.allConflicts(mine).size();
        if (conflicts > 0) {
            btnConflicts.setVisibility(View.VISIBLE);
            btnConflicts.setText("⚠ 有 " + conflicts + " 處衝堂，點此查看");
        } else {
            btnConflicts.setVisibility(View.GONE);
        }
        adapter.setItems(mine);
        tvEmpty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
