package com.chadderbox.launchbox.viewholders;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

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
    private ItemTouchHelper mTouchHelper;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.item_icon);
        mLabel = itemView.findViewById(R.id.item_name);

        ShadowHelper.applySettings(mLabel.getContext(), mLabel.getPaint());

        itemView.setOnClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performOpenAction(v);
            }
        });

        itemView.setOnLongClickListener(v -> {
            var tag = v.getTag();
            if (tag instanceof ListItem item) {
                item.performHoldAction(v);
                return true;
            }

            return false;
        });

        SettingsManager.registerChangeListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bind(AppItem appItem, IconPackLoader iconPackLoader, ItemTouchHelper touchHelper, boolean isEditMode) {
        mAppItem = appItem;
        mIconPackLoader = iconPackLoader;
        mTouchHelper = touchHelper;

        var app = mAppItem.getAppInfo();
        itemView.setTag(mAppItem);
        mLabel.setText(app.getLabel());
        mLabel.setTypeface(FontHelper.getFont(SettingsManager.getFont()));

        var dragHandle = (ShadowImageView) itemView.findViewById(R.id.drag_handle);
        dragHandle.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        if (isEditMode) {
            dragHandle.regenerateShadow();
            dragHandle.refreshDrawableState();
            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (mTouchHelper != null) {
                        mTouchHelper.startDrag(this);
                    }
                }

                return false;
            });
        }

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
        var packageName = mAppItem.getAppInfo().getPackageName();
        var category = mAppItem.getAppInfo().getCategory();

        // Clear the old icon immediately to prevent stale images
        mIcon.setImageDrawable(null);

        // Clear the shadow bitmap in case we're getting re-used
        mIcon.clearShadowBitmap();

        mIconExecutor.submit(() -> {
            var drawable = mIconPackLoader.loadAppIcon(packageName, category);
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
