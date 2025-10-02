package com.chadderbox.launchbox.data;

import android.content.Context;

import com.chadderbox.launchbox.main.MainActivity;

public final class AppItem extends ListItem {
    private final AppInfo mAppInfo;

    public AppItem(AppInfo appInfo) {
        super(ListItemType.APP);
        mAppInfo = appInfo;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    @Override
    protected void executeOpenAction(Context context) {
        if (context instanceof MainActivity app) {
            app.launchApp(mAppInfo);
        }
    }

    @Override
    protected void executeHoldAction(Context context) {
        if (context instanceof MainActivity app) {
            app.showAppMenu(mAppInfo);
        }
    }
}
