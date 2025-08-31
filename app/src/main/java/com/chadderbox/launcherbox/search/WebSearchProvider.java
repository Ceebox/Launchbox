package com.chadderbox.launcherbox.search;

import android.os.Handler;
import android.os.Looper;

import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.data.WebItem;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Provides web search results + suggestions.
 */
public final class WebSearchProvider implements ISearchProvider {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        mExecutor.execute(() -> {
            var result = new ArrayList<ListItem>();
            result.add(new WebItem(query));
            new Handler(Looper.getMainLooper()).post(() -> callback.accept(result));
        });
    }
}

