package com.chadderbox.launchbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.FontHelper;
import com.chadderbox.launchbox.utils.IconPackLoader;

import java.util.function.Consumer;

public class AppViewHolder extends RecyclerView.ViewHolder {
    private final ImageView mIcon;
    private final TextView mLabel;

    public AppViewHolder(@NonNull View itemView, Consumer<AppInfo> shortPressListener, Consumer<AppInfo> longPressListener) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.item_icon);
        mLabel = itemView.findViewById(R.id.item_name);

        itemView.setOnClickListener(v -> {
            AppInfo app = (AppInfo) v.getTag();
            if (shortPressListener != null) {
                shortPressListener.accept(app);
            }
        });

        itemView.setOnLongClickListener(v -> {
            AppInfo app = (AppInfo) v.getTag();
            if (app != null) {
                longPressListener.accept(app);
                return true;
            }
            return false;
        });
    }

    public void bind(AppInfo app, IconPackLoader iconPackLoader) {
        var drawable = iconPackLoader.loadAppIcon(app.getPackageName());

        itemView.setTag(app);
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
