package com.chadderbox.launchbox.viewholders;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.FontHelper;
import com.chadderbox.launchbox.utils.IconPackLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppViewHolder extends RecyclerView.ViewHolder {

    private static final ExecutorService mIconExecutor = Executors.newCachedThreadPool();
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());

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

        itemView.setTag(appItem);
        mLabel.setText(app.getLabel());
        mLabel.setTypeface(FontHelper.getFont(SettingsManager.getFont()));

        mIconExecutor.submit(() -> {
            var drawable = iconPackLoader.loadAppIcon(app.getPackageName());
            mMainHandler.post(() -> {
                // Verify the same item is still bound, we could have been reused
                if (itemView.getTag() == appItem) {
                    if (drawable != null) {
                        mIcon.setVisibility(View.VISIBLE);
                        mIcon.setImageDrawable(drawable);
                    } else {
                        mIcon.setVisibility(View.GONE);
                    }
                }
            });
        });
    }
}
