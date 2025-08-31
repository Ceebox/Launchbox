package com.chadderbox.launcherbox.search;

import com.chadderbox.launcherbox.data.ListItem;

import java.util.List;
import java.util.function.Consumer;

public interface ISearchProvider {
    void searchAsync(String query, Consumer<List<ListItem>> callback);
}
