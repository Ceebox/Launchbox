package com.chadderbox.launchbox.settings.options.implementation;

import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.APPEARANCE)
public final class FontOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Choose Font";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return SettingsManager.getFont();
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showFontDialog(activity);
    }

    private void showFontDialog(final SettingsActivity activity) {
        var fonts = new String[]{
            "System Default",
            "Sans Serif",
            "Serif",
            "Monospace"
        };

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Font")
            .setItems(fonts, (dialog, which) -> {
                var chosenFont = fonts[which];

                if (chosenFont.equals(SettingsManager.getFont())) {
                    return;
                }

                SettingsManager.setFont(chosenFont);
                Toast.makeText(activity, "Font applied: " + chosenFont, Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
