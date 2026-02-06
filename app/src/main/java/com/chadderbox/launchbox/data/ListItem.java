package com.chadderbox.launchbox.data;

import android.view.View;

public abstract class ListItem {
    private final ListItemType mType;
    private boolean mActionsEnabled = true;

    protected ListItem(ListItemType type) {
        mType = type;
    }

    public final void performOpenAction(View view) {
        if (!mActionsEnabled) {
            return;
        }

        executeOpenAction(view);
    }

    public final void performHoldAction(View view) {
        if (!mActionsEnabled) {
            return;
        }

        executeHoldAction(view);
    }

    protected abstract void executeOpenAction(View view);
    protected abstract void executeHoldAction(View view);

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
