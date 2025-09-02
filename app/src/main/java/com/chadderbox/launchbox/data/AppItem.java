package com.chadderbox.launchbox.data;

public final class AppItem extends ListItem {
    private final AppInfo mAppInfo;

    public AppItem(AppInfo appInfo) {
        super(ListItemType.APP);
        mAppInfo = appInfo;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }
}
