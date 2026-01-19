package com.chadderbox.launchbox.main.commands;

import android.content.Intent;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.settings.SettingsActivity;

public final class OpenSettingsCommand
    implements IDialogCommand {

    @Override
    public String getName() {
        return "Launcher Settings";
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        var settingsIntent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(settingsIntent);
    }
}
