package com.chadderbox.launchbox.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class FontHelper {

    private static final Map<String, Typeface> sFontCache = new HashMap<>();

    public static Typeface getFont(String key) {
        if (key == null || key.equals("default")) {
            return Typeface.DEFAULT;
        }

        // TODO: At some point, actually support loading external fonts
        // Check if we're loading a font from a file
        if (sFontCache.containsKey(key)) {
            return sFontCache.get(key);
        }

        switch (key.toLowerCase()) {
            case "sans-serif": return Typeface.SANS_SERIF;
            case "serif": return Typeface.SERIF;
            case "monospace": return Typeface.MONOSPACE;
            default: break;
        }

        var f = new File(key);
        if (f.exists()) {
            var newTypeface = Typeface.createFromFile(f);
            sFontCache.put(key, newTypeface);
            return newTypeface;
        }

        return Typeface.DEFAULT;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        var width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1;
        var height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1;

        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}