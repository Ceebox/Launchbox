package com.chadderbox.launchbox.data;

import android.content.Context;

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
    protected void executeOpenAction(Context context) {
        if (context instanceof MainActivity app) {
            app.openWebQuery(mSuggestion);
        }
    }

    @Override
    protected void executeHoldAction(Context context) { }
}