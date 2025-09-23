package com.chadderbox.launchbox.settings.options.implementation;

import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

public final class CharacterHeadingsOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Show Character Headings";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return SettingsManager.getCharacterHeadings() ? "On" : "Off";
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showCharacterHeadingDialog(activity);
    }

    private void showCharacterHeadingDialog(final SettingsActivity activity) {
        var options = new String[] { "On", "Off" };
        var current = SettingsManager.getCharacterHeadings();
        var checkedItem = current ? 0 : 1;

        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Character Headings")
            .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                var enabled = which == 0;
                if (enabled == current) {
                    return;
                }

                SettingsManager.setCharacterHeadings(enabled);
                Toast.makeText(activity, "Character headings: " + (enabled ? "On" : "Off"), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
