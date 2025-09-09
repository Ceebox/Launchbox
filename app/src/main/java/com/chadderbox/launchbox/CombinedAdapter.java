package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.components.FontTextView;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.data.SuggestionItem;
import com.chadderbox.launchbox.data.WebItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.IconPackLoader;
import com.chadderbox.launchbox.viewholders.AppViewHolder;
import com.chadderbox.launchbox.viewholders.HeaderViewHolder;
import com.chadderbox.launchbox.viewholders.SettingViewHolder;
import com.chadderbox.launchbox.viewholders.SuggestionViewHolder;
import com.chadderbox.launchbox.viewholders.WebViewHolder;

import java.util.Collection;
import java.util.List;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_WEB = 2;
    private static final int TYPE_SUGGESTION = 3;
    private static final int TYPE_SETTING = 4;

    private final List<ListItem> mItems;
    private final IconPackLoader mIconPackLoader;

    public CombinedAdapter(
            List<ListItem> items,
            IconPackLoader iconPackLoader
    ) {
        mItems = items;
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

    public boolean isEmpty() {
        for (var item : mItems) {
            if (!(item instanceof HeaderItem)) {
                return false;
            }
        }

        return true;
    }

    public List<ListItem> getItems() {
        return mItems;
    }

    public ListItem getItem(int index) {
        return mItems.get(index);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        mItems.clear();
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(ListItem item) {
        mItems.add(item);
        this.notifyDataSetChanged();
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
            return new AppViewHolder(view);
        } else if (viewType == TYPE_WEB) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new WebViewHolder(view);
        } else if (viewType == TYPE_SUGGESTION) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new SuggestionViewHolder(view);
        } else if (viewType == TYPE_SETTING) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new SettingViewHolder(view);
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
                appHolder.bind(appItem, mIconPackLoader);
            }
        }
        else if (holder instanceof WebViewHolder webHolder) {
            if (item instanceof WebItem webItem) {
                webHolder.bind(webItem);
            }
        }
        else if (holder instanceof SuggestionViewHolder suggestionHolder) {
            if (item instanceof SuggestionItem suggestionItem) {
                suggestionHolder.bind(suggestionItem);
            }
        }
        else if (holder instanceof SettingViewHolder settingHolder) {
            if (item instanceof SettingItem settingItem) {
                settingHolder.bind(settingItem);
            }
        }
    }
}
