package com.chadderbox.launcherbox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.chadderbox.launcherbox.data.AppInfo;

import java.util.ArrayList;
import java.util.List;

public final class AppLoader {
    private final PackageManager mPackageManager;

    public AppLoader(Context context) {
        mPackageManager = context.getPackageManager();
    }

    public List<AppInfo> loadInstalledApps() {
        var apps = new ArrayList<AppInfo>();
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        var resolveInfos = mPackageManager.queryIntentActivities(intent, 0);
        for (var info : resolveInfos) {
            apps.add(new AppInfo(info.loadLabel(mPackageManager).toString(), info.activityInfo.packageName));
        }

        return apps;
    }
}
