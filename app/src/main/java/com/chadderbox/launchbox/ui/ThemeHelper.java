package com.chadderbox.launchbox.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class ThemeHelper {

    private ThemeHelper() { }

    /** Resolves a color from the current theme; falls back to black if not found. */
    public static int resolveColorAttr(Context context, int attr) {
        return resolveColorAttr(context, attr, 0xFF000000);
    }

    /** Resolves a color from the current theme. */
    public static int resolveColorAttr(Context context, int attr, int fallback) {
        try (var a = context.obtainStyledAttributes(new int[] { attr })) {
            return a.getColor(0, fallback);
        }
    }

    /** Set the theme configuration on a context. */
    public static Context getContextWithTheme(Context context) {
        var overrideConfig = new Configuration();
        overrideConfig.uiMode = SettingsManager.getTheme();

        @SuppressLint("ObsoleteSdkInt")
        var contextWithTheme = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
            ? context.createConfigurationContext(overrideConfig)
            : context;

        return contextWithTheme;
    }

}
