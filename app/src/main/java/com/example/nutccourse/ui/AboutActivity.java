package com.example.nutccourse.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.CourseRepository;

/** 畫面 15：關於與資料說明。 */
public class AboutActivity extends BaseActivity {
    private static final String SOURCE_URL = "https://aisap.nutc.edu.tw/public/day/course_list.aspx?sem=1151";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupToolbar(getString(R.string.title_about), true);

        int total = repo.all().size();
        int parseErr = repo.countParseErrors();
        double ok = total == 0 ? 0 : 100.0 * (total - parseErr) / total;
        ((TextView) findViewById(R.id.tvDataStats)).setText(
                "學期：" + CourseRepository.SEMESTER_LABEL + "（日間部）\n"
                        + "課程數：" + total + " 門\n"
                        + "開課班級：" + repo.countClasses() + " 個\n"
                        + "系所／開課單位：" + repo.departments().size() + " 個\n"
                        + "無固定時段（機動排課）：" + repo.countNoSchedule() + " 門\n"
                        + String.format(java.util.Locale.ROOT, "時間解析成功率：%.2f%%（%d 筆格式特殊）\n", ok, parseErr)
                        + "資料快照時間：" + repo.updatedAtLabel());

        findViewById(R.id.btnOpenSource).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL)));
            } catch (ActivityNotFoundException e) {
                toast("找不到可以開啟網頁的 App");
            }
        });

        String version = "1.0";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        ((TextView) findViewById(R.id.tvVersion)).setText("版本 " + version + "　·　非學校官方系統");
    }
}
