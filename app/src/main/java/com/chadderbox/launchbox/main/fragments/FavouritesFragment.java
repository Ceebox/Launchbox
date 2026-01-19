package com.chadderbox.launchbox.main.fragments;

import android.annotation.SuppressLint;
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
import com.chadderbox.launchbox.main.adapters.DragCallback;
import com.chadderbox.launchbox.main.adapters.IAdapterFetcher;
import com.chadderbox.launchbox.main.viewmodels.FavouritesViewModel;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public final class FavouritesFragment
    extends AppListFragmentBase {

    private FavouritesViewModel mViewModel;
    private DragCallback mDragCallback;
    private WidgetHostManager mWidgetManager;

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
        var widgetContainer = (ViewGroup) root.findViewById(R.id.widget_container);

        setupWidgetManager(widgetContainer);
        if (getActivity() instanceof IAdapterFetcher fetcher) {
            mAdapter = fetcher.getAdapter(FavouritesFragment.class);
        }

        var recyclerView = (RecyclerView) root.findViewById(R.id.recyclerview);
        initialiseList(recyclerView);

        mDragCallback = new DragCallback(mAdapter, recyclerView);
        var touchHelper = new ItemTouchHelper(mDragCallback);
        touchHelper.attachToRecyclerView(recyclerView);
        mAdapter.attachTouchHelper(touchHelper);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (mDragCallback.isDragging()) {
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
                    if (mDragCallback.isDragging()) {
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
                ServiceManager.getService(AppLoader.class),
                ServiceManager.getService(FavouritesRepository.class)
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
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void refresh() {
        if (mViewModel != null) {
            mViewModel.loadFavourites();
        }
    }

    private void setupWidgetManager(ViewGroup widgetContainer) {
        // TODO: Probably move this setup code elsewhere?
        mWidgetManager = ServiceManager.initialiseService(WidgetHostManager.class, () -> new WidgetHostManager(requireActivity(), widgetContainer));
        mWidgetManager.loadSavedWidgets();

        var bindWidgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    var id = result.getData().getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    var info = android.appwidget.AppWidgetManager.getInstance(requireContext()).getAppWidgetInfo(id);
                    mWidgetManager.startWidgetConfiguration(info, id);
                }
            }
        );

        var configureWidgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mWidgetManager.handleConfigureResult(
                    result.getResultCode(),
                    result.getData()
                );
            }
        );

        mWidgetManager.setConfigLauncher(configureWidgetLauncher);
        mWidgetManager.setBindLauncher(bindWidgetLauncher);
    }
}
