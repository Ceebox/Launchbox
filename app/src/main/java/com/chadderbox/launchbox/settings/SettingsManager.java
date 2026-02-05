package com.chadderbox.launchbox.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.IntDef;

import org.json.JSONArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SettingsManager {
    public static final String PREFS_NAME = "cbxlauncher_prefs";
    public static final String KEY_APP_ALIAS_PREFIX = "app_alias_";
    public static final String KEY_FAVORITES = "favorites";
    public static final String KEY_CHARACTER_HEADINGS = "character_headings";
    public static final String KEY_ICON_PACK = "icon_pack";
    public static final String KEY_SHOW_ONLY_INSTALLED = "show_only_installed";
    public static final String KEY_LEFT_HANDED = "left_handed";
    public static final String KEY_FONT = "font";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_HIDDEN = "hidden";
    public static final String KEY_SHADOW_STRENGTH = "shadow_strength";
    public static final String KEY_THEME = "theme";
    public static final String KEY_TINT_ICONS = "tint_icons";
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

    public static void setAppAlias(String packageName, String newAlias) {
        sPrefs.edit().putString(KEY_APP_ALIAS_PREFIX + packageName, newAlias).apply();
    }

    public static String getAppAlias(String packageName) {
        return sPrefs.getString(KEY_APP_ALIAS_PREFIX + packageName, null);
    }

    public static void setFavourites(List<String> favourites) {
        var json = new JSONArray();
        for (var f : favourites) {
            json.put(f);
        }

        sPrefs.edit().putString(KEY_FAVORITES, json.toString()).apply();
    }

    public static List<String> getFavourites() {
        var jsonString = sPrefs.getString(KEY_FAVORITES, "[]");
        var list = new ArrayList<String>();

        try {
            var array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (Exception ignored) {}

        return list;
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

    public static void setShowOnlyInstalled(boolean showOnlyInstalled) {
        sPrefs.edit().putBoolean(KEY_SHOW_ONLY_INSTALLED, showOnlyInstalled).apply();
    }

    public static boolean getShowOnlyInstalled() {
        return sPrefs.getBoolean(KEY_SHOW_ONLY_INSTALLED, true);
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

    public static void setFontSize(int newSize) {
        sPrefs.edit().putInt(KEY_FONT_SIZE, newSize).apply();
    }

    public static int getFontSize() {
        return sPrefs.getInt(KEY_FONT_SIZE, 16);
    }

    public static List<String> getHidden() {
        var jsonString = sPrefs.getString(KEY_HIDDEN, "[]");
        var list = new ArrayList<String>();

        try {
            var array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (Exception ignored) {}

        return list;
    }

    public static void setHidden(List<String> hidden) {
        var json = new JSONArray();
        for (var h : hidden) {
            json.put(h);
        }

        sPrefs.edit().putString(KEY_HIDDEN, json.toString()).apply();
    }

    public static void setShadowStrength(int newStrength) {
        sPrefs.edit().putInt(KEY_SHADOW_STRENGTH, newStrength).apply();
    }

    public static int getShadowStrength() {
        return sPrefs.getInt(KEY_SHADOW_STRENGTH, 3);
    }

    public static void setTheme(int mode) {
        sPrefs.edit().putInt(KEY_THEME, mode).apply();
    }

    public static int getTheme() {
        return sPrefs.getInt(KEY_THEME, Configuration.UI_MODE_NIGHT_UNDEFINED);
    }

    public static void setTintIconsMode(@TintIconsMode int tintIconsMode) {
        sPrefs.edit().putInt(KEY_TINT_ICONS, tintIconsMode).apply();
    }

    public static @TintIconsMode int getTintIconsMode() {
        return sPrefs.getInt(KEY_TINT_ICONS, TINT_ICONS_DISABLED);
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

    //<editor-fold desc="Parameters">
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TINT_ICONS_DISABLED, TINT_ICONS_MATCH_FONT, TINT_ICONS_SYSTEM, TINT_ICONS_PASTEL})
    public @interface TintIconsMode { }

    public static final int TINT_ICONS_DISABLED = 0;
    public static final int TINT_ICONS_MATCH_FONT = 1;
    public static final int TINT_ICONS_SYSTEM = 2;
    public static final int TINT_ICONS_PASTEL = 4;
    //</editor-fold>
}
