package com.chadderbox.launchbox.main.fragments;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.main.adapters.DragCallback;
import com.chadderbox.launchbox.main.adapters.IAdapterFetcher;
import com.chadderbox.launchbox.main.viewmodels.FavouritesViewModel;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.widgets.WidgetHostManager;
import com.chadderbox.launchbox.data.WidgetListItem;
import com.chadderbox.launchbox.widgets.data.WidgetDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public final class FavouritesFragment extends AppListFragmentBase {

    private FavouritesViewModel mViewModel;
    private DragCallback mDragCallback;
    private WidgetHostManager mWidgetManager;
    private List<ListItem> mCurrentApps;

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

        setupWidgetManager();

        mDragCallback = new DragCallback(mAdapter, recyclerView);
        var touchHelper = new ItemTouchHelper(mDragCallback);
        touchHelper.attachToRecyclerView(recyclerView);
        mAdapter.attachTouchHelper(touchHelper);

        setupTouchInterception(recyclerView);

        mViewModel = new ViewModelProvider(
            requireActivity(),
            new FavouritesViewModel.Factory(
                requireActivity().getApplication(),
                ServiceManager.getService(AppLoader.class),
                ServiceManager.getService(FavouritesRepository.class)
            )
        ).get(FavouritesViewModel.class);

        mViewModel.getItems().observe(getViewLifecycleOwner(), apps -> {
            mCurrentApps = apps;
            refreshCombinedList();
        });

        mViewModel.loadFavourites();
        refreshCombinedList();

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchInterception(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (mDragCallback.isDragging()) {
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                }

                return false;
            }

            @Override public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) { }
            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (mDragCallback.isDragging()) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mWidgetManager != null) {
            mWidgetManager.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWidgetManager != null) {
            mWidgetManager.stopListening();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void enterEditMode() {
        if (mViewModel.isEditMode()) {
            return;
        }

        mViewModel.enterEditMode();
        mDragCallback.setEditMode(true);
        mAdapter.notifyEditModeChanged(true);
//        mWidgetManager.setWidgetsResizing(true);
        mAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void exitEditMode() {
        if (!mViewModel.isEditMode()) {
            return;
        }

        mViewModel.exitEditMode();
        mDragCallback.setEditMode(false);
        mAdapter.notifyEditModeChanged(false);
//        mWidgetManager.setWidgetsResizing(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void refresh() {
        if (mViewModel != null) {
            refreshCombinedList();
        }
    }

    private void refreshCombinedList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            var activity = getActivity();
            if (activity == null) return;

            var widgetDao = WidgetDatabase.getDatabase(activity).widgetDao();
            var widgetItems = widgetDao.getAll().stream()
                .map(WidgetListItem::new)
                .toList();;

            activity.runOnUiThread(() -> {
                var fullList = new java.util.ArrayList<com.chadderbox.launchbox.data.ListItem>();

                // Widgets first, then other content
                fullList.addAll(widgetItems);
                fullList.addAll(mCurrentApps);

                // TODO: Use DiffUtil
                mAdapter.clearItems();
                mAdapter.addAll(fullList);
            });
        });
    }

    private void setupWidgetManager() {
        mWidgetManager = ServiceManager.getService(WidgetHostManager.class);
        var bindWidgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    var id = result.getData().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    var info = AppWidgetManager.getInstance(requireContext()).getAppWidgetInfo(id);
                    mWidgetManager.startWidgetConfiguration(info, id);
                }
            }
        );

        var configureWidgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> mWidgetManager.handleConfigureResult(result.getResultCode(), result.getData())
        );

        mWidgetManager.setConfigLauncher(configureWidgetLauncher);
        mWidgetManager.setBindLauncher(bindWidgetLauncher);

        var activity = getActivity();
        if (activity == null) {
            return;
        }

        mWidgetManager.getWidgets().observe(activity, widgets -> {
            this.refreshCombinedList();
        });
    }
}