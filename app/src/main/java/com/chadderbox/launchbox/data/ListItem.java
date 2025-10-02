package com.chadderbox.launchbox.data;

import android.content.Context;

public abstract class ListItem {
    private final ListItemType mType;
    private boolean mActionsEnabled = true;

    protected ListItem(ListItemType type) {
        mType = type;
    }

    public final void performOpenAction(Context context) {
        if (!mActionsEnabled) {
            return;
        }

        executeOpenAction(context);
    }

    public final void performHoldAction(Context context) {
        if (!mActionsEnabled) {
            return;
        }

        executeHoldAction(context);
    }

    protected abstract void executeOpenAction(Context context);
    protected abstract  void executeHoldAction(Context context);

    public ListItemType getType() {
        return mType;
    }

    public void setActionsEnabled(boolean enabled) {
        mActionsEnabled = enabled;
    }

    public boolean getActionsEnabled() {
        return mActionsEnabled;
    }
}
