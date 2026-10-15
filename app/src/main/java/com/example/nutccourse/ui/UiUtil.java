package com.example.nutccourse.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;

/** 徽章（必／選、學分、學制）等共用的畫面小工具。 */
public final class UiUtil {
    private UiUtil() {
    }

    /** 課表格子的色塊，淺色系，在深色模式下也讀得到黑字。 */
    public static final int[] BLOCK_COLORS = {
            0xFFBFE3F5, 0xFFFFD9B3, 0xFFC8EBC9, 0xFFF3C6D9, 0xFFDCD0F5,
            0xFFFFF0A8, 0xFFB8E6DF, 0xFFE8D5C4, 0xFFCFD8F7, 0xFFF7CFC4
    };

    public static int blockColor(Course c) {
        return BLOCK_COLORS[Math.abs(c.id.hashCode()) % BLOCK_COLORS.length];
    }

    public static void tint(View v, int colorRes) {
        v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(), colorRes)));
    }

    public static void bindBadges(Context ctx, Course c, TextView type, TextView credits,
                                  TextView program, TextView extra) {
        type.setText(c.isRequired() ? "必修" : "選修");
        type.setTextColor(ContextCompat.getColor(ctx, c.isRequired() ? R.color.required : R.color.elective));
        tint(type, c.isRequired() ? R.color.required_bg : R.color.elective_bg);

        credits.setText(c.credits + " 學分");

        String prog = c.programLabel();
        if (prog != null) {
            program.setVisibility(View.VISIBLE);
            program.setText(prog);
            program.setTextColor(ContextCompat.getColor(ctx, R.color.track_fg));
            tint(program, R.color.track_bg);
        } else {
            program.setVisibility(View.GONE);
        }

        String ex = extraLabel(c);
        if (ex != null) {
            extra.setVisibility(View.VISIBLE);
            extra.setText(ex);
            extra.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
            tint(extra, R.color.accent_container);
        } else {
            extra.setVisibility(View.GONE);
        }
    }

    /** 通識領域／體育項目／全英語／開放外系，挑最有資訊量的一個。 */
    public static String extraLabel(Course c) {
        if (c.category != null) return c.category;
        if (c.peActivity != null) return c.peActivity;
        if (c.englishTaught) return "全英語";
        if (c.openForOther) return "開放外系";
        return null;
    }

    public static String metaLine(Course c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.className);
        if (c.teacher != null && !c.teacher.isEmpty()) sb.append(" · ").append(c.teacher);
        if (c.groupLabel() != null) sb.append(" · ").append(c.groupLabel());
        sb.append(" · ").append(c.id);
        return sb.toString();
    }
}
