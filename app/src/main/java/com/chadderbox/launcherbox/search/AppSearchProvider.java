package com.chadderbox.launcherbox.search;

import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.ListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppSearchProvider implements ISearchProvider {

    private List<AppInfo> mAllApps;

    public AppSearchProvider(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    public void setAllApps(List<AppInfo> allApps) {
        mAllApps = allApps;
    }

    @Override
    public List<ListItem> search(String query) {
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
