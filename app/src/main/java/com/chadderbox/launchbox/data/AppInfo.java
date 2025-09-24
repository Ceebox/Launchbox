package com.chadderbox.launchbox.data;

public final class AppInfo {
    private final String mLabel;
    private final String mPackageName;
    private final int mCategory;

    public AppInfo(String label, String packageName, int category) {
        mLabel = label;
        mPackageName = packageName;
        mCategory = category;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getCategory() {
        return mCategory;
    }
}
