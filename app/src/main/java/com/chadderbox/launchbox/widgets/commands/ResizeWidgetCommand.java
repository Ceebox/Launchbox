package com.chadderbox.launchbox.widgets.commands;

import android.appwidget.AppWidgetProviderInfo;

import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.ui.components.ResizableWidgetFrame;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public class ResizeWidgetCommand implements IDialogCommand {
    private final WidgetHostManager mWidgetManager;
    private final ResizableWidgetFrame mFrame;
    private final int mWidgetId;

    public ResizeWidgetCommand(WidgetHostManager widgetManager, ResizableWidgetFrame frame, int appWidgetId) {
        mWidgetManager = widgetManager;
        mFrame = frame;
        mWidgetId = appWidgetId;
    }

    @Override
    public String getName() {
        return "Resize Widget";
    }

    @Override
    public void execute() {
        mWidgetManager.setWidgetsResizing(true);
    }
}
