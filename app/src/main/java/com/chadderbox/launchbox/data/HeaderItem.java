package com.chadderbox.launchbox.data;

import android.content.Context;

public final class HeaderItem extends ListItem {
    private final String mTitle;

    public HeaderItem(String title) {
        super(ListItemType.HEADER);
        mTitle = title;
    }

    public String getHeader() {
        return mTitle;
    }

    @Override
    public void executeOpenAction(Context context) { }

    @Override
    protected void executeHoldAction(Context context) { }
}