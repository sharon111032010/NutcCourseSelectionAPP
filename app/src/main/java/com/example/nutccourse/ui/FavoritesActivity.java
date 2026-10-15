package com.example.nutccourse.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** 畫面 13：我的收藏。 */
public class FavoritesActivity extends BaseActivity {
    private CourseAdapter adapter;
    private TextView tvHeader, tvEmpty;
    private MaterialButton btnAddAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setupToolbar(getString(R.string.title_favorites), true);
        tvHeader = findViewById(R.id.tvHeader);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setText(R.string.fav_empty);
        btnAddAll = findViewById(R.id.btnHeaderAction);
        btnAddAll.setText("檢視哪些收藏可以直接加入課表");
        btnAddAll.setOnClickListener(v -> showAddable());

        adapter = new CourseAdapter(this, store, this::openDetail);
        adapter.setAction("取消收藏", c -> {
            store.toggleFavorite(c.id);
            render();
            Snackbar.make(findViewById(R.id.rv), "已取消收藏：" + c.name, Snackbar.LENGTH_LONG)
                    .setAction("復原", v -> {
                        store.toggleFavorite(c.id);
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
        List<Course> favs = repo.byIds(store.favoriteIds());
        tvHeader.setText("共 " + favs.size() + " 門收藏，點卡片看詳情、加入課表。");
        btnAddAll.setVisibility(favs.isEmpty() ? View.GONE : View.VISIBLE);
        adapter.setItems(favs);
        tvEmpty.setVisibility(favs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** 列出「還沒加入、而且不會衝堂」的收藏課程。 */
    private void showAddable() {
        List<Course> schedule = scheduleCourses();
        List<Course> ok = new ArrayList<>();
        for (Course c : repo.byIds(store.favoriteIds())) {
            if (!store.inSchedule(c.id) && !com.example.nutccourse.logic.ScheduleLogic.conflictsWithAny(c, schedule)) {
                ok.add(c);
            }
        }
        if (ok.isEmpty()) {
            toast("沒有可以直接加入（不衝堂且未加入）的收藏");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Course c : ok) sb.append("• ").append(c.name).append("　").append(c.timeSummary()).append("\n");
        new MaterialAlertDialogBuilder(this)
                .setTitle("可直接加入的收藏（" + ok.size() + " 門）")
                .setMessage(sb.toString().trim() + "\n\n註：這些課彼此之間也可能衝堂，加入時會再逐一檢查。")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("逐一加入", (d, w) -> addSequentially(ok, 0))
                .show();
    }

    /** 依序加入；若前面加入的收藏跟後面的衝堂，會跳出衝堂確認框。 */
    private void addSequentially(List<Course> list, int index) {
        if (index >= list.size()) {
            render();
            return;
        }
        Course c = list.get(index);
        if (store.inSchedule(c.id)) {
            addSequentially(list, index + 1);
            return;
        }
        if (!com.example.nutccourse.logic.ScheduleLogic.conflictsWithAny(c, scheduleCourses())) {
            store.addToSchedule(c.id);
            addSequentially(list, index + 1);
        } else {
            render();
            confirmAddToSchedule(c, () -> addSequentially(list, index + 1));
        }
    }
}
