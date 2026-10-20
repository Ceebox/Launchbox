package com.chadderbox.launchbox.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.chadderbox.launchbox.settings.SettingsManager;

import org.xmlpull.v1.XmlPullParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class IconPackParser
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final Map<String, String> sPackageToDrawableCache = new ConcurrentHashMap<>();
    private static volatile String sLoadedIconPack = null;
    private static final Object sLoadLock = new Object();

    public IconPackParser() {
        SettingsManager.registerChangeListener(this);
    }

    public String getDrawableNameForPackage(Context ctx, String iconPackPkg, String targetPkg) {
        ensureIndexLoaded(ctx, iconPackPkg);
        return sPackageToDrawableCache.get(targetPkg);
    }

    public void clearCache() {
        synchronized (sLoadLock) {
            sPackageToDrawableCache.clear();
            sLoadedIconPack = null;
        }
    }

    private void ensureIndexLoaded(Context ctx, String iconPackPkg) {
        if (iconPackPkg == null || iconPackPkg.equals(sLoadedIconPack)) {
            return;
        }

        synchronized (sLoadLock) {
            if (iconPackPkg.equals(sLoadedIconPack)) {
                return;
            }

            sPackageToDrawableCache.clear();
            try {
                var pm = ctx.getPackageManager();
                var res = pm.getResourcesForApplication(iconPackPkg);

                @SuppressLint("DiscouragedApi") // We need this because it ain't ours!
                var xmlId = res.getIdentifier("appfilter", "xml", iconPackPkg);
                if (xmlId == 0) {
                    return;
                }

                var parser = res.getXml(xmlId);
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                        var component = parser.getAttributeValue(null, "component");
                        var drawable = parser.getAttributeValue(null, "drawable");

                        if (component != null && drawable != null) {
                            var targetPkg = extractPackageFromComponent(component);
                            if (targetPkg != null) {
                                sPackageToDrawableCache.putIfAbsent(targetPkg, drawable);
                            }
                        }
                    }
                }
            } catch (Exception ignored) { }

            sLoadedIconPack = iconPackPkg;
        }
    }

    private static String extractPackageFromComponent(String component) {
        var start = component.indexOf('{');
        var slash = component.indexOf('/');
        if (start != -1 && slash != -1 && slash > start + 1) {
            return component.substring(start + 1, slash);
        }
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_ICON_PACK.equals(key)) {
            clearCache();
        }
    }
}
