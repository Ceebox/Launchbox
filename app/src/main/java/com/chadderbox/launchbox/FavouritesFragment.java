package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FavouritesFragment extends AppListFragmentBase {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private FavouritesViewModel mViewModel;

    public FavouritesFragment() {
        super(null);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof IAdapterFetcher fetcher) {
            mAdapter = fetcher.getAdapter(FavouritesFragment.class);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        var root = inflater.inflate(R.layout.fragment_favourites, container, false);

        initialiseList(root.findViewById(R.id.recyclerview));

        mViewModel = new ViewModelProvider(
            requireActivity(),
            new FavouritesViewModel.Factory(
                requireActivity().getApplication(),
                new AppLoader(requireContext()),
                new FavouritesRepository(mExecutor, mMainHandler)
            )
        ).get(FavouritesViewModel.class);

        mViewModel.getItems().observe(getViewLifecycleOwner(), list -> {

            if (mAdapter == null) {
                return;
            }

            mAdapter.clearItems();
            mAdapter.addAll(list);
            mAdapter.notifyDataSetChanged();
        });

        return root;
    }

    @Override
    void refresh() {
        if (mViewModel != null) {
            mViewModel.loadFavourites();
        }
    }
}
