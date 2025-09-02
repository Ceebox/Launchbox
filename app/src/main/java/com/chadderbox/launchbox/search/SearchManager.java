package com.chadderbox.launchbox.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.chadderbox.launchbox.data.ListItem;

/**
 * Responsible for obtaining the results for various search queries via a {@link ISearchProvider}.
 */
public final class SearchManager {

    private final List<ISearchProvider> mSearchProviders;

    public SearchManager(List<ISearchProvider> providers) {
        mSearchProviders = providers;
    }

    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        if (query == null || query.trim().isEmpty() || mSearchProviders == null) {
            callback.accept(new ArrayList<>());
            return;
        }

        var aggregated = new ArrayList<ListItem>();
        var pending = new AtomicInteger(mSearchProviders.size());

        // TODO: Probably isolate by type (app vs search result etc)
        for (var provider : mSearchProviders) {
            provider.searchAsync(query, results -> {
                aggregated.addAll(results);
                if (pending.decrementAndGet() == 0) {
                    callback.accept(aggregated);
                }
            });
        }
    }
}