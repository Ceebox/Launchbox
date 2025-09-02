package com.chadderbox.launchbox.data;

public final class WebItem extends ListItem {
    private final String query;

    public WebItem(String query) {
        super(ListItemType.WEB);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
