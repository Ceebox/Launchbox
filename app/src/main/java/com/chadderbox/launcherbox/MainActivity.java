package com.chadderbox.launcherbox;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launcherbox.components.AlphabetIndexView;
import com.chadderbox.launcherbox.components.NowPlayingView;
import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.AppItem;
import com.chadderbox.launcherbox.data.HeaderItem;
import com.chadderbox.launcherbox.data.ListItem;
import com.chadderbox.launcherbox.search.AppSearchProvider;
import com.chadderbox.launcherbox.search.ISearchProvider;
import com.chadderbox.launcherbox.search.SearchManager;
import com.chadderbox.launcherbox.search.WebSearchProvider;
import com.chadderbox.launcherbox.search.WebSuggestionProvider;
import com.chadderbox.launcherbox.settings.SettingsActivity;
import com.chadderbox.launcherbox.settings.SettingsManager;
import com.chadderbox.launcherbox.utils.AppLoader;
import com.chadderbox.launcherbox.utils.FavouritesRepository;
import com.chadderbox.launcherbox.utils.IconPackLoader;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    private static final long SEARCH_DELAY_MS = 300;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private SearchManager mSearchManager;
    private Runnable mSearchRunnable;
    private AppSearchProvider mAppSearchProvider;
    private RecyclerView mAppsView;
    private AlphabetIndexView mIndexView;
    private CombinedAdapter mAppsAdapter;
    private GestureDetector mGestureDetector;
    private NowPlayingView mNowPlayingView;
    private FavouritesRepository mFavouritesHelper;
    private String mLastIconPack;
    private String mLastFont;
    private IconPackLoader mIconPackLoader;
    private BottomSheetBehavior<View> mSearchSheet;
    private RecyclerView mSearchResultsView;
    private CombinedAdapter mSearchAdapter;
    private EditText mSearchInput;
    private AppLoader mAppLoader;

    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            var action = intent.getAction();
            if (action == null) {
                return;
            }

            // If our apps have changed, we should probably show that!
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                case Intent.ACTION_PACKAGE_REMOVED:
                case Intent.ACTION_PACKAGE_CHANGED:
                    loadAppsAsync();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsManager.initialiseSettingsManager(getApplicationContext());

        mLastIconPack = SettingsManager.getIconPack();
        mLastFont = SettingsManager.getFont();

        mIconPackLoader = new IconPackLoader(getApplicationContext(), mLastIconPack);
        mFavouritesHelper = new FavouritesRepository(mExecutor, mMainHandler);

        var searchProviders = new ArrayList<ISearchProvider>();
        mAppSearchProvider = new AppSearchProvider(new ArrayList<>());
        searchProviders.add(mAppSearchProvider);
        searchProviders.add(new WebSearchProvider());
        searchProviders.add(new WebSuggestionProvider());
        mSearchManager = new SearchManager(searchProviders);

        AppCompatDelegate.setDefaultNightMode(SettingsManager.getTheme());

        super.onCreate(savedInstanceState);
        getWindow().setDimAmount(0f);
        setContentView(R.layout.activity_main);

        mAppLoader = new AppLoader(getApplicationContext());
        mAppsView = findViewById(R.id.recyclerview_apps);
        mAppsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mAppsAdapter = new CombinedAdapter(new ArrayList<>(), this::launchApp, this::onAppLongPressed, this::openWebQuery, mIconPackLoader);
        mAppsView.setAdapter(mAppsAdapter);
        mIndexView = findViewById(R.id.alphabet_index);

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (velocityY < -500) {
                    openSearchSheet();
                    return true;
                }
                return false;
            }
        });

        mAppsView.setLongClickable(true);
        mAppsView.setOnLongClickListener(this);

        loadAppsAsync();

        mNowPlayingView = new NowPlayingView(this);
        mNowPlayingView.initialize();

        initialiseSearchView();

        mIndexView.setOnLetterSelectedListener(letter -> {
            var items = mAppsAdapter.getItems();
            var startIndex = AlphabetIndexView.LETTERS.indexOf(letter);

            var found = false;

            // Try from current letter up to Z
            for (var offset = 0; startIndex + offset < AlphabetIndexView.LETTERS.length(); offset++) {
                var currentLetter = AlphabetIndexView.LETTERS.charAt(startIndex + offset);

                for (var i = 0; i < items.size(); i++) {
                    var item = items.get(i);
                    if (item instanceof AppItem appItem) {
                        var appInfo = appItem.getAppInfo();
                        var label = appInfo.getLabel().toUpperCase();
                        if (label.startsWith(String.valueOf(currentLetter))) {
                            if (mFavouritesHelper.isFavourite(appInfo.getPackageName())) {
                                // Don't go to favourites at the top (in the wrong position)
                                continue;
                            }

                            mAppsView.scrollToPosition(i);
                            found = true;
                            break;
                        }
                    }
                }

                if (found){
                    break;
                }
            }

            // We've not found anything
            // Presumably, we're at like Z, so just go to the bottom
            if (!found) {
                mAppsView.scrollToPosition(items.size() - 1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasAestheticChanged()) {
            refreshUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        var filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mPackageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPackageReceiver);
    }


    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        // Scroll to top when the home button is pressed
        if (mAppsView != null) {
            mAppsView.smoothScrollToPosition(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public boolean onLongClick(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAppsView != null) {
                mAppsView.smoothScrollToPosition(0);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAppsAsync() {
        mExecutor.execute(() -> mFavouritesHelper.loadFavouritesAsync(favourites -> {
            var items = this.buildCombinedList(favourites);

            this.runOnUiThread(() -> {
                mAppsAdapter.clearItems();
                mAppsAdapter.addAll(items);
            });
        }));
    }

    private void launchApp(AppInfo app) {
        var pm = getPackageManager();
        var intent = pm.getLaunchIntentForPackage(app.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot launch " + app.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openWebQuery(String query) {
        var intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onAppLongPressed(AppInfo app) {
        var options = new String[] {
                mFavouritesHelper.isFavourite(app.getPackageName()) ? "Remove from favourites" : "Add to favourites",
                "Uninstall " + app.getLabel(),
                "Launcher Settings",
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle(app.getLabel())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Toggle favourites
                            mFavouritesHelper.loadFavouritesAsync(currentFavourites -> {
                                if (currentFavourites.contains(app.getPackageName())) {
                                    currentFavourites.remove(app.getPackageName());
                                } else {
                                    currentFavourites.add(app.getPackageName());
                                }
                                mFavouritesHelper.saveFavouritesAsync(currentFavourites);

                                mMainHandler.post(() -> {
                                    var combined = buildCombinedList(currentFavourites);
                                    mAppsAdapter.clearItems();
                                    mAppsAdapter.addAll(combined);
                                    mAppsAdapter.notifyDataSetChanged();
                                });
                            });
                            break;

                        case 1: // Uninstall
                            var packageUri = Uri.parse("package:" + app.getPackageName());
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                            uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(uninstallIntent);
                            break;

                        case 2: // Launcher Settings
                            var settingsIntent = new Intent(this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                    }
                })
                .show();
    }

    private List<ListItem> buildCombinedList(Set<String> favourites) {
        var apps = mAppLoader.loadInstalledApps();
        mAppSearchProvider.setAllApps(apps);

        var favApps = new ArrayList<AppInfo>();
        var otherApps = new ArrayList<AppInfo>();

        // NOTE: I can't decide if I want to split favourites or not, maybe it should be an option?
        for (var app : apps) {
            if (favourites.contains(app.getPackageName())) {
                favApps.add(app);
            }

            otherApps.add(app);
        }

        favApps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        otherApps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        var items = new ArrayList<ListItem>();

        if (!favApps.isEmpty()) {
            items.add(new HeaderItem("Favourites"));
            for (var app : favApps) {
                items.add(new AppItem(app));
            }
        }

        items.add(new HeaderItem("Apps"));
        for (var app : otherApps) {
            items.add(new AppItem(app));
        }

        return items;
    }

    private boolean hasAestheticChanged() {

        // First load, this is gonna happen anyway
        if (mLastFont == null) {
            return false;
        }

        var newIconPack = SettingsManager.getIconPack();
        var newFont = SettingsManager.getFont();
        return !Objects.equals(mLastIconPack, newIconPack) || !Objects.equals(mLastFont, newFont);
    }

    private void refreshUi() {
        IconPackLoader.clearCache();

        mLastIconPack = SettingsManager.getIconPack();
        mLastFont = SettingsManager.getFont();

        mIconPackLoader.setIconPackPackage(mLastIconPack);
        loadAppsAsync();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialiseSearchView() {
        var sheet = findViewById(R.id.search_sheet);
        mSearchSheet = BottomSheetBehavior.from(sheet);
        mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        mSearchResultsView = sheet.findViewById(R.id.search_results);
        mSearchResultsView.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new CombinedAdapter(new ArrayList<>(), this::launchApp, this::onAppLongPressed, this::openWebQuery, mIconPackLoader);
        mSearchResultsView.setAdapter(mSearchAdapter);

        mSearchInput = sheet.findViewById(R.id.search_input);
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSearchRunnable != null) {
                    mSearchHandler.removeCallbacks(mSearchRunnable);
                }

                final String query = s.toString();
                mSearchRunnable = () -> performSearch(query);
                mSearchHandler.postDelayed(mSearchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Start hidden
        mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        mSearchSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    EditText input = findViewById(R.id.search_input);
                    input.requestFocus();
                }

                // Prevent weird "peeking", it kinda stays open
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        // Close the search if we press the back button
        var onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // NOTE: Don't call super on this to prevent weird back animation
                if (mSearchSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    closeSearchSheet();
                }
            }
        });

        // Hacks to try and get swipe detection working
        var root = findViewById(R.id.root_coordinator);
        root.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));

        var rootLayout = findViewById(R.id.root_layout);
        var gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
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

        rootLayout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    public void openSearchSheet() {
        mSearchSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void closeSearchSheet() {
        mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        var input = (EditText) findViewById(R.id.search_input);
        input.clearFocus();
        input.setText("");;
        var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
            mSearchAdapter.clearItems();
            if (!results.isEmpty()) {
                mSearchAdapter.addAll(results);
            } else {
                mSearchAdapter.add(new HeaderItem("No results"));
            }

            mSearchAdapter.notifyDataSetChanged();
        });
    }
}