package com.chadderbox.launchbox.data;

import android.content.Intent;

public final class SettingItem extends ListItem {
    private final String mTitle;
    private final Intent mIntent;

    public SettingItem(String title, Intent intent) {
        super(ListItemType.SETTING);
        mTitle = title;
        mIntent = intent;
    }

    public String getTitle() {
        return mTitle;
    }

    public Intent getIntent() {
        return mIntent;
    }
}
