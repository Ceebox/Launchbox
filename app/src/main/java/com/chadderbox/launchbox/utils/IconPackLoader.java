package com.chadderbox.launchbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.Map;

public final class IconPackLoader {

    private static final Map<String, Drawable> sIconCache = new HashMap<>();
    private static final Map<String, Drawable> sSystemIconCache = new HashMap<>();
    private final Context mContext;
    private final PackageManager mPackageManager;
    private String mIconPackPackage;
    private Drawable mDefaultMissingIcon = null;

    public IconPackLoader(Context ctx, String iconPackPackage) {
        mContext = ctx;
        mPackageManager = ctx.getPackageManager();
        mIconPackPackage = iconPackPackage;
        loadDefaultMissingIcon();
    }

    public static void clearCache() {
        sIconCache.clear();
    }

    public String getIconPackPackage() {
        return mIconPackPackage;
    }

    public void setIconPackPackage(String newIconPackPackage) {
        if ((mIconPackPackage == null && newIconPackPackage != null)
            || (mIconPackPackage != null && !mIconPackPackage.equals(newIconPackPackage))) {
            mIconPackPackage = newIconPackPackage;
            sIconCache.clear();
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
        if (mIconPackPackage == null) {
            return null;
        }

        if (sIconCache.containsKey(packageName)) {
            var cached = sIconCache.get(packageName);
            if (cached != null) {
                return cached;
            }
        }

        // Default
        if (mIconPackPackage.equalsIgnoreCase("System Default")) {
            if (sSystemIconCache.containsKey(packageName)) {
                var cachedSysIcon = sSystemIconCache.get(packageName);
                if (cachedSysIcon != null) {
                    return cachedSysIcon;
                }
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
            var drawableName = IconPackParser.getDrawableNameForPackage(mContext, mIconPackPackage, packageName);

            if (drawableName != null) {
                @SuppressLint("DiscouragedApi")
                var resId = res.getIdentifier(drawableName, "drawable", mIconPackPackage);
                if (resId != 0) {
                    icon = ResourcesCompat.getDrawable(res, resId, mContext.getTheme());
                }
            }
        } catch (Exception ignored) {
        }

        if (icon == null && mDefaultMissingIcon != null) {
            icon = mDefaultMissingIcon;
        }

        sIconCache.put(packageName, icon);

        return icon;
    }
}
