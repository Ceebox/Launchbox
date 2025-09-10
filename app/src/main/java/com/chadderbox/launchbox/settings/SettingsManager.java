package com.chadderbox.launchbox.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.HashSet;
import java.util.Set;

public final class SettingsManager {
    public static final String PREFS_NAME = "cbxlauncher_prefs";
    public static final String KEY_CHARACTER_HEADINGS = "character_headings";
    public static final String KEY_ICON_PACK = "icon_pack";
    public static final String KEY_FONT = "font";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_FAVORITES = "favorites";
    public static final String KEY_THEME = "theme";

    private static SharedPreferences sPrefs;

    public static void initialiseSettingsManager(Context context) {
        sPrefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void setCharacterHeadings(boolean hasCharacterHeadings) {
        sPrefs.edit().putBoolean(KEY_CHARACTER_HEADINGS, hasCharacterHeadings).apply();
    }

    public static boolean getCharacterHeadings() {
        return sPrefs.getBoolean(KEY_CHARACTER_HEADINGS, false);
    }

    public static void setIconPack(String packageName) {
        sPrefs.edit().putString(KEY_ICON_PACK, packageName).apply();
    }

    public static String getIconPack() {
        return sPrefs.getString(KEY_ICON_PACK, "System Default");
    }

    public static void setFont(String fontName) {
        sPrefs.edit().putString(KEY_FONT, fontName).apply();
    }

    public static String getFont() {
        return sPrefs.getString(KEY_FONT, "System Default");
    }

    public static void setFontSize(int fontName) {
        sPrefs.edit().putInt(KEY_FONT_SIZE, fontName).apply();
    }

    public static int getFontSize() {
        return sPrefs.getInt(KEY_FONT_SIZE, 16);
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
