package com.chadderbox.launchbox.data;

import android.view.View;

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
    public void executeOpenAction(View view) { }

    @Override
    protected void executeHoldAction(View view) { }
}