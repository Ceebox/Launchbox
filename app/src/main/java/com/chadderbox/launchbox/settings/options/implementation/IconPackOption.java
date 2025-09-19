package com.chadderbox.launchbox.settings.options.implementation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

import java.util.ArrayList;
import java.util.Objects;

public final class IconPackOption implements ISettingOption {

    @Override
    public String getTitle() {
        return "Choose Icon Pack";
    }

    @Override
    public String getSubtitle(SettingsActivity activity) {
        return SettingsManager.getIconPack();
    }

    @Override
    public void performClick(SettingsActivity activity) {
        showIconPackDialog(activity);
    }

    private void showIconPackDialog(SettingsActivity activity) {
        var pm = activity.getPackageManager();

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

        new AlertDialog.Builder(activity)
            .setTitle("Select Icon Pack")
            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                var chosenPackage = packageNames.get(which);
                SettingsManager.setIconPack(chosenPackage);

                var message = Objects.equals(chosenPackage, "System Default")
                    ? "Using system icons"
                    : "Icon pack applied: " + names.get(which);

                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
