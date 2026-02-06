package com.chadderbox.launchbox.data;

import android.view.View;

import com.chadderbox.launchbox.main.MainActivity;

public final class SuggestionItem extends ListItem {
    private final String mSuggestion;

    public SuggestionItem(String suggestion) {
        super(ListItemType.SUGGESTION);
        mSuggestion = suggestion;
    }

    public String getSuggestion() {
        return mSuggestion;
    }

    @Override
    protected void executeOpenAction(View view) {
        if (view.getContext() instanceof MainActivity app) {
            app.openWebQuery(mSuggestion);
        }
    }

    @Override
    protected void executeHoldAction(View view) { }
}