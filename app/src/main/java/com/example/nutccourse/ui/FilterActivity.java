package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Session;
import com.example.nutccourse.logic.FilterCriteria;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

/** 畫面 4：進階篩選。設定星期、節次（完整符合／有碰到）、修別、系所、學制、年級等。 */
public class FilterActivity extends BaseActivity {
    public static final String EXTRA_OPEN_SEARCH_AFTER = "open_search_after";
    private static final String ANY = "不限";
    private static final int MAX_PERIOD = 13;

    private ChipGroup cgDays, cgPeriods;
    private RadioGroup rgMode, rgType;
    private Spinner spDept, spTrack, spGrade, spCategory;
    private MaterialSwitch swOpenOther, swEnglish, swHideUnscheduled, swHideConflict;
    private TextView tvModeExample;

    private List<String> deptOptions, trackOptions, gradeOptions, categoryOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        setupToolbar(getString(R.string.title_filter), true);

        cgDays = findViewById(R.id.cgDays);
        cgPeriods = findViewById(R.id.cgPeriods);
        rgMode = findViewById(R.id.rgMode);
        rgType = findViewById(R.id.rgType);
        spDept = findViewById(R.id.spDept);
        spTrack = findViewById(R.id.spTrack);
        spGrade = findViewById(R.id.spGrade);
        spCategory = findViewById(R.id.spCategory);
        swOpenOther = findViewById(R.id.swOpenOther);
        swEnglish = findViewById(R.id.swEnglish);
        swHideUnscheduled = findViewById(R.id.swHideUnscheduled);
        swHideConflict = findViewById(R.id.swHideConflict);
        tvModeExample = findViewById(R.id.tvModeExample);

        for (int d = 1; d <= 7; d++) cgDays.addView(makeChip(cgDays, "星期" + Session.dayName(d), d));
        for (int p = 1; p <= MAX_PERIOD; p++) cgPeriods.addView(makeChip(cgPeriods, "第" + p + "節", p));

        deptOptions = withAny(repo.departments());
        trackOptions = withAny(repo.tracks());
        gradeOptions = new ArrayList<>();
        gradeOptions.add(ANY);
        for (int g = 1; g <= 5; g++) gradeOptions.add(g + " 年級");
        categoryOptions = withAny(repo.categories());
        bindSpinner(spDept, deptOptions);
        bindSpinner(spTrack, trackOptions);
        bindSpinner(spGrade, gradeOptions);
        bindSpinner(spCategory, categoryOptions);

        rgMode.setOnCheckedChangeListener((g, id) -> updateModeExample());

        fillFrom(store.loadFilter());

        findViewById(R.id.btnClearPeriods).setOnClickListener(v -> cgPeriods.clearCheck());
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            FilterCriteria empty = new FilterCriteria();
            empty.matchAny = store.defaultMatchAny();
            fillFrom(empty);
        });
        findViewById(R.id.btnApply).setOnClickListener(v -> apply());
    }

    private Chip makeChip(ChipGroup parent, String text, int tag) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filter, parent, false);
        chip.setText(text);
        chip.setTag(tag);
        chip.setId(android.view.View.generateViewId());
        return chip;
    }

    private static List<String> withAny(List<String> list) {
        List<String> out = new ArrayList<>();
        out.add(ANY);
        out.addAll(list);
        return out;
    }

    private void bindSpinner(Spinner sp, List<String> options) {
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(ad);
    }

    private void select(Spinner sp, List<String> options, String value) {
        int idx = value == null || value.isEmpty() ? 0 : options.indexOf(value);
        sp.setSelection(Math.max(idx, 0));
    }

    private String selected(Spinner sp, List<String> options) {
        int pos = sp.getSelectedItemPosition();
        return pos <= 0 ? "" : options.get(pos);
    }

    private void fillFrom(FilterCriteria f) {
        for (int i = 0; i < cgDays.getChildCount(); i++) {
            Chip c = (Chip) cgDays.getChildAt(i);
            c.setChecked(f.days.contains((Integer) c.getTag()));
        }
        for (int i = 0; i < cgPeriods.getChildCount(); i++) {
            Chip c = (Chip) cgPeriods.getChildAt(i);
            c.setChecked(f.periods.contains((Integer) c.getTag()));
        }
        rgMode.check(f.matchAny ? R.id.rbAny : R.id.rbFull);
        rgType.check("必".equals(f.type) ? R.id.rbTypeReq : "選".equals(f.type) ? R.id.rbTypeEle : R.id.rbTypeAll);
        select(spDept, deptOptions, f.dept);
        select(spTrack, trackOptions, f.track);
        spGrade.setSelection(f.grade >= 1 && f.grade <= 5 ? f.grade : 0);
        select(spCategory, categoryOptions, f.category);
        swOpenOther.setChecked(f.openForOtherOnly);
        swEnglish.setChecked(f.englishOnly);
        swHideUnscheduled.setChecked(f.hideUnscheduled);
        swHideConflict.setChecked(f.hideConflict);
        updateModeExample();
    }

    private void updateModeExample() {
        boolean any = rgMode.getCheckedRadioButtonId() == R.id.rbAny;
        tvModeExample.setText(any
                ? "例：勾第 1～2 節，「一 1-3」「一 2-4」也會列出，因為有碰到。"
                : "例：勾第 1～2 節，「一 1-2」「一 1」「一 2」符合；「一 1-3」「一 2-4」超出範圍，不列出。");
    }

    private void apply() {
        FilterCriteria f = new FilterCriteria();
        f.keyword = store.loadFilter().keyword;   // 關鍵字由搜尋頁管理，這裡保留
        for (int i = 0; i < cgDays.getChildCount(); i++) {
            Chip c = (Chip) cgDays.getChildAt(i);
            if (c.isChecked()) f.days.add((Integer) c.getTag());
        }
        for (int i = 0; i < cgPeriods.getChildCount(); i++) {
            Chip c = (Chip) cgPeriods.getChildAt(i);
            if (c.isChecked()) f.periods.add((Integer) c.getTag());
        }
        f.matchAny = rgMode.getCheckedRadioButtonId() == R.id.rbAny;
        int typeId = rgType.getCheckedRadioButtonId();
        f.type = typeId == R.id.rbTypeReq ? "必" : typeId == R.id.rbTypeEle ? "選" : "";
        f.dept = selected(spDept, deptOptions);
        f.track = selected(spTrack, trackOptions);
        f.grade = Math.max(spGrade.getSelectedItemPosition(), 0);
        f.category = selected(spCategory, categoryOptions);
        f.openForOtherOnly = swOpenOther.isChecked();
        f.englishOnly = swEnglish.isChecked();
        f.hideUnscheduled = swHideUnscheduled.isChecked();
        f.hideConflict = swHideConflict.isChecked();
        store.saveFilter(f);

        if (getIntent().getBooleanExtra(EXTRA_OPEN_SEARCH_AFTER, false)) {
            startActivity(new Intent(this, SearchActivity.class));
        }
        finish();
    }
}
