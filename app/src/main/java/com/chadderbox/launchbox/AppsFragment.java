package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppsFragment extends AppListFragmentBase {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private AppsViewModel mViewModel;

    public AppsFragment() {
        super(null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        var root = inflater.inflate(R.layout.fragment_apps, container, false);

        mViewModel = new ViewModelProvider(requireActivity()).get(AppsViewModel.class);
        mViewModel.getAdapter().observe(getViewLifecycleOwner(), adapter -> {
            mAdapter = adapter;
            initialiseList(root.findViewById(R.id.recyclerview));
            loadAppsAsync();
        });

        return root;
    }

    @Override
    void refresh() {
        if (mViewModel != null) {
            mViewModel.getAppLoader().refreshInstalledApps();
            loadAppsAsync();
        }
    }

    private void loadAppsAsync() {
        mExecutor.execute(() -> {
            var items = buildAppsList();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (mAdapter != null) {
                        mAdapter.clearItems();
                        mAdapter.addAll(items);
                    }
                });
            }
        });
    }

    private List<ListItem> buildAppsList() {
        var apps = mViewModel.getAppLoader().getInstalledApps();
        apps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        var items = new ArrayList<ListItem>();
        items.add(new HeaderItem("Apps"));
        for (var app : apps) {
            items.add(new AppItem(app));
        }

        return items;
    }
}
