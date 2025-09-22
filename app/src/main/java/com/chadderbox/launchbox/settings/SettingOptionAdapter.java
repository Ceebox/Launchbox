package com.chadderbox.launchbox.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.options.ISettingOption;

import java.util.List;

public final class SettingOptionAdapter extends RecyclerView.Adapter<SettingOptionAdapter.SettingItemHolder> {

    private final List<ISettingOption> mItems;

    public SettingOptionAdapter(List<ISettingOption> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public SettingItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var view = inflater.inflate(R.layout.setting_item_layout, parent, false);
        return new SettingItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingItemHolder holder, int position) {
        var option = mItems.get(position);
        holder.title.setText(option.getTitle());
        holder.subtitle.setText(option.getSubtitle((SettingsActivity)holder.itemView.getContext()));
        holder.itemView.setOnClickListener(v -> option.performClick((SettingsActivity)v.getContext()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class SettingItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        SettingItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
