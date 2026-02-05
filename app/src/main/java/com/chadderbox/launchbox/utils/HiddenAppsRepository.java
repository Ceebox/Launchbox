package com.chadderbox.launchbox.utils;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public final class HiddenAppsRepository {
    public HiddenAppsRepository() { }

    public boolean isHidden(@NonNull String packageName) {
        var favourites = SettingsManager.getHidden();
        return favourites.contains(packageName);
    }

    /**
     * Synchronously load hidden apps from settings
     */
    public List<String> loadHiddenApps() {
        return new ArrayList<>(SettingsManager.getHidden());
    }

    public void saveHiddenApps(@NonNull List<String> hiddenAppPackageNames) {
        SettingsManager.setHidden(hiddenAppPackageNames);
    }
}
