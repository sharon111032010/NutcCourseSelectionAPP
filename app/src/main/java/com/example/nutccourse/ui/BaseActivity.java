package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.CourseRepository;
import com.example.nutccourse.data.UserStore;
import com.example.nutccourse.logic.ScheduleLogic;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/** 所有畫面共用：資料存取、Toolbar、加入課表（含衝堂確認）。 */
public abstract class BaseActivity extends AppCompatActivity {
    protected CourseRepository repo;
    protected UserStore store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 若 App 被系統回收後直接回到某個畫面，這裡會同步重新載入資料
        repo = CourseRepository.get(this);
        store = new UserStore(this);
    }

    protected void setupToolbar(CharSequence title, boolean showBack) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
        }
    }

    protected void setToolbarSubtitle(CharSequence subtitle) {
        if (getSupportActionBar() != null) getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    protected List<Course> scheduleCourses() {
        return repo.byIds(store.scheduleIds());
    }

    protected void openDetail(Course c) {
        Intent i = new Intent(this, CourseDetailActivity.class);
        i.putExtra(CourseDetailActivity.EXTRA_ID, c.id);
        startActivity(i);
    }

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 加入課表前先檢查衝堂。有衝突時跳出對話框列出衝突課程與重疊節次，
     * 使用者可以取消或「仍然加入」。
     */
    protected void confirmAddToSchedule(Course c, @Nullable Runnable onAdded) {
        if (store.inSchedule(c.id)) {
            toast("已經在課表裡了");
            return;
        }
        List<ScheduleLogic.Conflict> conflicts = ScheduleLogic.conflictsWith(c, scheduleCourses());
        if (conflicts.isEmpty()) {
            store.addToSchedule(c.id);
            toast("已加入課表：" + c.name);
            if (onAdded != null) onAdded.run();
            return;
        }
        StringBuilder msg = new StringBuilder("「" + c.name + "」會跟下列課程衝堂：\n\n");
        for (ScheduleLogic.Conflict cf : conflicts) {
            msg.append("• ").append(cf.b.name).append("（").append(cf.b.className).append("）\n   ")
                    .append(cf.timeLabel()).append("\n");
        }
        msg.append("\n仍然加入的話，課表上兩門課會疊在同一格顯示。");
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠ 會衝堂")
                .setMessage(msg.toString())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("仍然加入", (d, w) -> {
                    store.addToSchedule(c.id);
                    toast("已加入課表（有衝堂）");
                    if (onAdded != null) onAdded.run();
                })
                .show();
    }
}
