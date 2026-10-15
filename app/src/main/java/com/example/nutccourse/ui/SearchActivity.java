package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.logic.CourseFilter;
import com.example.nutccourse.logic.FilterCriteria;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/** 畫面 3：課程搜尋與結果列表。 */
public class SearchActivity extends BaseActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable runSearch = this::runSearch;

    private FilterCriteria criteria;
    private CourseAdapter adapter;
    private TextInputEditText etKeyword;
    private TextView tvCount, tvEmpty, tvFilterSummary;
    private View btnClearFilter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupToolbar(getString(R.string.title_search), true);

        etKeyword = findViewById(R.id.etKeyword);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvFilterSummary = findViewById(R.id.tvFilterSummary);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        rv = findViewById(R.id.rv);

        adapter = new CourseAdapter(this, store, this::openDetail);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        criteria = store.loadFilter();
        etKeyword.setText(criteria.keyword);
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                criteria.keyword = s.toString();
                store.saveFilter(criteria);
                handler.removeCallbacks(runSearch);
                handler.postDelayed(runSearch, 250);   // 打字時稍微延遲，避免每個字都重算
            }
        });

        findViewById(R.id.btnFilter).setOnClickListener(v ->
                startActivity(new Intent(this, FilterActivity.class)));
        btnClearFilter.setOnClickListener(v -> {
            String kw = criteria.keyword;
            criteria = new FilterCriteria();
            criteria.keyword = kw;
            criteria.matchAny = store.defaultMatchAny();
            store.saveFilter(criteria);
            runSearch();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 從篩選頁回來時重新讀取條件（保留目前輸入的關鍵字）
        String kw = etKeyword.getText() == null ? "" : etKeyword.getText().toString();
        criteria = store.loadFilter();
        criteria.keyword = kw;
        runSearch();
    }

    private void runSearch() {
        String summary = criteria.describe();
        tvFilterSummary.setText(summary.isEmpty() ? "未設定篩選條件" : summary);
        btnClearFilter.setVisibility(criteria.hasConditions() ? View.VISIBLE : View.GONE);

        if (criteria.isEmpty()) {
            adapter.setItems(java.util.Collections.emptyList());
            tvCount.setText("");
            showEmpty(getString(R.string.search_empty_prompt));
            return;
        }
        List<Course> schedule = criteria.hideConflict ? scheduleCourses() : null;
        List<Course> result = CourseFilter.apply(repo.all(), criteria, schedule);
        result.sort((a, b) -> a.sortKey() - b.sortKey());
        int total = result.size();
        if (total > CourseFilter.RESULT_LIMIT) {
            result = result.subList(0, CourseFilter.RESULT_LIMIT);
            tvCount.setText("共 " + total + " 筆，只顯示前 " + CourseFilter.RESULT_LIMIT + " 筆，請再縮小範圍");
        } else {
            tvCount.setText("共 " + total + " 筆");
        }
        adapter.setItems(result);
        rv.scrollToPosition(0);
        if (total == 0) showEmpty(getString(R.string.search_no_result));
        else tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
