package com.chadderbox.launchbox.data;

import android.content.Context;
import android.content.Intent;

import com.chadderbox.launchbox.MainActivity;

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

    @Override
    public void performOpenAction(Context context) {
        if (context instanceof MainActivity app) {
            app.openSetting(this);
        }
    }

    @Override
    public void performHoldAction(Context context) { }
}
