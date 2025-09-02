package com.chadderbox.launchbox.search;

import com.chadderbox.launchbox.data.ListItem;

import java.util.List;
import java.util.function.Consumer;

public interface ISearchProvider {
    void searchAsync(String query, Consumer<List<ListItem>> callback);
}
