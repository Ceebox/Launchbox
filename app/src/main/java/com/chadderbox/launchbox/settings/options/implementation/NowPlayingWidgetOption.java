package com.chadderbox.launchbox.settings.options.implementation;

import android.content.Intent;
import android.widget.Toast;

import android.app.AlertDialog;

import com.chadderbox.launchbox.settings.SettingsActivity;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.settings.options.ISettingOption;

public final class NowPlayingWidgetOption
    implements ISettingOption {

    @Override
    public String getTitle() {
        return "Now Playing Widget";
    }

    @Override
    public String getSubtitle(final SettingsActivity activity) {
        return SettingsManager.getNowPlayingEnabled() ? "On" : "Off";
    }

    @Override
    public void performClick(final SettingsActivity activity) {
        showNowPlayingDialog(activity);
    }

    private void showNowPlayingDialog(final SettingsActivity activity) {

        if (!hasNotificationAccess(activity)) {
            requestNotificationAccess(activity);
            return;
        }

        var options = new String[] { "Off", "On" };
        var initiallyEnabled = SettingsManager.getNowPlayingEnabled();
        var checkedItem = initiallyEnabled ? 1 : 0;

        new AlertDialog.Builder(activity)
            .setTitle("Now Playing Widget")
            .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                var enabled = which == 1;
                if (enabled == initiallyEnabled) {
                    return;
                }

                SettingsManager.setNowPlayingEnabled(enabled);
                Toast.makeText(activity, "Now Playing Widget: " + (enabled ? "On" : "Off"), Toast.LENGTH_SHORT).show();

                activity.refreshOptions();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private boolean hasNotificationAccess(final SettingsActivity activity) {
        var enabledListeners = android.provider.Settings.Secure.getString(
            activity.getContentResolver(),
            "enabled_notification_listeners"
        );

        return enabledListeners != null && enabledListeners.contains(activity.getPackageName());
    }

    private void requestNotificationAccess(final SettingsActivity activity) {
        var intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        activity.startActivity(intent);
    }
}
