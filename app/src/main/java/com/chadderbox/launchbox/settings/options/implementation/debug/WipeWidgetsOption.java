package com.chadderbox.launchbox.settings.options.implementation.debug;

import android.app.AlertDialog;
import android.widget.Toast;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.options.ISettingOption;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

@SettingCategory(category = SettingGroup.DEBUG)
public class WipeWidgetsOption implements ISettingOption {
    @Override
    public String getTitle() {
        return "Wipe All Widgets";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return "";
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Remove All Widgets")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Remove", (dialog, which) -> {
                ServiceManager.getService(WidgetHostManager.class).wipeWidgets();
                Toast.makeText(activity, "All widgets removed", Toast.LENGTH_SHORT).show();
                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
