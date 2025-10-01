package com.chadderbox.launchbox.main.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.main.adapters.DragCallback;
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

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
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

        var recyclerView = (RecyclerView) root.findViewById(R.id.recyclerview);
        initialiseList(recyclerView);

        var dragCallback = new DragCallback(mAdapter, recyclerView);
        var touchHelper = new ItemTouchHelper(dragCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (dragCallback.isDragging()) {
                    // While dragging, keep parent (ViewPager) from intercepting
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) { }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (dragCallback.isDragging()) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

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
