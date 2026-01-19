package com.chadderbox.launchbox.main.commands;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.main.MainActivity;

public final class EnterEditModeCommand
    implements IDialogCommand {

    @Override
    public String getName() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        return activity.isEditMode() ? "Exit Edit Mode" : "Enter Edit Mode";
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        if (!activity.isEditMode()) {
            activity.enterEditMode();
        } else {
            activity.exitEditMode();
        }
    }
}
