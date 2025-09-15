package com.chadderbox.launchbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import androidx.core.content.res.ResourcesCompat;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class IconPackLoader
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final LruCache<String, Drawable> sIconCache = new LruCache<>(256);
    private static final LruCache<String, Drawable> sSystemIconCache = new LruCache<>(256);
    private final Context mContext;
    private final IconPackParser mIconPackParser;
    private final PackageManager mPackageManager;
    private String mIconPackPackage;
    private Drawable mDefaultMissingIcon = null;

    public IconPackLoader(Context ctx, String iconPackPackage) {
        mContext = ctx;
        mIconPackParser = new IconPackParser();
        mPackageManager = ctx.getPackageManager();
        mIconPackPackage = iconPackPackage;
        loadDefaultMissingIcon();

        SettingsManager.registerChangeListener(this);
    }

    public static void clearCache() {
        sIconCache.evictAll();
    }

    private void setIconPackPackage(String newIconPackPackage) {
        if ((mIconPackPackage == null && newIconPackPackage != null)
            || (mIconPackPackage != null && !mIconPackPackage.equals(newIconPackPackage))) {
            mIconPackPackage = newIconPackPackage;
            sIconCache.evictAll();
            loadDefaultMissingIcon();
        }
    }

    @SuppressLint("DiscouragedApi")
    private void loadDefaultMissingIcon() {
        if (mIconPackPackage == null) {
            mDefaultMissingIcon = null;
            return;
        }
        try {
            var res = mPackageManager.getResourcesForApplication(mIconPackPackage);
            var resId = res.getIdentifier("icon_missing", "drawable", mIconPackPackage);
            if (resId != 0) {
                mDefaultMissingIcon = ResourcesCompat.getDrawable(res, resId, mContext.getTheme());
            } else {
                mDefaultMissingIcon = null;
            }
        } catch (Exception e) {
            mDefaultMissingIcon = null;
        }
    }

    public Drawable loadAppIcon(String packageName) {

        // No icon pack
        if (mIconPackPackage == null || "None".equals(mIconPackPackage)) {
            return null;
        }

        var cached = sIconCache.get(packageName);
        if (cached != null) {
            return cached;
        }

        // Default
        if (mIconPackPackage.equalsIgnoreCase("System Default")) {
            var cachedSysIcon = sSystemIconCache.get(packageName);
            if (cachedSysIcon != null) {
                return cachedSysIcon;
            }
            try {
                var systemIcon = mPackageManager.getApplicationIcon(packageName);
                sSystemIconCache.put(packageName, systemIcon);
                return systemIcon;
            } catch (PackageManager.NameNotFoundException ignored) { }
        }

        Drawable icon = null;
        try {
            var res = mPackageManager.getResourcesForApplication(mIconPackPackage);
            var drawableName = mIconPackParser.getDrawableNameForPackage(mContext, mIconPackPackage, packageName);

            if (drawableName != null) {
                @SuppressLint("DiscouragedApi")
                var resId = res.getIdentifier(drawableName, "drawable", mIconPackPackage);
                if (resId != 0) {
                    icon = ResourcesCompat.getDrawable(res, resId, mContext.getTheme());
                }
            }
        } catch (Exception ignored) {
        }

        icon = TintHelper.tryTintIcon(mContext, icon);

        if (icon == null && mDefaultMissingIcon != null) {
            icon = mDefaultMissingIcon;
        }

        sIconCache.put(packageName, icon);

        return icon;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_ICON_PACK.equals(key)) {
            setIconPackPackage(SettingsManager.getIconPack());
            clearCache();
        }
    }
}
