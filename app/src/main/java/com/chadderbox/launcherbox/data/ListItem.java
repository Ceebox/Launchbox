package com.chadderbox.launcherbox.data;

public abstract class ListItem {
    private final ListItemType mType;

    protected ListItem(ListItemType type) {
        mType = type;
    }

    public ListItemType getType() {
        return mType;
    }

}