package com.chadderbox.launchbox.data;

import android.view.View;

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
    protected void executeOpenAction(View view) {
        if (view.getContext() instanceof MainActivity app) {
            app.openWebQuery(mQuery);
        }
    }

    @Override
    protected void executeHoldAction(View view) { }
}
