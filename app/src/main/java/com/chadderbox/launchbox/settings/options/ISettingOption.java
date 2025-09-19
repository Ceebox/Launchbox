package com.chadderbox.launchbox.settings.options;

import com.chadderbox.launchbox.settings.SettingsActivity;

public interface ISettingOption {

    String getTitle();

    String getSubtitle(final SettingsActivity activity);

    void performClick(final SettingsActivity activity);

}
