package com.chadderbox.launchbox.data;

import android.content.Context;

import com.chadderbox.launchbox.main.MainActivity;

public final class WebItem extends ListItem {
    private final String mQuery;

    public WebItem(String query) {
        super(ListItemType.WEB);
        mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    public void performOpenAction(Context context) {
        if (context instanceof MainActivity app) {
            app.openWebQuery(mQuery);
        }
    }

    @Override
    public void performHoldAction(Context context) { }
}
