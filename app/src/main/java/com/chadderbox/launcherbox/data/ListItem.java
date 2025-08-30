package com.chadderbox.launcherbox.data;

public final class ListItem {
    private final ListItemType mType;
    public final String mHeaderTitle;
    private final AppInfo mAppInfo;

    public ListItem(String headerTitle) {
        mType = ListItemType.HEADER;
        mHeaderTitle = headerTitle;
        mAppInfo = null;
    }

    public ListItem(AppInfo appInfo) {
        mType = ListItemType.APP;
        mHeaderTitle = null;
        mAppInfo = appInfo;
    }

    public ListItemType getType() {
        return mType;
    }

    public String getHeader() {
        return mHeaderTitle;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    public boolean isApp() {
        return mType == ListItemType.APP;
    }
}