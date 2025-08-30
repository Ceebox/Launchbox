package com.chadderbox.launcherbox.settings;

import android.content.Context;

public final class SettingOption {
    private final String mTitle;
    private final SubtitleProvider mSubtitleProvider;
    private final ClickAction mClickAction;

    public interface SubtitleProvider {
        String getSubtitle(Context context);
    }

    public interface ClickAction {
        void onClick(Context context);
    }

    public SettingOption(String title,
                         SubtitleProvider subtitleProvider,
                         ClickAction clickAction) {
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