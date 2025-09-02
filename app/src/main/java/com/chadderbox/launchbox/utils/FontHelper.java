package com.chadderbox.launchbox.utils;

import android.graphics.Typeface;
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
}