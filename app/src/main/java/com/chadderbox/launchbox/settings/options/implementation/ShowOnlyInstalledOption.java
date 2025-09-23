package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.widget.Toast;

import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

public final class ShowOnlyInstalledOption implements ISettingOption {

    @Override
    public String getTitle() {
        return "Installed App Letters Only";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return SettingsManager.getShowOnlyInstalled() ? "On" : "Off";
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        var options = new String[] { "Off", "On" };
        var initiallyEnabled = SettingsManager.getShowOnlyInstalled();
        var checkedItem = initiallyEnabled ? 1 : 0;

        new AlertDialog.Builder(activity)
            .setTitle("Installed App Letters Only")
            .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                var enabled = which == 1;
                if (enabled == initiallyEnabled) {
                    return;
                }

                SettingsManager.setShowOnlyInstalled(enabled);
                Toast.makeText(activity, "Show Only Installed Apps: " + (enabled ? "On" : "Off"), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
