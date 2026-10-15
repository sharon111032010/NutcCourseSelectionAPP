package com.example.nutccourse.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nutccourse.R;
import com.example.nutccourse.data.CourseRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 畫面 1：啟動畫面。在背景執行緒讀取 assets 裡的課程 JSON，讀完進首頁。 */
public class SplashActivity extends AppCompatActivity {
    private static final long MIN_SHOW_MS = 700;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView status = findViewById(R.id.tvStatus);
        View progress = findViewById(R.id.progress);

        long start = SystemClock.uptimeMillis();
        executor.execute(() -> {
            try {
                CourseRepository repo = CourseRepository.get(getApplicationContext());
                int count = repo.all().size();
                long wait = Math.max(0, MIN_SHOW_MS - (SystemClock.uptimeMillis() - start));
                main.postDelayed(() -> {
                    status.setText("已載入 " + count + " 門課");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }, wait);
            } catch (RuntimeException e) {
                main.post(() -> {
                    progress.setVisibility(View.GONE);
                    status.setText("課程資料讀取失敗\n" + e.getMessage());
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }
}
