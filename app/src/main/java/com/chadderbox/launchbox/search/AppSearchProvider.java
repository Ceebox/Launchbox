package com.chadderbox.launchbox.search;

import android.os.Handler;
import android.os.Looper;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.CancellationToken;
import com.chadderbox.launchbox.utils.FavouritesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class AppSearchProvider implements ISearchProvider {

    private static final int LEVENSHTEIN_HEURISTIC = 3;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    private final List<SearchMetadata> mCache = new ArrayList<>();

    private final AppLoader mAppLoader;
    private final FavouritesRepository mFavouritesRepository;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AppSearchProvider() {
        mAppLoader = ServiceManager.getService(AppLoader.class);
        mFavouritesRepository = ServiceManager.getService(FavouritesRepository.class);
        refreshCache();
    }

    public void refreshCache() {
        mExecutor.execute(() -> {
            var apps = mAppLoader.getInstalledApps();
            var temp = new ArrayList<SearchMetadata>(apps.size());
            for (var app : apps) {
                temp.add(new SearchMetadata(app));
            }

            synchronized (mCache) {
                mCache.clear();
                mCache.addAll(temp);
            }
        });
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback, CancellationToken cancellationToken) {
        mExecutor.execute(() -> {
            var searchQuery = query.toLowerCase(Locale.getDefault());
            var favourites = mFavouritesRepository.loadFavourites();

            List<SearchMetadata> snapshot;
            synchronized (mCache) {
                snapshot = new ArrayList<>(mCache);
            }

            var favouriteItems = new ArrayList<ListItem>();
            var normal = new ArrayList<ListItem>();
            var fuzzy = new ArrayList<ListItem>();

            for (var meta : snapshot) {
                if (cancellationToken.isCancelled()) {
                    return;
                }

                if (meta.lowerLabel.contains(searchQuery) || meta.lowerPackage.contains(searchQuery)) {
                    if (favourites.contains(meta.app.getPackageName())) {
                        favouriteItems.add(new AppItem(meta.app));
                    } else {
                        normal.add(new AppItem(meta.app));
                    }
                } else if (searchQuery.length() > 3 &&
                    SearchHelpers.calculateLevenshteinDistance(searchQuery, meta.lowerLabel) < LEVENSHTEIN_HEURISTIC) {
                    fuzzy.add(new AppItem(meta.app));
                }
            }

            var results = new ArrayList<ListItem>(favouriteItems.size() + normal.size() + fuzzy.size());
            results.addAll(favouriteItems);
            results.addAll(normal);
            results.addAll(fuzzy);

            mMainHandler.post(() -> {
                if (!cancellationToken.isCancelled()) {
                    callback.accept(results);
                }
            });
        });
    }

    private static String formatPackageName(String packageName) {
        var lower = packageName.toLowerCase(Locale.getDefault());
        if (lower.startsWith("com.") || lower.startsWith("org.") || lower.startsWith("net.")) {
            return lower.substring(4);
        }

        return lower;
    }

    private static class SearchMetadata {
        final AppInfo app;
        final String lowerLabel;
        final String lowerPackage;

        SearchMetadata(AppInfo app) {
            this.app = app;
            this.lowerLabel = app.getLabel().toLowerCase(Locale.getDefault());
            this.lowerPackage = formatPackageName(app.getPackageName());
        }
    }
}