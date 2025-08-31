package com.chadderbox.launcherbox.search;

import android.os.Handler;
import android.os.Looper;

import com.chadderbox.launcherbox.data.AppItem;
import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.data.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AppSearchProvider implements ISearchProvider {

    private List<AppInfo> mAllApps;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public AppSearchProvider(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    public void setAllApps(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        mExecutor.execute(() -> {
            var searchQuery = query.toLowerCase(Locale.getDefault());
            var results = new ArrayList<ListItem>();
            for (var app : mAllApps) {
                if (app.getLabel().toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    results.add(new AppItem(app));
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(results));
        });
    }
}