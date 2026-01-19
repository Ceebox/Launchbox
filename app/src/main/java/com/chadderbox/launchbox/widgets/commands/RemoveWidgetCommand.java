package com.chadderbox.launchbox.widgets.commands;

import android.view.ViewGroup;

import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.ui.components.ResizableWidgetFrame;
import com.chadderbox.launchbox.widgets.WidgetHostManager;
import com.chadderbox.launchbox.widgets.data.WidgetItem;

import java.util.concurrent.Executors;

public class RemoveWidgetCommand implements IDialogCommand {

    private final WidgetHostManager mHostManager;
    private final ResizableWidgetFrame mFrame;
    private final int mAppWidgetId;

    public RemoveWidgetCommand(WidgetHostManager manager, ResizableWidgetFrame frame, int appWidgetId) {
        mHostManager = manager;
        mFrame = frame;
        mAppWidgetId = appWidgetId;
    }

    @Override
    public String getName() {
        return "Remove Widget";
    }

    @Override
    public void execute() {
        mHostManager.removeWidget(mFrame, mAppWidgetId);
    }
}
