package com.chadderbox.launcherbox.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.ListItem;

public final class SearchManager {

    private List<AppInfo> mAllApps;

    public SearchManager(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    public void setAllApps(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    public List<ListItem> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        query = query.toLowerCase(Locale.getDefault());
        var results = new ArrayList<ListItem>();

        for (var app : mAllApps) {
            if (app.getLabel().toLowerCase(Locale.getDefault()).contains(query)) {
                results.add(new ListItem(app));
            }
        }

        return results;
    }
}