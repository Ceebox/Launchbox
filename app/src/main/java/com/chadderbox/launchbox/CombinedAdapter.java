package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.compose.ui.platform.ViewCompositionStrategy;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.components.FontTextView;
import com.chadderbox.launchbox.components.ListItemLayoutKt;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.data.SuggestionItem;
import com.chadderbox.launchbox.data.WebItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.IconPackLoader;
import com.chadderbox.launchbox.viewholders.ComposeRowViewHolder;
import com.chadderbox.launchbox.viewholders.HeaderViewHolder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_WEB = 2;
    private static final int TYPE_SUGGESTION = 3;
    private static final int TYPE_SETTING = 4;

    private static final float HEADER_SIZE_MULTIPLIER = 1.5f;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

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
            header.setIsHeading(true);
            header.setTag(R.id.isHeading, true);

            var matchIconPadding = SettingsManager.getIconPack().equals("None") ? 0 : 16;
            header.setPadding(matchIconPadding, 16, matchIconPadding, 16);

            return new HeaderViewHolder(header);
        }

        var itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_layout, parent, false);

        ListItemLayoutKt.initListItemComposition(itemView.findViewById(R.id.compose_item));

        return new ComposeRowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        var item = mItems.get(position);
        mExecutor.execute(() -> {
            if (holder instanceof ComposeRowViewHolder composeHolder) {
                var composeView = composeHolder.getComposeView();
                switch (item.getType()) {
                    case APP -> {
                        if (item instanceof AppItem appItem) {
                            mMainHandler.post(() -> ListItemLayoutKt.setAppRow(composeView, appItem, mIconPackLoader));
                        }
                    }
                    case WEB -> {
                        if (item instanceof WebItem webItem) {
                            mMainHandler.post(() -> ListItemLayoutKt.setWebRow(composeView, webItem));
                        }
                    }
                    case SUGGESTION -> {
                        if (item instanceof SuggestionItem suggestionItem) {
                            mMainHandler.post(() -> ListItemLayoutKt.setSuggestionRow(composeView, suggestionItem));
                        }
                    }
                    case SETTING -> {
                        if (item instanceof SettingItem settingItem) {
                            mMainHandler.post(() -> ListItemLayoutKt.setSettingRow(composeView, settingItem));
                        }
                    }
                }
            }
            else if (holder instanceof HeaderViewHolder headerHolder) {
                if (item instanceof HeaderItem headerItem) {
                    mMainHandler.post(() -> headerHolder.bind(headerItem.getHeader()));
                }
            }
        });
    }
}
