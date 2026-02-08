package com.chadderbox.launchbox.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LruCache;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.ui.TintHelper;

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

    private void loadDefaultMissingIcon() {
        if (mIconPackPackage == null) {
            mDefaultMissingIcon = null;
            return;
        }
        try {
            var res = mPackageManager.getResourcesForApplication(mIconPackPackage);

            @SuppressLint("DiscouragedApi")
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

    public Drawable loadAppIcon(final String packageName, int category) {

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

            if (icon == null) {
                var categoryDrawableName = getDrawableNameForCategory(category);
                if (categoryDrawableName != null) {
                    @SuppressLint("DiscouragedApi")
                    var resId = res.getIdentifier(categoryDrawableName, "drawable", mIconPackPackage);
                    if (resId != 0) {
                        icon = ResourcesCompat.getDrawable(res, resId, mContext.getTheme());
                    }
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

    private static String getDrawableNameForCategory(int category) {
        return switch (category) {
            case ApplicationInfo.CATEGORY_SOCIAL -> "category_social";
            case ApplicationInfo.CATEGORY_PRODUCTIVITY -> "category_productivity";
            case ApplicationInfo.CATEGORY_GAME -> "category_games";
            case ApplicationInfo.CATEGORY_NEWS -> "category_news";
            case ApplicationInfo.CATEGORY_MAPS -> "category_maps";
            case ApplicationInfo.CATEGORY_VIDEO -> "category_video";
            case ApplicationInfo.CATEGORY_AUDIO -> "category_music";
            case ApplicationInfo.CATEGORY_IMAGE -> "category_photos";
            default -> null;
        };
    }

    public static Drawable getAdaptiveIcon(String packageName, PackageManager packageManager) {
        try {
            var appInfo = packageManager.getApplicationInfo(packageName, 0);
            var icon = packageManager.getApplicationIcon(appInfo);

            if (icon instanceof AdaptiveIconDrawable) {
                // It's already adaptive, return as is
                return icon;
            } else {
                // Wrap legacy icon to make it look adaptive
                return wrapLegacyIcon(icon);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Allows correctly rendering an adaptive icon inside a square image
     * (This is because an adaptive icon extends beyond the visible area for parallax)
     */
    public static Bitmap flattenAdaptiveIcon(AdaptiveIconDrawable adaptiveIcon, int size) {
        var bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);

        // (Adaptive icons need 25% extra safe zone for layers)
        adaptiveIcon.setBounds(0, 0, size, size);
        adaptiveIcon.draw(canvas);

        return bitmap;
    }

    private static Drawable wrapLegacyIcon(Drawable legacyIcon) {
        // Create a generic background
        var background = new ColorDrawable(Color.WHITE);

        // TODO: use a LayerDrawable or a custom AdaptiveIcon wrapper to center the icon inside a shape
        return new AdaptiveIconDrawable(background, legacyIcon);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_ICON_PACK.equals(key)) {
            setIconPackPackage(SettingsManager.getIconPack());
            clearCache();
        }
    }
}
