package com.chadderbox.launchbox.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.components.FontTextView;
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
        holder.getTitle().setText(option.getTitle());
        holder.getSubtitle().setText(option.getSubtitle((SettingsActivity)holder.itemView.getContext()));
        holder.itemView.setOnClickListener(v -> option.performClick((SettingsActivity)v.getContext()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class SettingItemHolder extends RecyclerView.ViewHolder {
        private final FontTextView mTitle;
        private final FontTextView mSubtitle;

        public FontTextView getTitle() {
            return mTitle;
        }

        public FontTextView getSubtitle() {
            return mSubtitle;
        }

        SettingItemHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mSubtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
