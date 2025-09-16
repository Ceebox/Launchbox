package com.chadderbox.launchbox.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.Objects;

public final class SettingsActivity extends AppCompatActivity {

    private ArrayList<SettingOption> mOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.setTheme(this, SettingsManager.getTheme());

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
            "Show Character Headings",
            ctx -> SettingsManager.getCharacterHeadings() ? "On" : "Off",
            ctx -> showCharacterHeadingDialog()
        ));

        mOptions.add(new SettingOption(
                "Choose Font",
                ctx -> SettingsManager.getFont(),
                ctx -> showFontDialog()
        ));

        mOptions.add(new SettingOption(
            "Font Size",
            ctx -> String.valueOf(SettingsManager.getFontSize()),
            ctx -> showFontSizeDialog()
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

    private void showCharacterHeadingDialog() {
        var options = new String[] { "On", "Off" };
        var current = SettingsManager.getCharacterHeadings();
        var checkedItem = current ? 0 : 1;

        new AlertDialog.Builder(this)
            .setTitle("Character Headings")
            .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                var enabled = which == 0;
                if (enabled == current) {
                    return;
                }

                SettingsManager.setCharacterHeadings(enabled);
                Toast.makeText(this, "Character headings: " + (enabled ? "On" : "Off"), Toast.LENGTH_SHORT).show();

                buildOptions();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
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

                    if (chosenFont.equals(SettingsManager.getFont())) {
                        return;
                    }

                    SettingsManager.setFont(chosenFont);
                    Toast.makeText(this, "Font applied: " + chosenFont, Toast.LENGTH_SHORT).show();

                    buildOptions();
                })
                .show();
    }

    private void showFontSizeDialog() {
        var seekBar = new SeekBar(this);
        seekBar.setMax(32 - 14);
        seekBar.setProgress(SettingsManager.getFontSize() - 14);
        seekBar.setPadding(40, 40, 40, 40);

        var preview = new TextView(this);
        preview.setText(getString(R.string.font_size_pick, String.valueOf(SettingsManager.getFontSize())));
        preview.setTextSize(16);
        preview.setPadding(40, 20, 40, 20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                var fontSize = 14 + progress;
                preview.setText(getString(R.string.font_size_pick, String.valueOf(fontSize)));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        var layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(preview);
        layout.addView(seekBar);

        new AlertDialog.Builder(this)
            .setTitle("Select Font Size")
            .setView(layout)
            .setPositiveButton("OK", (dialog, which) -> {
                var selectedSize = 14 + seekBar.getProgress();
                if (selectedSize == SettingsManager.getFontSize()) {
                    return;
                }

                SettingsManager.setFontSize(selectedSize);
                Toast.makeText(this, "Size applied: " + selectedSize, Toast.LENGTH_SHORT).show();

                buildOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showThemeDialog() {
        var themes = new String[] { "System Default", "Light", "Dark", "OLED" };
        var themeModes = new int[]{
            ThemeHelper.MODE_NIGHT_FOLLOW_SYSTEM,
            ThemeHelper.MODE_NIGHT_NO,
            ThemeHelper.MODE_NIGHT_YES,
            ThemeHelper.MODE_NIGHT_OLED
        };

        new AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(themes, (dialog, which) -> {
                var chosenMode = themeModes[which];
                ThemeHelper.setTheme(this, chosenMode);

                Toast.makeText(this, "Theme applied: " + themes[which], Toast.LENGTH_SHORT).show();

                buildOptions();
            })
            .show();
    }

    private String getCurrentThemeName() {
        return switch (SettingsManager.getTheme()) {
            case ThemeHelper.MODE_NIGHT_NO -> "Light";
            case ThemeHelper.MODE_NIGHT_YES -> "Dark";
            case ThemeHelper.MODE_NIGHT_OLED -> "OLED";
            default -> "System Default";
        };
    }
}
