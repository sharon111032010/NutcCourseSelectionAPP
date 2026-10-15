package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 畫面 6：依系所瀏覽（系所 → 班級 → 課程 的第一層）。 */
public class DepartmentListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setupToolbar(getString(R.string.title_departments), true);

        Map<String, Integer> counts = repo.departmentCounts();
        ((TextView) findViewById(R.id.tvHeader)).setText(
                "共 " + counts.size() + " 個系所／開課單位，依課程數排序。系所是由開課班級名稱推算。");

        List<SimpleRowAdapter.Row> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            int classes = repo.classesOf(e.getKey()).size();
            rows.add(new SimpleRowAdapter.Row(e.getKey(), e.getKey(), classes + " 個班級", e.getValue() + " 門"));
        }
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleRowAdapter(rows, row -> {
            Intent i = new Intent(this, ClassListActivity.class);
            i.putExtra(ClassListActivity.EXTRA_DEPT, row.key);
            startActivity(i);
        }));
    }
}
