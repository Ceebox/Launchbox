package com.chadderbox.launchbox.data;

public final class AppInfo {
    private final String mLabel;
    private final String mPackageName;

    public AppInfo(String label, String packageName) {
        mLabel = label;
        mPackageName = packageName;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPackageName() {
        return mPackageName;
    }
}
