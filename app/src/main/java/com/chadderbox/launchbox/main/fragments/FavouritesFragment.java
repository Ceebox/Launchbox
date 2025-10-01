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
import com.chadderbox.launchbox.main.viewmodels.FavouritesViewModel;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.core.ServiceManager;

public final class FavouritesFragment
    extends AppListFragmentBase {

    private FavouritesViewModel mViewModel;

    public FavouritesFragment() {
        super(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        var root = inflater.inflate(R.layout.fragment_favourites, container, false);

        if (getActivity() instanceof IAdapterFetcher fetcher) {
            mAdapter = fetcher.getAdapter(FavouritesFragment.class);
        }

        initialiseList(root.findViewById(R.id.recyclerview));

        mViewModel = new ViewModelProvider(
            requireActivity(),
            new FavouritesViewModel.Factory(
                requireActivity().getApplication(),
                ServiceManager.resolve(AppLoader.class),
                ServiceManager.resolve(FavouritesRepository.class)
            )
        ).get(FavouritesViewModel.class);

        mViewModel.getItems().observe(getViewLifecycleOwner(), list -> {
            mAdapter.clearItems();
            mAdapter.addAll(list);
            mAdapter.notifyDataSetChanged();
        });

        mViewModel.loadFavourites();

        return root;
    }

    @Override
    public void refresh() {
        if (mViewModel != null) {
            mViewModel.loadFavourites();
        }
    }
}
