package com.chadderbox.launchbox.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.options.ISettingOption;
import com.chadderbox.launchbox.settings.options.implementation.CharacterHeadingsOption;
import com.chadderbox.launchbox.settings.options.implementation.FontOption;
import com.chadderbox.launchbox.settings.options.implementation.FontSizeOption;
import com.chadderbox.launchbox.settings.options.implementation.IconPackOption;
import com.chadderbox.launchbox.settings.options.implementation.LeftHandedOption;
import com.chadderbox.launchbox.settings.options.implementation.NowPlayingWidgetOption;
import com.chadderbox.launchbox.settings.options.implementation.ShadowStrengthOption;
import com.chadderbox.launchbox.settings.options.implementation.ShowOnlyInstalledOption;
import com.chadderbox.launchbox.settings.options.implementation.ThemeOption;
import com.chadderbox.launchbox.settings.options.implementation.WallpaperDimOption;
import com.chadderbox.launchbox.settings.options.implementation.WallpaperOverrideOption;
import com.chadderbox.launchbox.utils.CustomFontFactory;
import com.chadderbox.launchbox.utils.ThemeHelper;

import java.util.ArrayList;

public final class SettingsActivity extends ComponentActivity {

    private SettingOptionAdapter mOptionsAdapter;
    private ArrayList<ISettingOption> mOptions;

    @Override
    protected void attachBaseContext(Context newBase) {
        var contextWithTheme = ThemeHelper.getContextWithTheme(newBase);
        super.attachBaseContext(contextWithTheme);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        CustomFontFactory.initialise(this);

        SettingsManager.initialiseSettingsManager(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        var actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        buildOptions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void buildOptions() {
        mOptions = new ArrayList<>();
        mOptions.add(new ThemeOption());
        mOptions.add(new IconPackOption());
        mOptions.add(new FontOption());
        mOptions.add(new FontSizeOption());
        mOptions.add(new ShadowStrengthOption());
        mOptions.add(new WallpaperOverrideOption(this));
        mOptions.add(new WallpaperDimOption());
        mOptions.add(new NowPlayingWidgetOption());
        mOptions.add(new LeftHandedOption());
        mOptions.add(new ShowOnlyInstalledOption());
        mOptions.add(new CharacterHeadingsOption());

        setupOptions();
    }

    private void setupOptions() {
        var recyclerView = (RecyclerView) findViewById(R.id.recyclerViewSettings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mOptionsAdapter = new SettingOptionAdapter(mOptions);
        recyclerView.setAdapter(mOptionsAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshOptions() {
        // Re-evaluate all options
        mOptionsAdapter.notifyDataSetChanged();
    }
}