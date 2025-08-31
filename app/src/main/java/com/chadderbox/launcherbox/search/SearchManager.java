package com.chadderbox.launcherbox.search;

import java.util.ArrayList;
import java.util.List;

import com.chadderbox.launcherbox.data.ListItem;

/**
 * Responsible for obtaining the results for various search queries via a {@link ISearchProvider}.
 */
public final class SearchManager {

    private final List<ISearchProvider> mSearchProviders;

    public SearchManager(List<ISearchProvider> providers) {
        mSearchProviders = providers;
    }

    public List<ListItem> search(String query) {
        if (query == null || query.trim().isEmpty() || mSearchProviders == null) {
            return new ArrayList<>();
        }

        var results = new ArrayList<ListItem>();
        for (var provider : mSearchProviders) {
            results.addAll(provider.search(query));
        }

        return results;
    }
}