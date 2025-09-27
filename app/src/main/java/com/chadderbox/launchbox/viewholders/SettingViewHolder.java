package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.utils.ShadowHelper;
import com.chadderbox.launchbox.utils.TintHelper;

public final class SettingViewHolder extends RecyclerView.ViewHolder {
    private final TextView mTextView;

    public SettingViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.item_name);

        ShadowHelper.applySettings(mTextView);

        itemView.setOnClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performOpenAction(v.getContext());
            }
        });

        itemView.setOnLongClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performHoldAction(v.getContext());
                return true;
            }

            return false;
        });
    }

    public void bind(SettingItem item) {
        itemView.setTag(item);

        var context = itemView.getContext();
        var icon = (ImageView)itemView.findViewById(R.id.item_icon);
        var drawable = AppCompatResources.getDrawable(context, R.drawable.ic_settings);
        icon.setImageDrawable(TintHelper.tryTintIcon(context, drawable));
        mTextView.setText(item.getTitle());
    }
}
