package com.chadderbox.launchbox.fonts;

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

        var fontFile = new File(key);
        if (!fontFile.exists()) {
            // Since we don't have an exact match, try to find the font
            var fontDir = new File("/system/fonts/");
            var files = fontDir.listFiles();
            if (files != null) {
                for (var file : files) {
                    var name = file.getName().toLowerCase();
                    var searchKey = key.toLowerCase();

                    if (name.startsWith(searchKey)) {
                        fontFile = file;
                        if (name.contains("regular")) {
                            break;
                        }
                    }
                }
            }
        }

        if (fontFile.exists()) {
            try {
                var newTypeface = Typeface.createFromFile(fontFile);
                sFontCache.put(key, newTypeface);
                return newTypeface;
            } catch (Exception e) {
                return Typeface.DEFAULT;
            }
        }

        return Typeface.DEFAULT;
    }
}