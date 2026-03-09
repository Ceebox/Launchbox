package com.chadderbox.launchbox.widgets.commands;

import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public final class RemoveWidgetCommand implements IDialogCommand {

    private final WidgetHostManager mHostManager;
    private final int mAppWidgetId;

    public RemoveWidgetCommand(WidgetHostManager manager, int appWidgetId) {
        mHostManager = manager;
        mAppWidgetId = appWidgetId;
    }

    @Override
    public String getName() {
        return "Remove Widget";
    }

    @Override
    public void execute() {
        mHostManager.deleteWidget(mAppWidgetId);
    }
}
