package com.chadderbox.launchbox.settings.options;

import com.chadderbox.launchbox.settings.SettingsActivity;

public final class GenericSettingOption
    implements ISettingOption {

    private final String mTitle;
    private final ISubtitleProvider mSubtitleProvider;
    private final IClickAction mClickAction;

    public GenericSettingOption(
        String title,
        ISubtitleProvider subtitleProvider,
        IClickAction clickAction
    ) {
        mTitle = title;
        mSubtitleProvider = subtitleProvider;
        mClickAction = clickAction;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return mSubtitleProvider.getSubtitle(activity);
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        mClickAction.onClick(activity);
    }
}