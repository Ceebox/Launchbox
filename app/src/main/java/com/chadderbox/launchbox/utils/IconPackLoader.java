package com.chadderbox.launchbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.LruCache;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public final class IconPackLoader {

    private static final LruCache<String, Drawable> sIconCache = new LruCache<>(256);
    private static final LruCache<String, Drawable> sSystemIconCache = new LruCache<>(256);
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
        sIconCache.evictAll();
    }

    public String getIconPackPackage() {
        return mIconPackPackage;
    }

    public void setIconPackPackage(String newIconPackPackage) {
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
        if (mIconPackPackage == null) {
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

        icon = TintHelper.tryTintIcon(mContext, icon);

        if (icon == null && mDefaultMissingIcon != null) {
            icon = mDefaultMissingIcon;
        }

        sIconCache.put(packageName, icon);

        return icon;
    }
}
