package com.chadderbox.launchbox.data;

import android.content.Context;

public abstract class ListItem {
    private final ListItemType mType;

    protected ListItem(ListItemType type) {
        mType = type;
    }

    public abstract void performOpenAction(Context context);

    public abstract void performHoldAction(Context context);

    public ListItemType getType() {
        return mType;
    }
}
