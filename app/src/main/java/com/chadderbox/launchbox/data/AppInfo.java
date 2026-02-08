package com.chadderbox.launchbox.data;

import android.content.pm.ApplicationInfo;

public final class AppInfo {
    private final String mName;
    private final String mPackageName;
    private final int mCategory;
    private String mAlias;

    public AppInfo(String name, String packageName, int category, String alias) {
        mName = name;
        mPackageName = packageName;
        mAlias = alias;
        mCategory = category == ApplicationInfo.CATEGORY_UNDEFINED ?
            inferCategory(packageName)
            : category;
    }

    public String getLabel() {
        if (mAlias == null) {
            return mName;
        }

        return mAlias;
    }

    public String getName() {
        return mName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getCategory() {
        return mCategory;
    }

    public String getAlias() {
        return mAlias;
    }

    public void setAlias(String newAlias) {
        mAlias = newAlias;
    }

    private static int inferCategory(String packageName) {
        if (packageName.contains("game")) return ApplicationInfo.CATEGORY_GAME;
        if (packageName.contains("social") || packageName.contains("messenger")) return ApplicationInfo.CATEGORY_SOCIAL;
        if (packageName.contains("music") || packageName.contains("audio")) return ApplicationInfo.CATEGORY_AUDIO;
        if (packageName.contains("video") || packageName.contains("player")) return ApplicationInfo.CATEGORY_VIDEO;
        if (packageName.contains("map") || packageName.contains("navigation")) return ApplicationInfo.CATEGORY_MAPS;
        return ApplicationInfo.CATEGORY_UNDEFINED;
    }
}
