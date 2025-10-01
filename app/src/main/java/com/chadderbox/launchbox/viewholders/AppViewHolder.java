package com.chadderbox.launchbox.viewholders;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.ui.components.ShadowImageView;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.fonts.FontHelper;
import com.chadderbox.launchbox.icons.IconPackLoader;
import com.chadderbox.launchbox.ui.ShadowHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppViewHolder
    extends ViewHolderItemBase {

    private static final ExecutorService mIconExecutor = Executors.newCachedThreadPool();
    private static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final ShadowImageView mIcon;
    private final TextView mLabel;
    private IconPackLoader mIconPackLoader;
    private AppItem mAppItem;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.item_icon);
        mLabel = itemView.findViewById(R.id.item_name);

        ShadowHelper.applySettings(mLabel.getContext(), mLabel.getPaint());

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

        SettingsManager.registerChangeListener(this);
    }

    public void bind(AppItem appItem, IconPackLoader iconPackLoader) {
        mAppItem = appItem;
        mIconPackLoader = iconPackLoader;

        var app = mAppItem.getAppInfo();
        itemView.setTag(mAppItem);
        mLabel.setText(app.getLabel());
        mLabel.setTypeface(FontHelper.getFont(SettingsManager.getFont()));

        loadIcon();
    }

    @Override
    protected void whenSharedPreferencesChanged(String key) {
        super.whenSharedPreferencesChanged(key);

        if (SettingsManager.KEY_ICON_PACK.equals(key)) {
            loadIcon();
        }
    }

    private void loadIcon() {
        // Clear the shadow bitmap in case we're getting re-used
        mIcon.clearShadowBitmap();
        mIconExecutor.submit(() -> {
            var drawable = mIconPackLoader.loadAppIcon(mAppItem.getAppInfo().getPackageName(), mAppItem.getAppInfo().getCategory());
            mMainHandler.post(() -> {
                // Verify the same item is still bound, we could have been reused
                if (itemView.getTag() == mAppItem) {
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

    public ShadowImageView getIcon() {
        return mIcon;
    }
}
