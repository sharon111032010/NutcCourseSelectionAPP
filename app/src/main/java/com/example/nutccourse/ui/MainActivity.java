package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.CourseRepository;
import com.example.nutccourse.logic.ScheduleLogic;

import java.util.List;

/** 畫面 2：首頁。統計列 + 十個功能入口。 */
public class MainActivity extends BaseActivity {
    private TextView tvMine, tvCredits, tvConflicts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar(getString(R.string.title_home), false);
        setToolbarSubtitle(CourseRepository.SEMESTER_LABEL + " 學期 · 日間部");

        ((TextView) findViewById(R.id.tvStatCourses)).setText(String.valueOf(repo.all().size()));
        tvMine = findViewById(R.id.tvStatMine);
        tvCredits = findViewById(R.id.tvStatCredits);
        tvConflicts = findViewById(R.id.tvStatConflicts);
        ((TextView) findViewById(R.id.tvDataInfo)).setText(
                "資料快照：" + repo.updatedAtLabel() + "　·　非學校官方系統");

        findViewById(R.id.tvFakeSearch).setOnClickListener(v -> go(SearchActivity.class));
        findViewById(R.id.cardSearch).setOnClickListener(v -> go(SearchActivity.class));
        findViewById(R.id.cardFilter).setOnClickListener(v -> {
            Intent i = new Intent(this, FilterActivity.class);
            i.putExtra(FilterActivity.EXTRA_OPEN_SEARCH_AFTER, true);
            startActivity(i);
        });
        findViewById(R.id.cardBrowse).setOnClickListener(v -> go(DepartmentListActivity.class));
        findViewById(R.id.cardGe).setOnClickListener(v -> go(GeneralEdActivity.class));
        findViewById(R.id.cardSchedule).setOnClickListener(v -> go(ScheduleActivity.class));
        findViewById(R.id.cardSelected).setOnClickListener(v -> go(SelectedCoursesActivity.class));
        findViewById(R.id.cardConflicts).setOnClickListener(v -> go(ConflictActivity.class));
        findViewById(R.id.cardFavorites).setOnClickListener(v -> go(FavoritesActivity.class));
        findViewById(R.id.cardSettings).setOnClickListener(v -> go(SettingsActivity.class));
        findViewById(R.id.cardAbout).setOnClickListener(v -> go(AboutActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Course> mine = scheduleCourses();
        ScheduleLogic.Stats st = ScheduleLogic.stats(mine);
        int conflicts = ScheduleLogic.allConflicts(mine).size();
        tvMine.setText(String.valueOf(st.count));
        tvCredits.setText(String.valueOf(st.credits));
        tvConflicts.setText(String.valueOf(conflicts));
        tvConflicts.setTextColor(conflicts > 0
                ? ContextCompat.getColor(this, R.color.accent) : 0xFFFFFFFF);
    }

    private void go(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }
}
