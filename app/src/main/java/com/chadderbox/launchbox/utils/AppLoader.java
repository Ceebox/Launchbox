package com.chadderbox.launchbox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.chadderbox.launchbox.data.AppInfo;

import java.util.ArrayList;
import java.util.List;

public final class AppLoader {
    private final PackageManager mPackageManager;
    private final AppAliasProvider mAliasProvider;
    private final ArrayList<AppInfo> mInstalledApps = new ArrayList<>();

    public AppLoader(Context context, AppAliasProvider appAliasProvider) {
        mPackageManager = context.getPackageManager();
        mAliasProvider = appAliasProvider;
        refreshInstalledApps();
    }

    public void refreshInstalledApps() {
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        var resolveInfos = mPackageManager.queryIntentActivities(intent, 0);
        mInstalledApps.clear();
        for (var info : resolveInfos) {
            var label = info.loadLabel(mPackageManager).toString();
            var packageName = info.activityInfo.packageName;

            var category = ApplicationInfo.CATEGORY_UNDEFINED;
            try {
                var appInfo = mPackageManager.getApplicationInfo(packageName, 0);
                category = appInfo.category;
            } catch (PackageManager.NameNotFoundException ignored) { }

            var alias = mAliasProvider.getAlias(packageName);
            mInstalledApps.add(new AppInfo(label, packageName, category, alias));
        }
    }

    public List<AppInfo> getInstalledApps() {
        return mInstalledApps;
    }
}
