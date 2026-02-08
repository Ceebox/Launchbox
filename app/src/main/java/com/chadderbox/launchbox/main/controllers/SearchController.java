package com.chadderbox.launchbox.main.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.icons.IconPackLoader;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.main.adapters.CombinedAdapter;
import com.chadderbox.launchbox.search.AppSearchProvider;
import com.chadderbox.launchbox.search.SearchManager;
import com.chadderbox.launchbox.search.SettingsSearchProvider;
import com.chadderbox.launchbox.search.WebSearchProvider;
import com.chadderbox.launchbox.search.WebSuggestionProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public final class SearchController {

    private static final long SEARCH_DELAY_MS = 150;

    private final BottomSheetBehavior<View> mSearchSheetBehaviour;
    private final CombinedAdapter mSearchAdapter;
    private final GestureDetector mGestureDetector;
    private final AppSearchProvider mAppSearchProvider;
    private SearchManager mSearchManager;
    private Runnable mSearchRunnable;

    @SuppressLint("ClickableViewAccessibility")
    public SearchController(View searchSheet) {

        mSearchSheetBehaviour = BottomSheetBehavior.from(searchSheet);
        mSearchSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

        var activity = ServiceManager.getActivity(MainActivity.class);
        mAppSearchProvider = new AppSearchProvider();
        var searchProviders = List.of(
            mAppSearchProvider,
            new WebSearchProvider(),
            new WebSuggestionProvider(),
            new SettingsSearchProvider(activity.getApplicationContext())
        );

        ServiceManager.registerService(SearchManager.class, () -> mSearchManager = new SearchManager(searchProviders));

        var iconPackLoader = ServiceManager.getService(IconPackLoader.class);
        mSearchAdapter = new CombinedAdapter(new ArrayList<>(), iconPackLoader);

        var searchResultsView = (RecyclerView) searchSheet.findViewById(R.id.search_results);
        searchResultsView.setLayoutManager(new LinearLayoutManager(activity));
        searchResultsView.setAdapter(mSearchAdapter);

        var searchInput = (EditText) searchSheet.findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                var handler = ServiceManager.getMainHandler();
                if (mSearchRunnable != null) {
                    handler.removeCallbacks(mSearchRunnable);
                }

                final var query = s.toString();
                mSearchRunnable = () -> performSearch(query);
                handler.postDelayed(mSearchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            var handled = false;

            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {

                if (mSearchAdapter.getItemCount() > 0) {
                    mSearchAdapter.getItem(0).performOpenAction(v);
                    closeSearchSheet();
                }

                handled = true;
            }

            return handled;
        });

        // Start hidden
        mSearchSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        mSearchSheetBehaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    var input = (EditText) activity.findViewById(R.id.search_input);
                    input.requestFocus();
                }

                // Prevent weird "peeking", it kinda stays open
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mSearchSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

                    // Stop the pesky keyboard staying open
                    var input = (EditText) activity.findViewById(R.id.search_input);
                    var imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        mGestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                var diffY = e1.getY() - e2.getY();
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    openSearchSheet();
                    return true;
                }
                return false;
            }
        });

        // Hacks to try and get swipe detection working
        var root = activity.findViewById(R.id.root_coordinator);
        root.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
    }

    public int getSheetState() {
        return mSearchSheetBehaviour.getState();
    }

    public void openSearchSheet() {
        mSearchSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void closeSearchSheet() {
        mSearchSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);

        var activity = ServiceManager.getActivity(MainActivity.class);
        var input = (EditText) activity.findViewById(R.id.search_input);
        input.clearFocus();
        input.setText("");
        var imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            mSearchAdapter.clearItems();
            mSearchAdapter.notifyDataSetChanged();
            return;
        }

        mSearchManager.searchAsync(query, results -> {
            if (!results.isEmpty()) {
                mSearchAdapter.updateItems(results);
            } else {
                mSearchAdapter.add(new HeaderItem("No results"));
            }
        });
    }

    public void notifyAppsChanged() {
        mAppSearchProvider.refreshCache();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }
}
