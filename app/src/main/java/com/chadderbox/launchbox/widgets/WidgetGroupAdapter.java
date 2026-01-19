package com.chadderbox.launchbox.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetProviderInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WidgetGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> mItems = new ArrayList<>();
    private final OnWidgetSelectedListener mListener;

    public WidgetGroupAdapter(Map<String, List<AppWidgetProviderInfo>> grouped, OnWidgetSelectedListener listener) {
        mListener = listener;
        for (var entry : grouped.entrySet()) {
            mItems.add(entry.getKey());
            mItems.addAll(entry.getValue());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_widget_header, parent, false));
        }

        return new ItemViewHolder(inflater.inflate(R.layout.item_widget_provider, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        var item = mItems.get(position);
        if (holder instanceof HeaderViewHolder headerHolder) {
            headerHolder.title.setText((String) item);
        } else if (holder instanceof ItemViewHolder itemHolder) {
            var info = (AppWidgetProviderInfo) item;
            itemHolder.label.setText(info.label);
            itemHolder.dims.setText(info.minWidth + " x " + info.minHeight);
            itemHolder.itemView.setOnClickListener(v -> mListener.onSelected(info));

            // TODO: Widget icon support (that isn't UGLY)
//            var icon = info.loadPreviewImage(itemHolder.itemView.getContext(), 0);
//            if (icon == null) {
//                icon = info.loadIcon(itemHolder.itemView.getContext(), 0);
//            }
//
//            itemHolder.icon.setImageDrawable(icon);
//            itemHolder.itemView.setOnClickListener(v -> mListener.onSelected(info));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(android.view.View v) {
            super(v);
            title = v.findViewById(R.id.header_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        TextView dims;
//        ImageView icon;
        ItemViewHolder(android.view.View v) {
            super(v);
            label = v.findViewById(R.id.widget_label);
            dims = v.findViewById(R.id.widget_dims);
//            icon = v.findViewById(R.id.widget_icon);
        }
    }

    public interface OnWidgetSelectedListener {
        void onSelected(AppWidgetProviderInfo info);
    }
}
