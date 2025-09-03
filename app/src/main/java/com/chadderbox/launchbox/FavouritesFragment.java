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
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FavouritesFragment extends AppListFragmentBase {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private FavouritesViewModel mViewModel;

    public FavouritesFragment() {
        super(null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(FavouritesViewModel.class);
        mViewModel.getAdapter().observe(getViewLifecycleOwner(), adapter -> {
            mAdapter = adapter;
            initialiseList(root.findViewById(R.id.recyclerview));
            loadFavouritesAsync();
        });

        return root;
    }

    @Override
    void refresh() {
        if (mViewModel != null) {
            mViewModel.getAppLoader().refreshInstalledApps();
            loadFavouritesAsync();
        }
    }

    private void loadFavouritesAsync() {
        mExecutor.execute(() ->
            mViewModel.getFavouritesRepository().loadFavouritesAsync(favourites -> {
                var apps = mViewModel.getAppLoader().getInstalledApps();

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
                    if (mAdapter != null) {
                        mAdapter.clearItems();
                        mAdapter.addAll(items);
                    }
                });
            })
        );
    }
}
