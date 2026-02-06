package com.chadderbox.launchbox.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.dialogs.CommandService;
import com.chadderbox.launchbox.main.adapters.CombinedAdapter;
import com.chadderbox.launchbox.main.adapters.IAdapterFetcher;
import com.chadderbox.launchbox.main.adapters.MainPagerAdapter;
import com.chadderbox.launchbox.main.commands.EnterEditModeCommand;
import com.chadderbox.launchbox.main.commands.OpenSettingsCommand;
import com.chadderbox.launchbox.main.commands.RenameCommand;
import com.chadderbox.launchbox.main.commands.ToggleFavouriteCommand;
import com.chadderbox.launchbox.main.commands.ToggleHideCommand;
import com.chadderbox.launchbox.main.commands.UninstallCommand;
import com.chadderbox.launchbox.main.controllers.AlphabetViewController;
import com.chadderbox.launchbox.main.controllers.FragmentController;
import com.chadderbox.launchbox.main.controllers.SearchController;
import com.chadderbox.launchbox.main.controllers.ViewPagerController;
import com.chadderbox.launchbox.main.fragments.AppListFragmentBase;
import com.chadderbox.launchbox.main.fragments.AppsFragment;
import com.chadderbox.launchbox.main.fragments.FavouritesFragment;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.data.SettingItem;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.AppAliasProvider;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.fonts.CustomFontFactory;
import com.chadderbox.launchbox.utils.FavouritesRepository;
import com.chadderbox.launchbox.icons.IconPackLoader;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.ui.ThemeHelper;
import com.chadderbox.launchbox.utils.HiddenAppsRepository;
import com.chadderbox.launchbox.wallpaper.WallpaperManager;
import com.chadderbox.launchbox.widgets.WidgetHostManager;
import com.chadderbox.launchbox.widgets.commands.AddWidgetCommand;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity
    extends FragmentActivity
    implements View.OnLongClickListener,
    IAdapterFetcher,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final HashMap<Class<? extends AppListFragmentBase>, CombinedAdapter> mAdapters = new HashMap<>();
    private AppLoader mAppLoader;
    private AppAliasProvider mAppAliasHelper;
    private AlphabetViewController mAlphabetController;
    private WallpaperManager mWallpaperManager;
    private ViewPagerController mViewPagerController;
    private FragmentController mFragmentController;
    private SearchController mSearchController;
    private FavouritesRepository mFavouritesHelper;
    private HiddenAppsRepository mHiddenAppsHelper;
    private IconPackLoader mIconPackLoader;
    private boolean mIsEditMode = false;

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
                    // TODO: Probably remove this from favourites and aliases
                case Intent.ACTION_PACKAGE_CHANGED:
                    mAlphabetController.populateAlphabetViewLetters();
                    mViewPagerController.refreshAllVisibleFragments();
                    mSearchController.notifyAppsChanged();
                    break;
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {

        // If we don't init this here, we get a crash
        SettingsManager.initialiseSettingsManager(newBase);
        SettingsManager.registerChangeListener(this);

        super.attachBaseContext(ThemeHelper.getContextWithTheme(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ServiceManager.registerActivity(MainActivity.class, this);
        CustomFontFactory.initialise(this);

        ServiceManager.registerService(IconPackLoader.class, () -> mIconPackLoader = new IconPackLoader(getApplicationContext(), SettingsManager.getIconPack()));
        ServiceManager.registerService(FavouritesRepository.class, () -> mFavouritesHelper = new FavouritesRepository(mExecutor));
        ServiceManager.registerService(HiddenAppsRepository.class, () -> mHiddenAppsHelper = new HiddenAppsRepository());
        ServiceManager.registerService(AppAliasProvider.class, () -> mAppAliasHelper = new AppAliasProvider());
        ServiceManager.registerService(AppLoader.class, () -> mAppLoader = new AppLoader(this, mAppAliasHelper));
        ServiceManager.registerService(CommandService.class, () -> new CommandService((this)));

        super.onCreate(savedInstanceState);
        getWindow().setDimAmount(0f);
        setContentView(R.layout.activity_main);

        mWallpaperManager = new WallpaperManager(findViewById(R.id.wallpaper_image));
        mWallpaperManager.applyBackground();

        mFragmentController = new FragmentController();

        mSearchController = new SearchController(findViewById(R.id.search_sheet));
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

        var pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        mViewPagerController = new ViewPagerController(pagerAdapter, findViewById(R.id.viewpager));
        mAlphabetController = new AlphabetViewController(mViewPagerController, findViewById(R.id.alphabet_index));

        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, () -> {
            // NOTE: Don't call super on this to prevent weird back animation
            // Close the search if we press the back button
            if (mSearchController.getSheetState() == BottomSheetBehavior.STATE_EXPANDED) {
                mSearchController.closeSearchSheet();
            }

            // TODO: Possibly make this customisable?
            // Otherwise, if we're on the apps section, scroll to the top
            if (mFragmentController.getCurrentFragment() instanceof AppsFragment appsFragment) {
                appsFragment.smoothScrollToPosition(0);
            }

            exitEditMode();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSearchController.closeSearchSheet();
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
        if (Intent.ACTION_MAIN.equals(intent.getAction()) &&
            intent.hasCategory(Intent.CATEGORY_HOME)
        ) {
            mViewPagerController.scrollToFavourites();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSearchController.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    @Override
    public boolean onLongClick(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mFragmentController.getCurrentFragment() instanceof AppsFragment appsFragment) {
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
            Toast.makeText(this, "Cannot launch " + app.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openWebQuery(String query) {
        var intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
        startActivity(intent);
    }

    public void openSetting(SettingItem settingItem) {
        startActivity(settingItem.getIntent());
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showAppMenu(View appView, AppInfo app) {
        var packageName = app.getPackageName();
        var isFavourite = mFavouritesHelper.isFavourite(packageName);
        var onFavouritesScreen = mFragmentController.getCurrentFragment() instanceof FavouritesFragment;
        var widgetManager = ServiceManager.getService(WidgetHostManager.class);
        var commandService = ServiceManager.getService(CommandService.class);

        //TODO: Conditionally add the EnterEditMode command here if it is a favourite
        var commands = new ArrayList<>((List.of(
            new ToggleFavouriteCommand(app, isFavourite),
            new RenameCommand(app),
            new UninstallCommand(app),
            new AddWidgetCommand(widgetManager),
            new OpenSettingsCommand()
        )));

        if (!onFavouritesScreen) {
            // Why hide a favourite app?
            var isHidden = mHiddenAppsHelper.isHidden(packageName);
            commands.add(3, new ToggleHideCommand(app, isHidden));
        }

        if (isFavourite && onFavouritesScreen) {
            commands.add(3, new EnterEditModeCommand());
        }

        commandService.showCommandPopup(appView, commands);
    }

    // TODO: This is bad! We shouldn't need this!
    public ViewPagerController getViewPagerController() {
        return mViewPagerController;
    }

    public FragmentController getFragmentController() {
        return mFragmentController;
    }

    public boolean isEditMode() {
        return mIsEditMode;
    }

    public void enterEditMode() {
        if (mIsEditMode) {
            return;
        }

        mIsEditMode = true;
        mAlphabetController.setVisible(false);
        mViewPagerController.enterEditMode();
    }

    public void exitEditMode() {
        if (!mIsEditMode) {
            return;
        }

        mIsEditMode = false;
        mAlphabetController.setVisible(true);
        mViewPagerController.exitEditMode();
    }

    public void refreshUi() {
        mAppLoader.refreshInstalledApps();
        mViewPagerController.refreshAllVisibleFragments();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (SettingsManager.KEY_THEME.equals(key)) {
            recreate();
            return;
        }

        if (SettingsManager.KEY_ICON_PACK.equals(key) ||
            SettingsManager.KEY_CHARACTER_HEADINGS.equals(key) ||
            SettingsManager.KEY_FONT_SIZE.equals(key) ||
            SettingsManager.KEY_FONT.equals(key)
        ) {
            refreshUi();
            return;
        }

        if (SettingsManager.KEY_SHOW_ONLY_INSTALLED.equals(key)) {
            mAlphabetController.populateAlphabetViewLetters();
        }

        if (SettingsManager.KEY_WALLPAPER.equals(key) ||
            SettingsManager.KEY_WALLPAPER_DIM_AMOUNT.equals(key)
        ) {
            mWallpaperManager.applyBackground();
        }
    }
}
