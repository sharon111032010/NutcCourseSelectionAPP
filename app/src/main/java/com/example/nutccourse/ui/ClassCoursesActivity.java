package com.example.nutccourse.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/** 畫面 8：班級課程。列出一個班級的全部課，可一鍵把必修加入課表。 */
public class ClassCoursesActivity extends BaseActivity {
    public static final String EXTRA_CLASS = "class_name";

    private List<Course> courses;
    private CourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        String className = getIntent().getStringExtra(EXTRA_CLASS);
        if (className == null) {
            finish();
            return;
        }
        courses = repo.coursesOfClass(className);
        setupToolbar(className, true);
        setToolbarSubtitle(getString(R.string.title_class_courses));

        ScheduleLogic.Stats st = ScheduleLogic.stats(courses);
        String prog = courses.isEmpty() ? null : courses.get(0).programLabel();
        ((TextView) findViewById(R.id.tvHeader)).setText(
                (prog != null ? prog + "　" : "") + "共 " + st.count + " 門 · 必修 " + st.requiredCount
                        + " 門（" + st.requiredCredits + " 學分）· 選修 " + st.electiveCount + " 門（"
                        + st.electiveCredits + " 學分）");

        MaterialButton btn = findViewById(R.id.btnHeaderAction);
        if (st.requiredCount > 0) {
            btn.setVisibility(android.view.View.VISIBLE);
            btn.setText("把這班必修全部加入課表");
            btn.setOnClickListener(v -> addAllRequired());
        }

        adapter = new CourseAdapter(this, store, this::openDetail);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        adapter.setItems(courses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.refreshMarks();
    }

    private void addAllRequired() {
        List<Course> toAdd = new ArrayList<>();
        for (Course c : courses) if (c.isRequired() && !store.inSchedule(c.id)) toAdd.add(c);
        if (toAdd.isEmpty()) {
            toast("這班必修都已經在課表裡了");
            return;
        }
        // 預先算加入後的衝堂數，讓使用者決定
        List<Course> after = new ArrayList<>(scheduleCourses());
        int before = ScheduleLogic.allConflicts(after).size();
        after.addAll(toAdd);
        int newConflicts = ScheduleLogic.allConflicts(after).size() - before;
        int credits = 0;
        for (Course c : toAdd) credits += c.credits;

        String msg = "將加入 " + toAdd.size() + " 門必修，共 " + credits + " 學分。";
        if (newConflicts > 0) msg += "\n\n⚠ 加入後會多出 " + newConflicts + " 處衝堂，可到「衝堂檢查」查看。";
        new MaterialAlertDialogBuilder(this)
                .setTitle("加入必修")
                .setMessage(msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("加入", (d, w) -> {
                    for (Course c : toAdd) store.addToSchedule(c.id);
                    adapter.refreshMarks();
                    toast("已加入 " + toAdd.size() + " 門課");
                })
                .show();
    }
}
