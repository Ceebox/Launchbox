package com.chadderbox.launchbox.main.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.main.adapters.IAdapterFetcher;
import com.chadderbox.launchbox.main.viewmodels.AppsViewModel;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.core.ServiceManager;

public final class AppsFragment
    extends AppListFragmentBase {

    private AppsViewModel mViewModel;

    public AppsFragment() {
        super(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        var root = inflater.inflate(R.layout.fragment_apps, container, false);

        if (getActivity() instanceof IAdapterFetcher fetcher) {
            mAdapter = fetcher.getAdapter(AppsFragment.class);
        }

        initialiseList(root.findViewById(R.id.recyclerview));

        mViewModel = new ViewModelProvider(
            requireActivity(),
            new AppsViewModel.Factory(
                requireActivity().getApplication(),
                ServiceManager.resolve(AppLoader.class)
            )
        ).get(AppsViewModel.class);

        mViewModel.getItems().observe(getViewLifecycleOwner(), list -> {
            mAdapter.clearItems();
            mAdapter.addAll(list);
            mAdapter.notifyDataSetChanged();
        });

        return root;
    }

    @Override
    public void refresh() {
        if (mViewModel != null) {
            mViewModel.getAppLoader().refreshInstalledApps();
            mViewModel.loadApps();
        }
    }
}
