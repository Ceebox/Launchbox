package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.APPEARANCE)
public final class ThemeOption implements ISettingOption {

    @Override
    public String getTitle() {
        return "Choose Theme";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return getCurrentThemeName();
    }

    @Override
    public void performClick(SettingsActivity activity) {
        showThemeDialog(activity);
    }

    private void showThemeDialog(SettingsActivity activity) {
        final var themes = new String[] { "System Default", "Light", "Dark" };
        final var themePrefs = new int[] { Configuration.UI_MODE_NIGHT_UNDEFINED, Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES };

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Theme")
            .setItems(themes, (dialog, which) -> {
                final var chosen = themePrefs[which];
                SettingsManager.setTheme(chosen);

                Toast.makeText(activity,
                    "Theme applied: " + themes[which],
                    Toast.LENGTH_SHORT).show();

                activity.recreate();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private String getCurrentThemeName() {
        return switch (SettingsManager.getTheme()) {
            case Configuration.UI_MODE_NIGHT_NO -> "Light";
            case Configuration.UI_MODE_NIGHT_YES -> "Dark";
            default -> "System Default";
        };
    }
}
