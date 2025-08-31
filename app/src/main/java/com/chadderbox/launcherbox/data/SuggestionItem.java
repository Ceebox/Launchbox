package com.chadderbox.launcherbox.data;

public final class SuggestionItem extends ListItem {
    private final String mSuggestion;

    public SuggestionItem(String suggestion) {
        super(ListItemType.SUGGESTION);
        mSuggestion = suggestion;
    }

    public String getSuggestion() {
        return mSuggestion;
    }
}