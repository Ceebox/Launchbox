package com.chadderbox.launchbox.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public final class TintHelper {

    private TintHelper() { }

    public static int getTintColour(Context ctx) {

        // This might need updating in the future if I decide to add O-LED backgrounds
        // This used to be com.google.android.material.R.attr.colorOnBackground
        var typedValue = new TypedValue();
        boolean found = ctx.getTheme().resolveAttribute(
            android.R.attr.textColorPrimary,
            typedValue,
            true
        );

        int colour;
        if (found) {
            if (typedValue.resourceId != 0) {
                colour = ContextCompat.getColor(ctx, typedValue.resourceId);
            } else {
                colour = typedValue.data;
            }
        } else {
            colour = 0xFFFFFFFF;
        }

        return colour;
    }

    public static Drawable tryTintIcon(Context ctx, Drawable icon) {
        if (icon instanceof VectorDrawable || icon instanceof VectorDrawableCompat) {

            // Don't change other instances
            icon = icon.mutate();
            Drawable wrapped = DrawableCompat.wrap(icon);

            // Try to tint the icon to match our font colour
            DrawableCompat.setTint(wrapped, getTintColour(ctx));
            return wrapped;
        }

        return icon;
    }

}
