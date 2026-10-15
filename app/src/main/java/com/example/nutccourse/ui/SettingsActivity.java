package com.example.nutccourse.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.example.nutccourse.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

/** 畫面 14：設定。顯示選項與清除本機資料。 */
public class SettingsActivity extends BaseActivity {
    private TextView tvStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar(getString(R.string.title_settings), true);
        tvStorage = findViewById(R.id.tvStorage);

        MaterialSwitch swWeekend = findViewById(R.id.swWeekend);
        swWeekend.setChecked(store.showWeekend());
        swWeekend.setOnCheckedChangeListener((b, checked) -> store.setShowWeekend(checked));

        MaterialSwitch swDefaultAny = findViewById(R.id.swDefaultAny);
        swDefaultAny.setChecked(store.defaultMatchAny());
        swDefaultAny.setOnCheckedChangeListener((b, checked) -> store.setDefaultMatchAny(checked));

        findViewById(R.id.btnClearSchedule).setOnClickListener(v ->
                confirm("清空我的課表", "確定要移除課表裡的全部 " + store.scheduleIds().size() + " 門課嗎？", () -> {
                    store.clearSchedule();
                    toast("已清空課表");
                }));
        findViewById(R.id.btnClearFavorites).setOnClickListener(v ->
                confirm("清空收藏", "確定要清空全部 " + store.favoriteIds().size() + " 門收藏嗎？", () -> {
                    store.clearFavorites();
                    toast("已清空收藏");
                }));
        findViewById(R.id.btnResetFilter).setOnClickListener(v ->
                confirm("重設篩選條件", "搜尋頁的關鍵字與所有篩選條件都會清除。", () -> {
                    store.resetFilter();
                    toast("已重設篩選條件");
                }));
        updateStorage();
    }

    private void confirm(String title, String msg, Runnable action) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    action.run();
                    updateStorage();
                })
                .show();
    }

    private void updateStorage() {
        String f = store.loadFilter().describe();
        tvStorage.setText("課表：" + store.scheduleIds().size() + " 門\n"
                + "收藏：" + store.favoriteIds().size() + " 門\n"
                + "篩選條件：" + (f.isEmpty() ? "未設定" : f));
    }
}
