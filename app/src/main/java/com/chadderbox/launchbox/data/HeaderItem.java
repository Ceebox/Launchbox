package com.chadderbox.launchbox.data;

import android.content.Context;

import com.chadderbox.launchbox.MainActivity;

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
    public void performOpenAction(Context context) { }

    @Override
    public void performHoldAction(Context context) { }
}