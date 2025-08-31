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
import com.chadderbox.launcherbox.data.SuggestionItem;
import com.chadderbox.launcherbox.data.WebItem;
import com.chadderbox.launcherbox.settings.SettingsManager;
import com.chadderbox.launcherbox.utils.FontHelper;
import com.chadderbox.launcherbox.utils.IconPackLoader;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CombinedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private static final int TYPE_WEB = 2;
    private static final int TYPE_SUGGESTION = 3;

    private final List<ListItem> mItems;
    private final Consumer<AppInfo> mAppShortPressListener;
    private final Consumer<AppInfo> mAppLongPressListener;
    private final Consumer<String> mWebShortPressListener;
    private final IconPackLoader mIconPackLoader;

    public CombinedAdapter(
            List<ListItem> items,
            Consumer<AppInfo> appShortPressListener,
            Consumer<AppInfo> appLongPressListener,
            Consumer<String> webShortPressListener,
            IconPackLoader iconPackLoader
    ) {
        mItems = items;
        mAppShortPressListener = appShortPressListener;
        mAppLongPressListener = appLongPressListener;
        mWebShortPressListener = webShortPressListener;
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
            if (SettingsManager.getIconPack() == null) {
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
        }

        // Idk
        return new HeaderViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        var item = mItems.get(position);

        if (holder instanceof HeaderViewHolder headerHolder) {
            if (item instanceof HeaderItem headerItem) {
                headerHolder.header.setText(headerItem.getHeader());
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
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView header;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            header = (TextView) itemView;
        }
    }

    public static class WebViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private String mQuery;

        public WebViewHolder(@NonNull View itemView, Consumer<String> clickListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_name);

            itemView.setOnClickListener(v -> {
                if (mQuery != null && clickListener != null) {
                    clickListener.accept(mQuery);
                }
            });
        }

        public void bind(String query) {
            mQuery = query;

            var context = itemView.getContext();
            ImageView icon = itemView.findViewById(R.id.item_icon);
            icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_browse));
            textView.setText(context.getString(R.string.search_the_web_for, query));
        }
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private String mSuggestion;

        public SuggestionViewHolder(@NonNull View itemView, Consumer<String> clickListener) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_name);

            itemView.setOnClickListener(v -> {
                if (mSuggestion != null && clickListener != null) {
                    clickListener.accept(mSuggestion);
                }
            });
        }

        public void bind(String suggestion) {
            mSuggestion = suggestion;

            var context = itemView.getContext();
            ImageView icon = itemView.findViewById(R.id.item_icon);
            icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_browse_suggestion));
            textView.setText(suggestion);
        }
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
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
}
