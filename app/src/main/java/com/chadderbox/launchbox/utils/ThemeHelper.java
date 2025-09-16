package com.chadderbox.launchbox.utils;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsManager;

public final class ThemeHelper {

    public static final int MODE_NIGHT_FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int MODE_NIGHT_NO = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_NIGHT_YES = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int MODE_NIGHT_OLED = 3;

    private ThemeHelper() { }

    public static void setTheme(Activity ctx, int newTheme) {

        var newNightMode = newTheme == MODE_NIGHT_OLED ? MODE_NIGHT_YES : newTheme;
        var newAppTheme = newTheme == MODE_NIGHT_OLED ? R.style.Theme_Launcherbox_OLED : R.style.Theme_Launcherbox_Launcher;

        if (newTheme == SettingsManager.getTheme()) {
            return;
        }

        AppCompatDelegate.setDefaultNightMode(newNightMode);
        ctx.setTheme(newAppTheme);
        SettingsManager.setTheme(newTheme);
    }
}
