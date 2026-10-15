package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;

import java.util.ArrayList;
import java.util.List;

/** 畫面 7：選擇班級。列出某系所的所有班級。 */
public class ClassListActivity extends BaseActivity {
    public static final String EXTRA_DEPT = "dept";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        String dept = getIntent().getStringExtra(EXTRA_DEPT);
        if (dept == null) {
            finish();
            return;
        }
        setupToolbar(dept, true);
        setToolbarSubtitle(getString(R.string.title_classes));

        List<String> classes = repo.classesOf(dept);
        ((TextView) findViewById(R.id.tvHeader)).setText("共 " + classes.size() + " 個班級，依學制、年級排序。");

        List<SimpleRowAdapter.Row> rows = new ArrayList<>();
        for (String name : classes) {
            List<Course> cs = repo.coursesOfClass(name);
            Course sample = cs.get(0);
            int credits = 0;
            for (Course c : cs) if (c.isRequired()) credits += c.credits;
            String sub = sample.programLabel() != null ? sample.programLabel() : "跨系選修代號";
            sub += " · 必修 " + credits + " 學分";
            rows.add(new SimpleRowAdapter.Row(name, name, sub, cs.size() + " 門"));
        }
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleRowAdapter(rows, row -> {
            Intent i = new Intent(this, ClassCoursesActivity.class);
            i.putExtra(ClassCoursesActivity.EXTRA_CLASS, row.key);
            startActivity(i);
        }));
    }
}
