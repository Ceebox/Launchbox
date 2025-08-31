package com.chadderbox.launcherbox.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.R;
import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.settings.SettingsManager;
import com.chadderbox.launcherbox.utils.FontHelper;
import com.chadderbox.launcherbox.utils.IconPackLoader;

import java.util.function.Consumer;

public class AppViewHolder extends RecyclerView.ViewHolder {
    private final ImageView icon;
    private final TextView label;

    public AppViewHolder(@NonNull View itemView, Consumer<AppInfo> shortPressListener, Consumer<AppInfo> longPressListener) {
        super(itemView);
        icon = itemView.findViewById(R.id.item_icon);
        label = itemView.findViewById(R.id.item_name);

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
        label.setText(app.getLabel());
        label.setTypeface(FontHelper.getFont(SettingsManager.getFont()));

        if (drawable != null) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageDrawable(drawable);
        } else {
            icon.setVisibility(View.GONE);
        }
    }
}
