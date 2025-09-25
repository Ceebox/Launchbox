package com.chadderbox.launchbox.settings.options.implementation;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.components.FontTextView;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.APPEARANCE)
public final class WallpaperDimOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Wallpaper Dim Percentage";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return getWallpaperDimPercentage();
    }

    @Override
    public void performClick(SettingsActivity activity) {
        showWallpaperDimDialog(activity);
    }

    private String getWallpaperDimPercentage() {
        return SettingsManager.getWallpaperDimAmount() * 100 + "%";
    }

    private void showWallpaperDimDialog(SettingsActivity activity) {
        var seekBar = new SeekBar(activity);
        seekBar.setMax(100);
        seekBar.setProgress((int) (SettingsManager.getWallpaperDimAmount() * 100));
        seekBar.setPadding(40, 40, 40, 40);

        var preview = new FontTextView(activity);
        preview.setText(activity.getString(R.string.wallpaper_dim_pick, SettingsManager.getWallpaperDimAmount() * 100 + "%"));
        preview.setTextSize(16);
        preview.setPadding(40, 20, 40, 20);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preview.setText(activity.getString(R.string.wallpaper_dim_pick, progress + "%"));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        var layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(preview);
        layout.addView(seekBar);

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Dim Amount")
            .setView(layout)
            .setPositiveButton("Ok", (dialog, which) -> {
                var dimAmount = seekBar.getProgress();
                if (dimAmount == SettingsManager.getWallpaperDimAmount()) {
                    return;
                }

                // Remember to convert back from percentage
                SettingsManager.setWallpaperDimAmount((float) dimAmount / 100);
                Toast.makeText(activity, "Wallpaper Dim Amount: " + (dimAmount + "%"), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
