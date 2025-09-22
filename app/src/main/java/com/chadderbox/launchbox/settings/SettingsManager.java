package com.chadderbox.launchbox.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.HashSet;
import java.util.Set;

public final class SettingsManager {
    public static final String PREFS_NAME = "cbxlauncher_prefs";
    public static final String KEY_CHARACTER_HEADINGS = "character_headings";
    public static final String KEY_ICON_PACK = "icon_pack";
    public static final String KEY_LEFT_HANDED = "left_handed";
    public static final String KEY_FONT = "font";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_FAVORITES = "favorites";
    public static final String KEY_THEME = "theme";
    public static final String KEY_WALLPAPER = "wallpaper";
    public static final String KEY_WALLPAPER_DIM_AMOUNT = "wallpaper_dim_amount";
    public static final String KEY_NOW_PLAYING_WIDGET = "now_playing_enabled";

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
        return sPrefs.getBoolean(KEY_CHARACTER_HEADINGS, true);
    }

    public static void setIconPack(String packageName) {
        sPrefs.edit().putString(KEY_ICON_PACK, packageName).apply();
    }

    public static String getIconPack() {
        return sPrefs.getString(KEY_ICON_PACK, "System Default");
    }

    public static void setLeftHanded(boolean leftHanded) {
        sPrefs.edit().putBoolean(KEY_LEFT_HANDED, leftHanded).apply();
    }

    public static boolean getLeftHanded() {
        return sPrefs.getBoolean(KEY_LEFT_HANDED, false);
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
        return sPrefs.getInt(KEY_THEME, Configuration.UI_MODE_NIGHT_UNDEFINED);
    }

    public static void setWallpaper(String wallpaper) {
        sPrefs.edit().putString(KEY_WALLPAPER, wallpaper).apply();
    }

    public static String getWallpaper() {
        return sPrefs.getString(KEY_WALLPAPER, null);
    }

    public static void setWallpaperDimAmount(float dimAmount) {
        sPrefs.edit().putFloat(KEY_WALLPAPER_DIM_AMOUNT, dimAmount).apply();
    }

    public static float getWallpaperDimAmount() {
        return sPrefs.getFloat(KEY_WALLPAPER_DIM_AMOUNT, 0.25f);
    }

    public static void setNowPlayingEnabled(boolean enabled) {
        sPrefs.edit().putBoolean(KEY_NOW_PLAYING_WIDGET, enabled).apply();
    }

    public static boolean getNowPlayingEnabled() {
        // Disable this initially  because people need to grant access
        return sPrefs.getBoolean(KEY_NOW_PLAYING_WIDGET, false);
    }
}
