package com.chadderbox.launchbox;

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

import com.chadderbox.launchbox.components.AlphabetIndexView;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.search.AppSearchProvider;
import com.chadderbox.launchbox.search.SearchManager;
import com.chadderbox.launchbox.search.SettingsSearchProvider;
import com.chadderbox.launchbox.search.WebSearchProvider;
import com.chadderbox.launchbox.search.WebSuggestionProvider;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.utils.IconPackLoader;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity implements View.OnLongClickListener, IAdapterFetcher {

    private static final long SEARCH_DELAY_MS = 300;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private final HashMap<Class<? extends AppListFragmentBase>, CombinedAdapter> mAdapters = new HashMap<>();
    private AppLoader mAppLoader;
    private SearchManager mSearchManager;
    private Runnable mSearchRunnable;
    private MainPagerAdapter mPagerAdapter;
    private ViewPager2 mViewPager;
    private GestureDetector mGestureDetector;
    private FavouritesRepository mFavouritesHelper;
    private String mLastIconPack;
    private String mLastFont;
    private int mLastTheme;
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
                    refreshAllVisibleFragments();
                    break;
            }
        }
    };

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SettingsManager.initialiseSettingsManager(getApplicationContext());

        mLastIconPack = SettingsManager.getIconPack();
        mLastFont = SettingsManager.getFont();
        mLastTheme = SettingsManager.getTheme();

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

        var appsAdapter = new CombinedAdapter(
            new ArrayList<>(),
            mIconPackLoader
        );

        mAdapters.put(AppsFragment.class, appsAdapter);

        var favouritesAdapter = new CombinedAdapter(
            new ArrayList<>(),
            mIconPackLoader
        );

        mAdapters.put(FavouritesFragment.class, favouritesAdapter);

        var fragments = List.of(
            FavouritesFragment.class,
            AppsFragment.class
        );

        mPagerAdapter = new MainPagerAdapter(this, fragments);

        refreshFavouritesFragment();

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentFragment = findPagerFragment(position);
            }
        });

        var indexView = (AlphabetIndexView)findViewById(R.id.alphabet_index);
        indexView.setOnLetterSelectedListener(letter -> {
            var lastPos = mPagerAdapter.getItemCount() - 1;
            mViewPager.setCurrentItem(lastPos);

            var fragment = findPagerFragment(lastPos);
            if (fragment instanceof AppsFragment appsFragment) {
                appsFragment.scrollToLetter(letter);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // NOTE: Don't call super on this to prevent weird back animation
                // Close the search if we press the back button
                if (mSearchSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    closeSearchSheet();
                }

                // Otherwise take us to the favourites section
                var firstFragment = findPagerFragment(0);
                if (mCurrentFragment != firstFragment) {
                    mViewPager.setCurrentItem(0, true);
                    mCurrentFragment = firstFragment;

                    // This doesn't really work
                    // TODO: Figure out why?
                    if (mCurrentFragment instanceof AppsFragment appsFragment) {
                        appsFragment.scrollToPosition(0);
                    }
                } else {
                    setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasAestheticChanged()) {
            refreshUi();
        }

        closeSearchSheet();
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

    @Override
    public CombinedAdapter getAdapter(Class<? extends AppListFragmentBase> fragmentClass) {
        return mAdapters.get(fragmentClass);
    }

    public void launchApp(AppInfo app) {
        var pm = getPackageManager();
        var intent = pm.getLaunchIntentForPackage(app.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot launch " + app.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openWebQuery(String query) {
        var intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
        startActivity(intent);
    }

    public void openSetting(SettingItem settingItem) {
        startActivity(settingItem.getIntent());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showAppMenu(AppInfo app) {
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

                                mFavouritesHelper.saveFavourites(currentFavourites);
                                refreshFavouritesFragment();

                                mMainHandler.post(this::refreshAllVisibleFragments);
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
            return true;
        }

        var newIconPack = SettingsManager.getIconPack();
        var newFont = SettingsManager.getFont();
        var newTheme = SettingsManager.getTheme();
        return !Objects.equals(mLastIconPack, newIconPack) ||
            !Objects.equals(mLastFont, newFont) ||
            !Objects.equals(mLastTheme, newTheme);
    }

    private void refreshUi() {
        IconPackLoader.clearCache();

        mLastIconPack = SettingsManager.getIconPack();
        mLastFont = SettingsManager.getFont();
        mLastTheme = SettingsManager.getTheme();

        mIconPackLoader.setIconPackPackage(mLastIconPack);

        mAppLoader.refreshInstalledApps();
        refreshAllVisibleFragments();
    }

    private void refreshFavouritesFragment() {
        mPagerAdapter.updateVisibility(fragment -> {
            if (fragment instanceof FavouritesFragment) {
                return mFavouritesHelper.hasFavourites();
            }

            return true;
        });
    }


    private void refreshAllVisibleFragments() {
        for (int i = 0; i < mPagerAdapter.getItemCount(); i++) {
            var fragment = findPagerFragment(i);
            if (fragment instanceof AppListFragmentBase base) {
                base.refresh();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialiseSearchView() {
        var sheet = findViewById(R.id.search_sheet);
        mSearchSheet = BottomSheetBehavior.from(sheet);
        mSearchSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        var searchResultsView = (RecyclerView) sheet.findViewById(R.id.search_results);
        searchResultsView.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new CombinedAdapter(new ArrayList<>(), mIconPackLoader);
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

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {

                if (mSearchAdapter.getItemCount() > 0) {
                    mSearchAdapter.getItem(0).performOpenAction(this);
                    closeSearchSheet();
                }

                handled = true;
            }

            return handled;
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

                    // Stop the pesky keyboard staying open
                    var input = (EditText) findViewById(R.id.search_input);
                    var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        // Hacks to try and get swipe detection working
        var root = findViewById(R.id.root_coordinator);
        root.setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
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

    private Fragment findPagerFragment(int position) {
        long itemId = mPagerAdapter.getItemId(position);
        return getSupportFragmentManager().findFragmentByTag("f" + itemId);
    }
}