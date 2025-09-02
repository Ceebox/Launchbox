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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.chadderbox.launcherbox.components.AlphabetIndexView;
import com.chadderbox.launcherbox.data.AppInfo;
import com.chadderbox.launcherbox.data.HeaderItem;
import com.chadderbox.launcherbox.data.SettingItem;
import com.chadderbox.launcherbox.search.AppSearchProvider;
import com.chadderbox.launcherbox.search.SearchManager;
import com.chadderbox.launcherbox.search.SettingsSearchProvider;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    private static final long SEARCH_DELAY_MS = 300;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private AppLoader mAppLoader;
    private SearchManager mSearchManager;
    private Runnable mSearchRunnable;
    private MainPagerAdapter mPagerAdapter;
    private AlphabetIndexView mIndexView;
    private GestureDetector mGestureDetector;
    private FavouritesRepository mFavouritesHelper;
    private String mLastIconPack;
    private String mLastFont;
    private IconPackLoader mIconPackLoader;
    private BottomSheetBehavior<View> mSearchSheet;
    private CombinedAdapter mSearchAdapter;
    private Fragment mCurrentFragment;

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
                    mPagerAdapter.refresh();
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

        mAppLoader = new AppLoader(this);
        var appSearchProvider = new AppSearchProvider(mAppLoader);
        var searchProviders = List.of(
            appSearchProvider,
            new WebSearchProvider(),
            new WebSuggestionProvider(),
            new SettingsSearchProvider(getApplicationContext())
        );

        mSearchManager = new SearchManager(searchProviders);

        AppCompatDelegate.setDefaultNightMode(SettingsManager.getTheme());

        super.onCreate(savedInstanceState);
        getWindow().setDimAmount(0f);
        setContentView(R.layout.activity_main);

        initialiseSearchView();

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

        var favouritesAdapter = new CombinedAdapter(new ArrayList<>(),
            this::launchApp,
            this::showAppMenu,
            query -> {},
            setting -> {},
            mIconPackLoader
        );

        var appsAdapter = new CombinedAdapter(new ArrayList<>(),
            this::launchApp,
            this::showAppMenu,
            query -> {},
            setting -> {},
            mIconPackLoader
        );

        var favouritesFragment = new FavouritesFragment(favouritesAdapter, mAppLoader, mFavouritesHelper);
        var appsFragment = new AppsFragment(appsAdapter, mAppLoader);
        mPagerAdapter = new MainPagerAdapter(this, new AppListFragmentBase[] {
            favouritesFragment,
            appsFragment
        });

        var viewPager = (ViewPager2) findViewById(R.id.viewpager);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentFragment = mPagerAdapter.getFragmentAt(position);
            }
        });

        mIndexView = findViewById(R.id.alphabet_index);
        mIndexView.setOnLetterSelectedListener(letter -> {
            viewPager.setCurrentItem(1);
            appsFragment.scrollToLetter(letter);
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
        if (mCurrentFragment instanceof AppsFragment appsFragment) {
            appsFragment.smoothScrollToPosition(0);
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
            if (mCurrentFragment instanceof AppsFragment appsFragment) {
                appsFragment.smoothScrollToPosition(0);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void openSetting(SettingItem settingItem) {
        startActivity(settingItem.getIntent());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAppMenu(AppInfo app) {
        var appName = app.getLabel();
        var options = new String[] {
                mFavouritesHelper.isFavourite(app.getPackageName()) ? "Unfavourite " + appName : "Favourite " + appName,
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

                                mMainHandler.post(() -> mPagerAdapter.refresh());
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

        mAppLoader.refreshInstalledApps();
        mPagerAdapter.refresh();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialiseSearchView() {
        var sheet = findViewById(R.id.search_sheet);
        mSearchSheet = BottomSheetBehavior.from(sheet);
        mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        var searchResultsView = (RecyclerView) sheet.findViewById(R.id.search_results);
        searchResultsView.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new CombinedAdapter(new ArrayList<>(), this::launchApp, this::showAppMenu, this::openWebQuery, this::openSetting, mIconPackLoader);
        searchResultsView.setAdapter(mSearchAdapter);

        var searchInput = (EditText) sheet.findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
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
                    var input = (EditText) findViewById(R.id.search_input);
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
        input.setText("");
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