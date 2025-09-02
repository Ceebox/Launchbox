package com.chadderbox.launcherbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

public final class IconPackParser {
    private static final Map<String, String> sCache = new HashMap<>();

    public static void clearCache() {
        sCache.clear();
    }

    public static String getDrawableNameForPackage(Context ctx, String iconPackPkg, String targetPkg) {
        if (sCache.containsKey(targetPkg)) {
            return sCache.get(targetPkg);
        }

        try {
            var pm = ctx.getPackageManager();
            var res = pm.getResourcesForApplication(iconPackPkg);

            @SuppressLint("DiscouragedApi") // We need this because it ain't ours!
            var xmlId = res.getIdentifier("appfilter", "xml", iconPackPkg);
            if (xmlId == 0) {
                return null;
            }

            var parser = res.getXml(xmlId);
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("item")) {
                    var component = parser.getAttributeValue(null, "component");
                    var drawable = parser.getAttributeValue(null, "drawable");

                    if (component != null && component.contains(targetPkg)) {
                        sCache.put(targetPkg, drawable);
                        return drawable;
                    }
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
