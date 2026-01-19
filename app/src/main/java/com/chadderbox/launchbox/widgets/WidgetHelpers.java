package com.chadderbox.launchbox.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class WidgetHelpers {
    private WidgetHelpers() { }

    public static Map<String, List<AppWidgetProviderInfo>> getGroupedWidgets(Activity activity, AppWidgetManager appWidgetManager) {
        var pm = activity.getPackageManager();
        var providers = appWidgetManager.getInstalledProviders();
        var grouped = new HashMap<String, List<AppWidgetProviderInfo>>();

        for (var info : providers) {
            try {
                var appInfo = pm.getApplicationInfo(info.provider.getPackageName(), 0);
                var appName = pm.getApplicationLabel(appInfo).toString();

                if (!grouped.containsKey(appName)) {
                    grouped.put(appName, new ArrayList<>());
                }

                var currentInfo = grouped.get(appName);
                if (currentInfo != null) {
                    currentInfo.add(info);
                }

            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }

        return grouped;
    }
}
