package com.example.nutccourse.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutccourse.R;

import java.util.ArrayList;
import java.util.List;

/** 簡單的一列：標題、副標、右側數字。系所列表與班級列表使用。 */
public class SimpleRowAdapter extends RecyclerView.Adapter<SimpleRowAdapter.VH> {

    public static class Row {
        public final String key;
        public final String title;
        public final String subtitle;
        public final String trailing;

        public Row(String key, String title, String subtitle, String trailing) {
            this.key = key;
            this.title = title;
            this.subtitle = subtitle;
            this.trailing = trailing;
        }
    }

    public interface OnRowClick {
        void onClick(Row row);
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnRowClick onClick;

    public SimpleRowAdapter(List<Row> rows, OnRowClick onClick) {
        this.rows.addAll(rows);
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Row r = rows.get(position);
        h.title.setText(r.title);
        h.subtitle.setText(r.subtitle);
        h.subtitle.setVisibility(r.subtitle == null ? View.GONE : View.VISIBLE);
        h.trailing.setText(r.trailing);
        h.itemView.setOnClickListener(v -> onClick.onClick(r));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, subtitle, trailing;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            subtitle = v.findViewById(R.id.tvSubtitle);
            trailing = v.findViewById(R.id.tvTrailing);
        }
    }
}
