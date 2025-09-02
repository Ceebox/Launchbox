package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.AppLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppsFragment extends AppListFragmentBase {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final AppLoader mAppLoader;

    public AppsFragment(
        CombinedAdapter appsAdapter,
        AppLoader appLoader
    ) {
        super(appsAdapter);
        mAppLoader = appLoader;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        var root = inflater.inflate(R.layout.fragment_apps, container, false);

        initialiseList(root.findViewById(R.id.recyclerview));
        loadAppsAsync();

        return root;
    }

    @Override
    void refresh() {
        mAppLoader.refreshInstalledApps();
        loadAppsAsync();
    }

    private void loadAppsAsync() {
        mExecutor.execute(() -> {
            var items = buildAppsList();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mAdapter.clearItems();
                    mAdapter.addAll(items);
                });
            }
        });
    }

    private List<ListItem> buildAppsList() {
        var apps = mAppLoader.getInstalledApps();
        apps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        var items = new ArrayList<ListItem>();
        items.add(new HeaderItem("Apps"));
        for (var app : apps) {
            items.add(new AppItem(app));
        }

        return items;
    }
}
