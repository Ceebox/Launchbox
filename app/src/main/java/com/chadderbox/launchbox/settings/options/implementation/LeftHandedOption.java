package com.chadderbox.launchbox.settings.options.implementation;

import android.app.AlertDialog;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

@SettingCategory(category = SettingGroup.ACCESSIBILITY)
public final class LeftHandedOption implements ISettingOption {

    @Override
    public String getTitle() {
        return "Left Handed";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return SettingsManager.getLeftHanded() ? "On" : "Off";
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        var options = new String[] { "Off", "On" };
        var initiallyEnabled = SettingsManager.getLeftHanded();
        var checkedItem = initiallyEnabled ? 1 : 0;

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Left Handed")
            .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                var enabled = which == 1;
                if (enabled == initiallyEnabled) {
                    return;
                }

                SettingsManager.setLeftHanded(enabled);
                Toast.makeText(activity, "Left Handed: " + (enabled ? "On" : "Off"), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
