package com.example.nutccourse.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/** 畫面 11：衝堂檢查。列出課表中每一處衝突與重疊節次。 */
public class ConflictActivity extends BaseActivity {
    private ConflictAdapter adapter;
    private TextView tvHeader, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setupToolbar(getString(R.string.title_conflicts), true);
        tvHeader = findViewById(R.id.tvHeader);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ConflictAdapter(new ConflictAdapter.Callbacks() {
            @Override
            public void onOpen(Course c) {
                openDetail(c);
            }

            @Override
            public void onRemove(Course c) {
                new MaterialAlertDialogBuilder(ConflictActivity.this)
                        .setTitle("移除課程")
                        .setMessage("要把「" + c.name + "」從課表移除嗎？")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.remove, (d, w) -> {
                            store.removeFromSchedule(c.id);
                            render();
                        })
                        .show();
            }
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
        List<ScheduleLogic.Conflict> list = ScheduleLogic.allConflicts(mine);
        tvHeader.setText("檢查了課表中的 " + mine.size() + " 門課，找到 " + list.size()
                + " 處衝堂。點課名看詳情，或直接移除其中一門。");
        adapter.setItems(list);
        tvEmpty.setText(mine.isEmpty() ? getString(R.string.schedule_empty) : getString(R.string.conflict_none));
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
