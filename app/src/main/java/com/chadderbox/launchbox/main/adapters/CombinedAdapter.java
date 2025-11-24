package com.chadderbox.launchbox.main.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.data.SuggestionItem;
import com.chadderbox.launchbox.data.WebItem;
import com.chadderbox.launchbox.icons.IconPackLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.viewholders.AppViewHolder;
import com.chadderbox.launchbox.viewholders.HeaderViewHolder;
import com.chadderbox.launchbox.viewholders.SettingViewHolder;
import com.chadderbox.launchbox.viewholders.SuggestionViewHolder;
import com.chadderbox.launchbox.viewholders.WebViewHolder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_WEB = 2;
    private static final int TYPE_SUGGESTION = 3;
    private static final int TYPE_SETTING = 4;

    private final List<ListItem> mItems;
    private final IconPackLoader mIconPackLoader;
    private boolean mActionsEnabled = true;
    private boolean mIsEditMode = false;
    private ItemTouchHelper mTouchHelper;

    public CombinedAdapter(
            List<ListItem> items,
            IconPackLoader iconPackLoader
    ) {
        mItems = items;
        mIconPackLoader = iconPackLoader;
    }

    public boolean getActionsEnabled() {
        return mActionsEnabled;
    }

    public void setActionsEnabled(boolean enabled) {
        if (mActionsEnabled == enabled) {
            return;
        }

        mActionsEnabled = enabled;
        for (var item : mItems) {
            item.setActionsEnabled(mActionsEnabled);
        }
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

    public void moveItem(final int fromPosition, final int toPosition) {
        if (fromPosition < 0 || toPosition < 0 ||
            fromPosition >= mItems.size() || toPosition >= mItems.size()) {
            return;
        }

        var item = mItems.remove(fromPosition);
        mItems.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void saveFavouritesOrder() {
        var favourites = ServiceManager.getService(FavouritesRepository.class);
        var packageNames = mItems.stream()
            .filter(i -> i instanceof AppItem)
            .map(i -> ((AppItem) i).getAppInfo().getPackageName())
            .toList();

        favourites.saveFavourites(packageNames);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        mItems.clear();
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void add(ListItem item) {
        mItems.add(item);
        item.setActionsEnabled(mActionsEnabled);
        this.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAll(Collection<? extends ListItem> items) {
        mItems.addAll(items);
        for (var item : items) {
            item.setActionsEnabled(mActionsEnabled);
        }

        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            return new HeaderViewHolder(view);
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
                appHolder.bind(appItem, mIconPackLoader, mTouchHelper, mIsEditMode);
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

    public void attachTouchHelper(ItemTouchHelper helper) {
        mTouchHelper = helper;
    }

    public void notifyEditModeChanged(boolean isEditMode) {
        mIsEditMode = isEditMode;
    }
}
