package com.chadderbox.launchbox.data;

public final class AppInfo {
    private final String mName;
    private final String mPackageName;
    private final int mCategory;
    private String mAlias;

    public AppInfo(String name, String packageName, int category, String alias) {
        mName = name;
        mPackageName = packageName;
        mCategory = category;
        mAlias = alias;
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
}
