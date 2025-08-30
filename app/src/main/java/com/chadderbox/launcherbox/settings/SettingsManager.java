package com.chadderbox.launcherbox.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.HashSet;
import java.util.Set;

public final class SettingsManager {
    private static final String PREFS_NAME = "cbxlauncher_prefs";
    private static final String KEY_ICON_PACK = "icon_pack";
    private static final String KEY_FONT = "font";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_THEME = "theme";

    private static SharedPreferences sPrefs;

    public static void initialiseSettingsManager(Context context) {
        sPrefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setIconPack(@Nullable String packageName) {
        sPrefs.edit().putString(KEY_ICON_PACK, packageName).apply();
    }

    @Nullable
    public static String getIconPack() {
        return sPrefs.getString(KEY_ICON_PACK, null);
    }

    public static boolean isUsingSystemIcons() {
        return getIconPack() == null;
    }

    public static void setFont(String fontName) {
        sPrefs.edit().putString(KEY_FONT, fontName).apply();
    }

    public static String getFont() {
        return sPrefs.getString(KEY_FONT, "system");
    }

    public static void setFavourites(Set<String> newFavourites) {
        sPrefs.edit().putStringSet(KEY_FAVORITES, newFavourites).apply();
    }

    public static HashSet<String> getFavourites() {
        return new HashSet<>(sPrefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    public static void setTheme(int mode) {
        sPrefs.edit().putInt(KEY_THEME, mode).apply();
    }

    public static int getTheme() {
        return sPrefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
