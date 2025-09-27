package com.chadderbox.launchbox.utils;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.R;

public final class ShadowHelper {

    private ShadowHelper() { }

    public static void setShadow(TextView paint, float radius, float dx, float dy, int colourResource) {
        paint.setShadowLayer(radius, dx, dy, colourResource);
    }

    public static void applySettings(TextView item) {
        var shadowRadius = 2f;
        var shadowDx = 4f;
        var shadowDy = 4f;
        var shadowColor = ContextCompat.getColor(item.getContext(), R.color.text_shadow);

        setShadow(item, shadowRadius, shadowDx, shadowDy, shadowColor);
    }

}
