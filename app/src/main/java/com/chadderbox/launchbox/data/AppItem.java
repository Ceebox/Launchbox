package com.chadderbox.launchbox.data;

import android.view.View;

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
    protected void executeOpenAction(View view) {
        if (view.getContext() instanceof MainActivity app) {
            app.launchApp(mAppInfo);
        }
    }

    @Override
    protected void executeHoldAction(View view) {
        if (view.getContext() instanceof MainActivity app) {
            app.showAppMenu(view, mAppInfo);
        }
    }
}
