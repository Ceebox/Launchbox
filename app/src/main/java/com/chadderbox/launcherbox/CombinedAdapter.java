package com.chadderbox.launcherbox;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.components.FontTextView;
import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.AppItem;
import com.chadderbox.launcherbox.data.HeaderItem;
import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.data.SettingItem;
import com.chadderbox.launcherbox.data.SuggestionItem;
import com.chadderbox.launcherbox.data.WebItem;
import com.chadderbox.launcherbox.settings.SettingsManager;
import com.chadderbox.launcherbox.utils.FontHelper;
import com.chadderbox.launcherbox.utils.IconPackLoader;
import com.chadderbox.launcherbox.viewholders.AppViewHolder;
import com.chadderbox.launcherbox.viewholders.HeaderViewHolder;
import com.chadderbox.launcherbox.viewholders.SettingViewHolder;
import com.chadderbox.launcherbox.viewholders.SuggestionViewHolder;
import com.chadderbox.launcherbox.viewholders.WebViewHolder;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_WEB = 2;
    private static final int TYPE_SUGGESTION = 3;
    private static final int TYPE_SETTING = 4;

    private final List<ListItem> mItems;
    private final Consumer<AppInfo> mAppShortPressListener;
    private final Consumer<AppInfo> mAppLongPressListener;
    private final Consumer<String> mWebShortPressListener;
    private final Consumer<SettingItem> mSettingsShortPressListener;
    private final IconPackLoader mIconPackLoader;

    public CombinedAdapter(
            List<ListItem> items,
            Consumer<AppInfo> appShortPressListener,
            Consumer<AppInfo> appLongPressListener,
            Consumer<String> webShortPressListener,
            Consumer<SettingItem> settingsShortPressListener,
            IconPackLoader iconPackLoader
    ) {
        mItems = items;
        mAppShortPressListener = appShortPressListener;
        mAppLongPressListener = appLongPressListener;
        mWebShortPressListener = webShortPressListener;
        mSettingsShortPressListener = settingsShortPressListener;
        mIconPackLoader = iconPackLoader;
    }

    @Override
    public int getItemViewType(int position) {
        var item = mItems.get(position);
        return switch (item.getType()) {
            case HEADER -> TYPE_HEADER;
            case APP -> TYPE_APP;
            case WEB -> TYPE_WEB;
            case SUGGESTION -> TYPE_SUGGESTION;
            case SETTING -> TYPE_SETTING;
        };
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
            if (SettingsManager.getIconPack().equals("None")) {
                matchIconPadding = 0;
            }

            header.setPadding(matchIconPadding, 16, matchIconPadding, 16);
            header.setTextSize(24f);
            header.setTypeface(header.getTypeface(), Typeface.BOLD);
            return new HeaderViewHolder(header);
        } else if (viewType == TYPE_APP) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new AppViewHolder(view, mAppShortPressListener, mAppLongPressListener);
        } else if (viewType == TYPE_WEB) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new WebViewHolder(view, mWebShortPressListener);
        } else if (viewType == TYPE_SUGGESTION) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new SuggestionViewHolder(view, mWebShortPressListener);
        } else if (viewType == TYPE_SETTING) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new SettingViewHolder(view, mSettingsShortPressListener);
        }

        // Idk
        return new HeaderViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        var item = mItems.get(position);

        if (holder instanceof HeaderViewHolder headerHolder) {
            if (item instanceof HeaderItem headerItem) {
                headerHolder.bind(headerItem.getHeader());
            }
        }
        else if (holder instanceof AppViewHolder appHolder) {
            if (item instanceof AppItem appItem) {
                appHolder.bind(appItem.getAppInfo(), mIconPackLoader);
            }
        }
        else if (holder instanceof WebViewHolder webHolder) {
            if (item instanceof WebItem webItem) {
                webHolder.bind(webItem.getQuery());
            }
        }
        else if (holder instanceof SuggestionViewHolder suggestionHolder) {
            if (item instanceof SuggestionItem suggestionItem) {
                suggestionHolder.bind(suggestionItem.getSuggestion());
            }
        }
        else if (holder instanceof SettingViewHolder settingHolder) {
            if (item instanceof SettingItem settingItem) {
                settingHolder.bind(settingItem);
            }
        }
    }
}
