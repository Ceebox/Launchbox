package com.chadderbox.launchbox.search;

import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.CancellationToken;

import java.util.List;
import java.util.function.Consumer;

public interface ISearchProvider {
    int getPriority();
    void searchAsync(
        String query,
        Consumer<List<ListItem>> callback,
        CancellationToken cancellationToken
    );
}
