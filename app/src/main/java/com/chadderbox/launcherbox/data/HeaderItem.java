package com.chadderbox.launcherbox.data;

public final class HeaderItem extends ListItem {
    private final String mTitle;

    public HeaderItem(String title) {
        super(ListItemType.HEADER);
        mTitle = title;
    }

    public String getHeader() {
        return mTitle;
    }
}