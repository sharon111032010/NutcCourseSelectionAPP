package com.example.nutccourse.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/** 畫面 9：通識／體育／語言等跨系選修課，依領域或類別快速切換。 */
public class GeneralEdActivity extends BaseActivity {
    private static final String ALL_GE = "全部通識";
    private static final String PE = "體育";
    private static final String LANG = "語言";
    private static final String ENGLISH = "全英語課程";
    private static final String OPEN = "開放外系";

    private CourseAdapter adapter;
    private TextView tvCount;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_ed);
        setupToolbar(getString(R.string.title_general_ed), true);
        tvCount = findViewById(R.id.tvCount);
        rv = findViewById(R.id.rv);

        adapter = new CourseAdapter(this, store, this::openDetail);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        List<String> options = new ArrayList<>();
        options.add(ALL_GE);
        options.addAll(repo.categories());
        options.add(PE);
        options.add(LANG);
        options.add(ENGLISH);
        options.add(OPEN);

        ChipGroup cg = findViewById(R.id.cgCategory);
        for (String opt : options) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filter, cg, false);
            chip.setId(android.view.View.generateViewId());
            chip.setText(opt);
            chip.setTag(opt);
            cg.addView(chip);
        }
        cg.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            Chip c = group.findViewById(ids.get(0));
            show((String) c.getTag());
        });
        ((Chip) cg.getChildAt(0)).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.refreshMarks();
    }

    private void show(String option) {
        List<Course> out = new ArrayList<>();
        for (Course c : repo.all()) {
            boolean ok;
            switch (option) {
                case ALL_GE:
                    ok = "通識".equals(c.dept);
                    break;
                case PE:
                    ok = "體育".equals(c.dept);
                    break;
                case LANG:
                    ok = "語言".equals(c.dept);
                    break;
                case ENGLISH:
                    ok = c.englishTaught;
                    break;
                case OPEN:
                    ok = c.openForOther;
                    break;
                default:
                    ok = option.equals(c.category);
            }
            if (ok) out.add(c);
        }
        if (PE.equals(option)) {
            // 體育課依運動項目排序，同項目的放一起
            out.sort((a, b) -> {
                String pa = a.peActivity == null ? "￿" : a.peActivity;
                String pb = b.peActivity == null ? "￿" : b.peActivity;
                int r = pa.compareTo(pb);
                return r != 0 ? r : a.sortKey() - b.sortKey();
            });
        } else {
            out.sort((a, b) -> a.sortKey() - b.sortKey());
        }
        tvCount.setText(option + "：共 " + out.size() + " 門");
        adapter.setItems(out);
        rv.scrollToPosition(0);
    }
}
