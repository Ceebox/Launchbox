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

    private final int LEVENSHTEIN_HEURISTIC = 5;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final AppLoader mAppLoader;

    public AppSearchProvider(AppLoader appLoader) {
        mAppLoader = appLoader;
    }

    @Override
    public int getPriority() {
        // Apps have the highest priority
        return 0;
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        mExecutor.execute(() -> {
            var searchQuery = query.toLowerCase(Locale.getDefault());
            var results = new ArrayList<ListItem>();
            var fuzzyResults = new ArrayList<ListItem>();
            var apps = mAppLoader.getInstalledApps();
            for (var app : apps) {
                if (app.getLabel().toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    results.add(new AppItem(app));
                } else if (searchQuery.length() > 3 && SearchHelpers.calculateLevenshteinDistance(searchQuery, app.getLabel().toLowerCase(Locale.getDefault())) < LEVENSHTEIN_HEURISTIC) {
                    fuzzyResults.add(new AppItem(app));
                }
            }

            // We want the fuzzy results to show after actual search results
            results.addAll(fuzzyResults);

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(results));
        });
    }
}