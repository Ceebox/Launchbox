package com.chadderbox.launchbox.data;

import android.content.Intent;
import android.view.View;

import com.chadderbox.launchbox.main.MainActivity;

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
    protected void executeOpenAction(View view) {
        if (view.getContext() instanceof MainActivity app) {
            app.openSetting(this);
        }
    }

    @Override
    protected void executeHoldAction(View view) { }
}
