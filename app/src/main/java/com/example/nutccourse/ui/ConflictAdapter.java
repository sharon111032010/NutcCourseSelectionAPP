package com.example.nutccourse.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;
import com.example.nutccourse.data.Course;
import com.example.nutccourse.logic.ScheduleLogic;

import java.util.ArrayList;
import java.util.List;

/** 衝堂清單：每列是一處衝突，兩門課各自可以點開或移除。 */
public class ConflictAdapter extends RecyclerView.Adapter<ConflictAdapter.VH> {

    public interface Callbacks {
        void onOpen(Course c);

        void onRemove(Course c);
    }

    private final List<ScheduleLogic.Conflict> items = new ArrayList<>();
    private final Callbacks cb;

    public ConflictAdapter(Callbacks cb) {
        this.cb = cb;
    }

    public void setItems(List<ScheduleLogic.Conflict> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conflict, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ScheduleLogic.Conflict cf = items.get(position);
        h.when.setText("⚠ " + cf.timeLabel());
        h.a.setText(label(cf.a));
        h.b.setText(label(cf.b));
        h.rowA.setOnClickListener(v -> cb.onOpen(cf.a));
        h.rowB.setOnClickListener(v -> cb.onOpen(cf.b));
        h.removeA.setOnClickListener(v -> cb.onRemove(cf.a));
        h.removeB.setOnClickListener(v -> cb.onRemove(cf.b));
    }

    private static String label(Course c) {
        return c.name + "\n" + c.className + " · " + c.credits + " 學分 · " + c.timeSummary();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView when, a, b;
        final View rowA, rowB, removeA, removeB;

        VH(View v) {
            super(v);
            when = v.findViewById(R.id.tvWhen);
            a = v.findViewById(R.id.tvA);
            b = v.findViewById(R.id.tvB);
            rowA = v.findViewById(R.id.rowA);
            rowB = v.findViewById(R.id.rowB);
            removeA = v.findViewById(R.id.btnRemoveA);
            removeB = v.findViewById(R.id.btnRemoveB);
        }
    }
}
