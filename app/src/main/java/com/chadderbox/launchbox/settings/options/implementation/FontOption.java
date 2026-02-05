package com.chadderbox.launchbox.settings.options.implementation;

import android.annotation.SuppressLint;
import android.graphics.fonts.SystemFonts;
import android.os.Build;
import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingGroup;
import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

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

    @SuppressLint("ObsoleteSdkInt")
    private void showFontDialog(final SettingsActivity activity) {
        var fontFamilies = new TreeSet<String>();
        fontFamilies.add("System Default");
        fontFamilies.add("Sans Serif");
        fontFamilies.add("Serif");
        fontFamilies.add("Monospace");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (var font : SystemFonts.getAvailableFonts()) {
                var file = font.getFile();
                if (file == null) {
                    continue;
                }

                var familyName = file.getName().split("-")[0];
                if (familyName.startsWith("Noto")) {
                    // There is like a bajillion noto sans / serif entries
                    // These are all just the default fonts anyway
                    continue;
                }

                fontFamilies.add(cleanName(familyName));
            }
        } else {
            var fontDir = new File("/system/fonts/");
            var files = fontDir.listFiles();
            if (files != null) {
                for (var file : files) {
                    var fileName = file.getName();
                    if (fileName.endsWith(".ttf") || fileName.endsWith(".otf")) {
                        var familyName = fileName.split("-")[0];
                        fontFamilies.add(cleanName(familyName));
                    }
                }
            }
        }

        var list = new ArrayList<>(fontFamilies);
        list.sort((a, b) -> {
            // Make the default appear at the top
            if (a.equals("System Default")) {
                return -1;
            }
            if (b.equals("System Default")) {
                return 1;
            }

            return a.compareToIgnoreCase(b);
        });

        var finalFonts = list.toArray(new String[0]);
        new AlertDialog.Builder(activity, R.style.Theme_Launcherbox_Dialog)
            .setTitle("Select Font")
            .setItems(finalFonts, (dialog, which) -> {
                var chosenFont = finalFonts[which];

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

    private static String cleanName(String name) {
        return name.replace(".ttf", "")
            .replace(".otf", "")
            .replaceAll("(?i)(Regular|Bold|Italic|Light|Medium|Thin|Black|Condensed)", "")
            .trim();
    }
}
