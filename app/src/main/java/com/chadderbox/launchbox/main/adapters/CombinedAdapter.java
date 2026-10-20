package com.chadderbox.launchbox.main.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ItemViewType;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.data.SuggestionItem;
import com.chadderbox.launchbox.data.WebItem;
import com.chadderbox.launchbox.data.WidgetListItem;
import com.chadderbox.launchbox.ui.components.ResizableWidgetFrame;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.viewholders.AppViewHolder;
import com.chadderbox.launchbox.viewholders.HeaderViewHolder;
import com.chadderbox.launchbox.viewholders.SettingViewHolder;
import com.chadderbox.launchbox.viewholders.SuggestionViewHolder;
import com.chadderbox.launchbox.viewholders.WebViewHolder;
import com.chadderbox.launchbox.viewholders.WidgetViewHolder;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final AsyncListDiffer<ListItem> mDiffer;
    private final WidgetHostManager mWidgetHostManager;
    private ItemTouchHelper mTouchHelper;
    private boolean mActionsEnabled = true;
    private boolean mIsEditing = false;
    private int mDefaultItemHeight = 0;

    public CombinedAdapter(
        List<ListItem> items,
        WidgetHostManager widgetManager
    ) {
        mDiffer = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                return oldItem.getType() == newItem.getType()
                    && oldItem.getIdentity().equals(newItem.getIdentity());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                return true;
            }
        });
        mWidgetHostManager = widgetManager;
        mDiffer.submitList(new ArrayList<>(items));
    }

    private static View inflateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_layout, parent, false);
    }

    public boolean getActionsEnabled() {
        return mActionsEnabled;
    }

    public void setActionsEnabled(boolean enabled) {
        if (mActionsEnabled == enabled) {
            return;
        }

        mActionsEnabled = enabled;
        for (var item : mDiffer.getCurrentList()) {
            item.setActionsEnabled(mActionsEnabled);
        }
    }

    public void attachTouchHelper(ItemTouchHelper helper) {
        mTouchHelper = helper;
    }

    public ItemTouchHelper getTouchHelper() {
        return mTouchHelper;
    }

    public boolean isEditing() {
        // For future clarification (because this is confusing me)
        // This is for editing the order of items inside the adapter
        return mIsEditing;
    }

    public void setIsEditing(boolean isEditing) {
        mIsEditing = isEditing;
    }

    @Override
    public int getItemViewType(int position) {
        var item = mDiffer.getCurrentList().get(position);
        return switch (item.getType()) {
            case HEADER -> ItemViewType.HEADER.getId();
            case APP -> ItemViewType.APP.getId();
            case WEB -> ItemViewType.WEB.getId();
            case SUGGESTION -> ItemViewType.SUGGESTION.getId();
            case SETTING -> ItemViewType.SETTING.getId();
            case WIDGET -> ItemViewType.WIDGET.getId();
        };
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public boolean isEmpty() {
        for (var item : mDiffer.getCurrentList()) {
            if (!(item instanceof HeaderItem)) {
                return false;
            }
        }

        return true;
    }

    public List<ListItem> getItems() {
        return mDiffer.getCurrentList();
    }

    public ListItem getItem(int index) {
        return mDiffer.getCurrentList().get(index);
    }

    public void moveItem(final int fromPosition, final int toPosition) {
        var updatedItems = new ArrayList<>(mDiffer.getCurrentList());
        if (fromPosition < 0 || toPosition < 0 ||
            fromPosition >= updatedItems.size() || toPosition >= updatedItems.size()) {
            return;
        }

        var item = updatedItems.remove(fromPosition);
        updatedItems.add(toPosition, item);
        mDiffer.submitList(updatedItems);
    }

    public void saveFavouritesOrder() {
        var favourites = ServiceManager.getService(FavouritesRepository.class);
        var packageNames = mDiffer.getCurrentList().stream()
            .filter(i -> i instanceof AppItem)
            .map(i -> ((AppItem) i).getAppInfo().getPackageName())
            .toList();

        favourites.saveFavourites(packageNames);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        mDiffer.submitList(new ArrayList<>());
    }

    public void add(ListItem item) {
        var updatedItems = new ArrayList<>(mDiffer.getCurrentList());
        updatedItems.add(item);
        item.setActionsEnabled(mActionsEnabled);
        mDiffer.submitList(updatedItems);
    }

    public void addAll(Collection<? extends ListItem> items) {
        var updatedItems = new ArrayList<>(mDiffer.getCurrentList());
        updatedItems.addAll(items);
        for (var item : items) {
            item.setActionsEnabled(mActionsEnabled);
        }

        mDiffer.submitList(updatedItems);
    }

    public void prependAll(Collection<? extends  ListItem> items) {
        var updatedItems = new ArrayList<>(mDiffer.getCurrentList());
        updatedItems.addAll(0, items);
        for (var item : items) {
            item.setActionsEnabled(mActionsEnabled);
        }

        mDiffer.submitList(updatedItems);
    }

    public void updateItems(List<ListItem> newList) {
        mDiffer.submitList(new ArrayList<>(newList));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mDefaultItemHeight == 0) {
            var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mDefaultItemHeight = view.getMeasuredHeight();
        }

        var type = ItemViewType.fromId(viewType);

        if (type == ItemViewType.WIDGET) {
            var frame = new ResizableWidgetFrame(parent.getContext());
            frame.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new WidgetViewHolder(this, frame, mWidgetHostManager);
        }

        var view = inflateView(parent);
        return switch (type) {
            case HEADER -> new HeaderViewHolder(this, view);
            case APP -> new AppViewHolder(this, view);
            case WEB -> new WebViewHolder(this, view);
            case SUGGESTION -> new SuggestionViewHolder(this, view);
            case SETTING -> new SettingViewHolder(this, view);
            case WIDGET -> throw new AssertionError("Widget handled above");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (mDiffer.getCurrentList().get(position)) {
            case HeaderItem item -> ((HeaderViewHolder) holder).bind(item);
            case AppItem item -> ((AppViewHolder) holder).bind(item);
            case WebItem item -> ((WebViewHolder) holder).bind(item);
            case SuggestionItem item -> ((SuggestionViewHolder) holder).bind(item);
            case SettingItem item -> ((SettingViewHolder) holder).bind(item);
            case WidgetListItem item -> ((WidgetViewHolder) holder).bind(item);
            default -> throw new IllegalStateException("Unexpected value: " + mDiffer.getCurrentList().get(position));
        }
    }
}

