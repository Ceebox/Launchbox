package com.chadderbox.launchbox.search;

import android.os.Handler;
import android.os.Looper;

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

    private final int LEVENSHTEIN_HEURISTIC = 3;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final AppLoader mAppLoader;
    private final FavouritesRepository mFavouritesRepository;

    public AppSearchProvider(AppLoader appLoader, FavouritesRepository favouritesRepository) {
        mAppLoader = appLoader;
        mFavouritesRepository = favouritesRepository;
    }

    @Override
    public int getPriority() {
        // Apps have the highest priority
        return 0;
    }

    @Override
    public void searchAsync(String query, Consumer<List<ListItem>> callback, CancellationToken cancellationToken) {
        mExecutor.execute(() -> {
            var searchQuery = query.toLowerCase(Locale.getDefault());

            var favourites = mFavouritesRepository.loadFavourites();
            var apps = mAppLoader.getInstalledApps();

            var results = new ArrayList<ListItem>();
            var favouriteResults = new ArrayList<ListItem>();
            var normalResults = new ArrayList<ListItem>();
            var fuzzyResults = new ArrayList<ListItem>();

            for (var app : apps) {

                if (cancellationToken.isCancelled()) {
                    return;
                }

                var labelForSearch = app.getLabel().toLowerCase(Locale.getDefault());
                var packageForSearch = formatPackageName(app.getPackageName());

                var matchesLabel = labelForSearch.contains(searchQuery);
                var matchesPackage = packageForSearch.contains(searchQuery);

                if (matchesLabel || matchesPackage) {
                    if (favourites.contains(app.getPackageName())) {
                        favouriteResults.add(new AppItem(app));
                    } else {
                        normalResults.add(new AppItem(app));
                    }
                } else if (searchQuery.length() > 3 &&
                    SearchHelpers.calculateLevenshteinDistance(searchQuery, app.getLabel().toLowerCase(Locale.getDefault())) < LEVENSHTEIN_HEURISTIC
                ) {
                    // TODO: Possibly do this comparison of after the last "." in the package name?
                    // NOTE: It may be faster to ignore the search query if it is longer than the longest package name + 2?
                    fuzzyResults.add(new AppItem(app));
                }
            }

            // Show in the order that the user probably wants them
            results.addAll(favouriteResults);
            results.addAll(normalResults);
            results.addAll(fuzzyResults);

            new Handler(Looper.getMainLooper()).post(() -> callback.accept(results));
        });
    }

    private static String formatPackageName(String packageName) {
        packageName = packageName.toLowerCase(Locale.getDefault());

        if (packageName.startsWith("com.")) {
            packageName = packageName.substring(4);
        }
        else if (packageName.startsWith("org.")) {
            packageName = packageName.substring(4);
        }
        else if (packageName.startsWith("net.")) {
            packageName = packageName.substring(4);
        }

        return packageName;
    }
}