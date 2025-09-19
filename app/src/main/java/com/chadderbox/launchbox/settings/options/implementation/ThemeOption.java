package com.chadderbox.launchbox.settings.options.implementation;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

public final class ThemeOption
    implements ISettingOption {
    @Override
    public String getTitle() {
        return "Choose Theme";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return getCurrentThemeName();
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showThemeDialog(activity);
    }

    private void showThemeDialog(final SettingsActivity activity) {
        var themes = new String[] { "System Default", "Light", "Dark" };
        var themeModes = new int[]{
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        };

        new AlertDialog.Builder(activity)
            .setTitle("Select Theme")
            .setItems(themes, (dialog, which) -> {
                var chosenMode = themeModes[which];
                SettingsManager.setTheme(chosenMode);
                AppCompatDelegate.setDefaultNightMode(chosenMode);
                activity.getDelegate().applyDayNight();
                Toast.makeText(activity, "Theme applied: " + themes[which], Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
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
}
