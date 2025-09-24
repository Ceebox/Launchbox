package com.chadderbox.launchbox.utils;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class AppAliasProvider {

    public AppAliasProvider() { }

    public String getAlias(String packageName) {
        return SettingsManager.getAppAlias(packageName);
    }

    public String getAlias(String packageName, String defaultName) {
        var foundAlias = SettingsManager.getAppAlias(packageName);
        return foundAlias == null ? defaultName : foundAlias;
    }

    public void setAlias(String packageName, String alias) {
        SettingsManager.setAppAlias(packageName, alias);
    }
}
