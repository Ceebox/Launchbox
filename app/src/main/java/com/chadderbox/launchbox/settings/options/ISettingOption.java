package com.chadderbox.launchbox.settings.options;

import com.chadderbox.launchbox.settings.SettingCategory;
import com.chadderbox.launchbox.settings.SettingsActivity;

@SettingCategory()
public interface ISettingOption {

    String getTitle();

    String getSubtitle(final SettingsActivity activity);

    void performClick(final SettingsActivity activity);

}
