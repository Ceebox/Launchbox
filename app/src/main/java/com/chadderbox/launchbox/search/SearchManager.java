package com.chadderbox.launchbox.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.CancellationToken;

/**
 * Responsible for obtaining the results for various search queries via a {@link ISearchProvider}.
 */
public final class SearchManager {

    private final List<ISearchProvider> mSearchProviders;
    private CancellationToken mCancellationToken;

    public SearchManager(List<ISearchProvider> providers) {
        mSearchProviders = new ArrayList<>(providers);
        mSearchProviders.sort(Comparator.comparingInt(ISearchProvider::getPriority));
    }

    public void searchAsync(String query, Consumer<List<ListItem>> callback) {
        if (query == null || query.trim().isEmpty() || mSearchProviders == null) {
            callback.accept(new ArrayList<>());
            return;
        }

        if (mCancellationToken != null && !mCancellationToken.isCancelled()) {
            mCancellationToken.cancel();
        }

        mCancellationToken = new CancellationToken();

        final var currentToken = mCancellationToken;
        var providerCount = mSearchProviders.size();
        var resultsBuffer = new ArrayList<List<ListItem>>(providerCount);
        for (var i = 0; i < providerCount; i++) {
            resultsBuffer.add(null);
        }

        var finished = new boolean[providerCount];
        var nextToPublish = new AtomicInteger(0);
        var aggregatedResults = new ArrayList<ListItem>();

        for (int i = 0; i < providerCount; i++) {
            final int index = i;
            var provider = mSearchProviders.get(index);
            provider.searchAsync(query, results -> {
                synchronized (resultsBuffer) {
                    if (currentToken.isCancelled()) {
                        // Don't process these results
                        return;
                    }

                    resultsBuffer.set(index, results != null ? results : new ArrayList<>());
                    finished[index] = true;

                    var updated = false;

                    // Flush results in order
                    while (nextToPublish.get() < providerCount && finished[nextToPublish.get()]) {
                        var currentIndex = nextToPublish.get();
                        List<ListItem> providerResults = resultsBuffer.get(currentIndex);

                        if (providerResults != null) {
                            aggregatedResults.addAll(providerResults);
                        }

                        nextToPublish.incrementAndGet();
                        updated = true;
                    }

                    if (updated) {
                        callback.accept(new ArrayList<>(aggregatedResults));
                    }
                }
            }, currentToken);
        }
    }
}