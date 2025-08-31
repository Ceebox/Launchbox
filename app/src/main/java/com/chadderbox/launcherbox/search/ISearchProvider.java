package com.chadderbox.launcherbox.search;

import com.chadderbox.launcherbox.data.ListItem;

import java.util.List;

public interface ISearchProvider {
    List<ListItem> search(String query);
}
