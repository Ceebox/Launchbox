package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chadderbox.launchbox.components.NowPlayingView;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FavouritesFragment extends AppListFragmentBase {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final AppLoader mAppLoader;
    private final FavouritesRepository mFavouritesHelper;

    public FavouritesFragment(
        CombinedAdapter favouritesAdapter,
        AppLoader appLoader,
        FavouritesRepository favouritesHelper
    ) {
        super(favouritesAdapter);
        mAppLoader = appLoader;
        mFavouritesHelper = favouritesHelper;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        var root = inflater.inflate(R.layout.fragment_favourites, container, false);

        var nowPlayingView = new NowPlayingView(root);
        nowPlayingView.initialize();

        initialiseList(root.findViewById(R.id.recyclerview));
        loadFavouritesAsync();

        return root;
    }

    @Override
    void refresh() {
        mAppLoader.refreshInstalledApps();
        loadFavouritesAsync();
    }

    private void loadFavouritesAsync() {
        mExecutor.execute(() -> mFavouritesHelper.loadFavouritesAsync(favourites -> {
            var apps = mAppLoader.getInstalledApps();

            var favApps = new ArrayList<AppInfo>();
            for (var app : apps) {
                if (favourites.contains(app.getPackageName())) {
                    favApps.add(app);
                }
            }

            favApps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

            var items = new ArrayList<ListItem>();
            if (!favApps.isEmpty()) {
                items.add(new HeaderItem("Favourites"));
                for (var app : favApps) {
                    items.add(new AppItem(app));
                }
            }

            mMainHandler.post(() -> {
                mAdapter.clearItems();
                mAdapter.addAll(items);
            });
        }));
    }
}
