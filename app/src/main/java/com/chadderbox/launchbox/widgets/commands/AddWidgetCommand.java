package com.chadderbox.launchbox.widgets.commands;

import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public final class AddWidgetCommand implements IDialogCommand {

    private final WidgetHostManager mWidgetManager;

    public AddWidgetCommand(WidgetHostManager widgetManager) {
        mWidgetManager = widgetManager;
    }

    @Override
    public String getName() {
        return "Add Widget";
    }

    @Override
    public void execute() {
        mWidgetManager.showCustomWidgetPicker();
    }
}
