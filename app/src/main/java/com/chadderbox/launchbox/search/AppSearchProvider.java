package com.chadderbox.launchbox.search;

import android.os.Handler;
import android.os.Looper;

import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.AppLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class AppSearchProvider implements ISearchProvider {
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final AppLoader mAppLoader;

    public AppSearchProvider(AppLoader appLoader) {
        mAppLoader = appLoader;
    }

    public void refreshApps() {
        // Don't worry too much about this, it gets called in lots of other places
        mAppLoader.refreshInstalledApps();
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        mExecutor.execute(() -> {
            var searchQuery = query.toLowerCase(Locale.getDefault());
            var results = new ArrayList<ListItem>();
            var apps = mAppLoader.getInstalledApps();
            for (var app : apps) {
                if (app.getLabel().toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    results.add(new AppItem(app));
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(results));
        });
    }
}