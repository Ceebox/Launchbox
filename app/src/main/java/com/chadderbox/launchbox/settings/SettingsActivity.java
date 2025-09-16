package com.chadderbox.launchbox.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.utils.FileHelpers;

import java.util.ArrayList;
import java.util.Objects;

public final class SettingsActivity extends AppCompatActivity {

    private SettingOptionAdapter mOptionsAdapter;
    private ArrayList<SettingOption> mOptions;
    private ActivityResultLauncher<Intent> mWallpaperPickerLauncher;

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

        mWallpaperPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    var selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {

                        // By selecting it, we gain permission to access it
                        // That's pretty neat actually, but it's gonna be super annoying if we don't save that permission
                        getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        SettingsManager.setWallpaper(selectedImageUri.toString());
                        Toast.makeText(SettingsActivity.this, "Wallpaper selected", Toast.LENGTH_SHORT).show();
                        refreshOptions();
                    }
                }
            }
        );

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
            "Wallpaper",
            ctx -> getCurrentWallpaper(),
            ctx -> showWallpaperDialog()
        ));

        mOptions.add(new SettingOption(
            "Wallpaper Dim Percentage",
            ctx -> getWallpaperDimPercentage(),
            ctx -> showWallpaperDimDialog()
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

        mOptionsAdapter = new SettingOptionAdapter(mOptions);
        recyclerView.setAdapter(mOptionsAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshOptions() {
        // Re-evaluate all options
        mOptionsAdapter.notifyDataSetChanged();
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

                refreshOptions();
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

                refreshOptions();
            })
            .setNegativeButton("Cancel", null)
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

                    refreshOptions();
                })
                .setNegativeButton("Cancel", null)
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
            .setPositiveButton("Ok", (dialog, which) -> {
                var selectedSize = 14 + seekBar.getProgress();
                if (selectedSize == SettingsManager.getFontSize()) {
                    return;
                }

                SettingsManager.setFontSize(selectedSize);
                Toast.makeText(this, "Size applied: " + selectedSize, Toast.LENGTH_SHORT).show();

                refreshOptions();
            })
            .setNegativeButton("Cancel", null)
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

                    refreshOptions();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getCurrentThemeName() {
        return switch (SettingsManager.getTheme()) {
            case AppCompatDelegate.MODE_NIGHT_NO -> "Light";
            case AppCompatDelegate.MODE_NIGHT_YES -> "Dark";
            default -> "System Default";
        };
    }

    private void showWallpaperDialog() {

        var options = new String[]{
            "Choose Wallpaper",
            "Clear Wallpaper"
        };

        new AlertDialog.Builder(this)
            .setTitle("Select Wallpaper")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mWallpaperPickerLauncher.launch(intent);
                        break;

                    case 1:
                        new AlertDialog.Builder(this)
                            .setTitle("Clear Wallpaper")
                            .setMessage("Are you sure you want to remove the current wallpaper?")
                            .setPositiveButton("Yes", (d, w) -> {
                                SettingsManager.setWallpaper("");
                                Toast.makeText(this, "Wallpaper cleared", Toast.LENGTH_SHORT).show();

                                refreshOptions();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                        break;
                }

                refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String getCurrentWallpaper() {
        var wallpaper = FileHelpers.tryGetFileNameFromString(getApplicationContext(), SettingsManager.getWallpaper());
        if (wallpaper == null || wallpaper.isEmpty()) {
            return "None";
        }

        return wallpaper;
    }

    private void showWallpaperDimDialog() {
        var seekBar = new SeekBar(this);
        seekBar.setMax(100);
        seekBar.setProgress((int) (SettingsManager.getWallpaperDimAmount() * 100));
        seekBar.setPadding(40, 40, 40, 40);

        var preview = new TextView(this);
        preview.setText(getString(R.string.wallpaper_dim_pick, SettingsManager.getWallpaperDimAmount() * 100 + "%"));
        preview.setTextSize(16);
        preview.setPadding(40, 20, 40, 20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preview.setText(getString(R.string.wallpaper_dim_pick, progress + "%"));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        var layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(preview);
        layout.addView(seekBar);

        new AlertDialog.Builder(this)
            .setTitle("Select Dim Amount")
            .setView(layout)
            .setPositiveButton("Ok", (dialog, which) -> {
                var dimAmount = seekBar.getProgress();
                if (dimAmount == SettingsManager.getWallpaperDimAmount()) {
                    return;
                }

                // Remember to convert back from percentage
                SettingsManager.setWallpaperDimAmount((float) dimAmount / 100);
                Toast.makeText(this, "Wallpaper Dim Amount: " + (dimAmount + "%"), Toast.LENGTH_SHORT).show();

                refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String getWallpaperDimPercentage() {
        return SettingsManager.getWallpaperDimAmount() * 100 + "%";
    }
}