package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.MainActivity;
import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.FontHelper;
import com.chadderbox.launchbox.utils.IconPackLoader;

import java.util.function.Consumer;

public class AppViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mIcon;
    private final TextView mLabel;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.item_icon);
        mLabel = itemView.findViewById(R.id.item_name);

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

    public void bind(AppItem appItem, IconPackLoader iconPackLoader) {
        var app = appItem.getAppInfo();
        var drawable = iconPackLoader.loadAppIcon(app.getPackageName());

        itemView.setTag(appItem);
        mLabel.setText(app.getLabel());
        mLabel.setTypeface(FontHelper.getFont(SettingsManager.getFont()));

        if (drawable != null) {
            mIcon.setVisibility(View.VISIBLE);
            mIcon.setImageDrawable(drawable);
        } else {
            mIcon.setVisibility(View.GONE);
        }
    }
}
