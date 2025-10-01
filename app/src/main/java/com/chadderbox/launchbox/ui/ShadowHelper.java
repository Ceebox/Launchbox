package com.chadderbox.launchbox.ui;

import android.content.Context;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsManager;

public final class ShadowHelper {

    private ShadowHelper() { }

    public static void setShadow(Paint paint, float radius, float dx, float dy, int colourResource) {
        paint.setShadowLayer(radius, dx, dy, colourResource);
    }

    public static void applySettings(Context context, Paint paint) {

        var shadowRadius = 2f;
        var shadowStrength = SettingsManager.getShadowStrength();
        if (shadowStrength == 0) {
            shadowRadius = 0f;
        }

        var shadowColor = ContextCompat.getColor(context, R.color.text_shadow);
        setShadow(paint, shadowRadius, shadowStrength, shadowStrength, shadowColor);
    }

}
