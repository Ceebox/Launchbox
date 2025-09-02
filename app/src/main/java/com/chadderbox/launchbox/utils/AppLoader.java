package com.chadderbox.launchbox.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.chadderbox.launchbox.data.AppInfo;

import java.util.ArrayList;
import java.util.List;

public final class AppLoader {
    private final PackageManager mPackageManager;
    private final ArrayList<AppInfo> mInstalledApps = new ArrayList<>();

    public AppLoader(Context context) {
        mPackageManager = context.getPackageManager();
        refreshInstalledApps();
    }

    public void refreshInstalledApps() {
        var intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        var resolveInfos = mPackageManager.queryIntentActivities(intent, 0);
        mInstalledApps.clear();
        for (var info : resolveInfos) {
            mInstalledApps.add(new AppInfo(info.loadLabel(mPackageManager).toString(), info.activityInfo.packageName));
        }
    }

    public List<AppInfo> getInstalledApps() {
        return mInstalledApps;
    }
}
