package com.chadderbox.launchbox.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.utils.IconPackLoader;
import com.chadderbox.launchbox.utils.IconPackParser;
import com.chadderbox.launchbox.R;

import java.util.ArrayList;
import java.util.Objects;

public final class SettingsActivity extends AppCompatActivity {

    private ArrayList<SettingOption> mOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SettingsManager.initialiseSettingsManager(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(SettingsManager.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        var actionBar = getSupportActionBar();
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
        mOptions.add(new SettingOption(
                "Choose Icon Pack",
                ctx -> SettingsManager.getIconPack(),
                ctx -> showIconPackDialog()
        ));

        mOptions.add(new SettingOption(
                "Choose Font",
                ctx -> SettingsManager.getFont(),
                ctx -> showFontDialog()
        ));

        mOptions.add(new SettingOption(
                "Choose Theme",
                ctx -> getCurrentThemeName(),
                ctx -> showThemeDialog()
        ));

        mOptions.add(new SettingOption(
                "Other settings…",
                ctx -> "More coming soon",
                ctx -> Toast.makeText(ctx, "Not implemented yet", Toast.LENGTH_SHORT).show()
        ));

        setupOptions();
    }

    private void setupOptions() {
        var recyclerView = (RecyclerView) findViewById(R.id.recyclerViewSettings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SettingOptionAdapter(mOptions));
    }

    private void showIconPackDialog() {
        var pm = getPackageManager();

        // Some popular icon pack intents, google
        var adwIntent = new Intent("org.adw.launcher.THEMES");
        var iconPackApps = new ArrayList<>(pm.queryIntentActivities(adwIntent, PackageManager.GET_META_DATA));

        var goIntent = new Intent("com.gau.go.launcherex.theme");
        iconPackApps.addAll(pm.queryIntentActivities(goIntent, PackageManager.GET_META_DATA));

        var names = new ArrayList<String>();
        var packageNames = new ArrayList<String>();

        names.add("System Default");
        packageNames.add("System Default");
        names.add("None");
        packageNames.add("None");

        // Populate dialog items with icon packs
        for (var info : iconPackApps) {
            var label = info.loadLabel(pm).toString();
            if (names.contains(label)) {
                continue;
            }

            names.add(label);
            packageNames.add(info.activityInfo.packageName);
        }

        new AlertDialog.Builder(this)
            .setTitle("Select Icon Pack")
            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                var chosenPackage = packageNames.get(which);
                SettingsManager.setIconPack(chosenPackage);

                Toast.makeText(this,
                    Objects.equals(chosenPackage, "System Default") ? "Using system icons"
                        : "Icon pack applied: " + names.get(which),
                    Toast.LENGTH_SHORT).show();

                // Clear cache since we've got a new theme
                IconPackParser.clearCache();
                IconPackLoader.clearCache();

                buildOptions();
            })
            .show();
    }

    private void showFontDialog() {
        var fonts = new String[]{
                "System Default",
                "Sans Serif",
                "Serif",
                "Monospace"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Font")
                .setItems(fonts, (dialog, which) -> {
                    var chosenFont = fonts[which];
                    SettingsManager.setFont(chosenFont);
                    Toast.makeText(this, "Font applied: " + chosenFont, Toast.LENGTH_SHORT).show();

                    buildOptions();
                })
                .show();
    }

    private void showThemeDialog() {
        var themes = new String[]{"System Default", "Light", "Dark"};
        var themeModes = new int[]{
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setItems(themes, (dialog, which) -> {
                    var chosenMode = themeModes[which];
                    SettingsManager.setTheme(chosenMode);
                    AppCompatDelegate.setDefaultNightMode(chosenMode);
                    getDelegate().applyDayNight();
                    Toast.makeText(this, "Theme applied: " + themes[which], Toast.LENGTH_SHORT).show();

                    buildOptions();
                })
                .show();
    }

    private String getCurrentThemeName() {
        return switch (SettingsManager.getTheme()) {
            case AppCompatDelegate.MODE_NIGHT_NO -> "Light";
            case AppCompatDelegate.MODE_NIGHT_YES -> "Dark";
            default -> "System Default";
        };
    }
}