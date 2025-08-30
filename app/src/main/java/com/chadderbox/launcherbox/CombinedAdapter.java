package com.chadderbox.launcherbox;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.components.FontTextView;
import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.data.ListItemType;
import com.chadderbox.launcherbox.settings.SettingsManager;
import com.chadderbox.launcherbox.utils.FontHelper;
import com.chadderbox.launcherbox.utils.IconPackLoader;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;

    private final List<ListItem> mItems;
    private final Consumer<AppInfo> mShortPressListener;
    private final Consumer<AppInfo> mLongPressListener;
    private final IconPackLoader mIconPackLoader;

    public CombinedAdapter(
            List<ListItem> items,
            Consumer<AppInfo> shortPressListener,
            Consumer<AppInfo> longPressListener,
            IconPackLoader iconPackLoader
    ) {
        mItems = items;
        mShortPressListener = shortPressListener;
        mLongPressListener = longPressListener;
        mIconPackLoader = iconPackLoader;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType() == ListItemType.HEADER ? TYPE_HEADER : TYPE_APP;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<ListItem> getItems() {
        return mItems;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        mItems.clear();
        this.notifyDataSetChanged();
    }

    public void add(ListItem item) {
        mItems.add(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAll(Collection<? extends ListItem> items) {
        mItems.addAll(items);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            var header = new FontTextView(parent.getContext());

            // When we don't have icons, it looks weird to have everything floating
            var matchIconPadding = 16;
            if (SettingsManager.getIconPack() == null) {
                matchIconPadding = 0;
            }

            header.setPadding(matchIconPadding, 16, matchIconPadding, 16);
            header.setTextSize(24f);
            header.setTypeface(header.getTypeface(), Typeface.BOLD);
            return new HeaderViewHolder(header);
        } else {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_layout, parent, false);
            return new AppViewHolder(view, mShortPressListener, mLongPressListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        var item = mItems.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).header.setText(item.getHeader());
        } else if (holder instanceof AppViewHolder) {
            var appInfo = item.getAppInfo();
            if (appInfo != null) {
                ((AppViewHolder) holder).bind(appInfo, mIconPackLoader);
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView header;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            header = (TextView) itemView;
        }
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView label;

        public AppViewHolder(@NonNull View itemView, Consumer<AppInfo> shortPressListener, Consumer<AppInfo> longPressListener) {
            super(itemView);
            icon = itemView.findViewById(R.id.appIcon);
            label = itemView.findViewById(R.id.appName);

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
}
