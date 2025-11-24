package com.chadderbox.launchbox.main.commands;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.main.MainActivity;

public final class EnterEditModeCommand
    implements IDialogCommand {

    @Override
    public String getName() {
        return "Enter Edit Mode";
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        activity.enterEditMode();
    }
}
