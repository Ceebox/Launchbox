package com.chadderbox.launchbox.settings;

import android.content.Context;

public final class SettingOption {
    private final String mTitle;
    private final ISubtitleProvider mSubtitleProvider;
    private final IClickAction mClickAction;

    public SettingOption(
        String title,
        ISubtitleProvider subtitleProvider,
        IClickAction clickAction
    ) {
        mTitle = title;
        mSubtitleProvider = subtitleProvider;
        mClickAction = clickAction;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle(Context context) {
        return mSubtitleProvider.getSubtitle(context);
    }

    public void performClick(Context context) {
        mClickAction.onClick(context);
    }
}