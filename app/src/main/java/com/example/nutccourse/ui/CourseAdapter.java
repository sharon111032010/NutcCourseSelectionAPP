package com.example.nutccourse.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.data.UserStore;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 課程卡片列表，搜尋、班級課程、通識、收藏、已選清單共用。 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.VH> {

    public interface OnCourseClick {
        void onClick(Course c);
    }

    private final Context ctx;
    private final UserStore store;
    private final OnCourseClick onClick;
    private final List<Course> items = new ArrayList<>();
    private Set<String> scheduleIds = new HashSet<>();
    private Set<String> favoriteIds = new HashSet<>();

    private String actionLabel;
    private OnCourseClick onAction;

    public CourseAdapter(Context ctx, UserStore store, OnCourseClick onClick) {
        this.ctx = ctx;
        this.store = store;
        this.onClick = onClick;
    }

    /** 每張卡片右下角加一個按鈕（例如「移除」）。 */
    public void setAction(String label, OnCourseClick onAction) {
        this.actionLabel = label;
        this.onAction = onAction;
    }

    public void setItems(List<Course> list) {
        items.clear();
        items.addAll(list);
        refreshMarks();
    }

    /** 重新讀取「已加入課表」「已收藏」標記。 */
    public void refreshMarks() {
        scheduleIds = store.scheduleIds();
        favoriteIds = store.favoriteIds();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Course c = items.get(position);
        h.name.setText(c.name);
        h.meta.setText(UiUtil.metaLine(c));
        h.time.setText(c.timeSummary());
        UiUtil.bindBadges(ctx, c, h.type, h.credits, h.program, h.extra);

        StringBuilder marks = new StringBuilder();
        if (scheduleIds.contains(c.id)) marks.append("✓已選");
        if (favoriteIds.contains(c.id)) {
            if (marks.length() > 0) marks.append(" ");
            marks.append("★");
        }
        h.marks.setText(marks);

        h.itemView.setOnClickListener(v -> onClick.onClick(c));
        if (onAction != null) {
            h.action.setVisibility(View.VISIBLE);
            h.action.setText(actionLabel);
            h.action.setOnClickListener(v -> onAction.onClick(c));
        } else {
            h.action.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, marks, meta, time, type, credits, program, extra;
        final MaterialButton action;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            marks = v.findViewById(R.id.tvMarks);
            meta = v.findViewById(R.id.tvMeta);
            time = v.findViewById(R.id.tvTime);
            type = v.findViewById(R.id.tvType);
            credits = v.findViewById(R.id.tvCredits);
            program = v.findViewById(R.id.tvProgram);
            extra = v.findViewById(R.id.tvExtra);
            action = v.findViewById(R.id.btnAction);
        }
    }
}
