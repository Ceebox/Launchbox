package com.chadderbox.launchbox.widgets.commands;

import android.appwidget.AppWidgetProviderInfo;

import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public class ConfigureWidgetCommand implements IDialogCommand {

    private final WidgetHostManager mManager;
    private final AppWidgetProviderInfo mInfo;
    private final int mAppWidgetId;

    public ConfigureWidgetCommand(WidgetHostManager manager, AppWidgetProviderInfo info, int appWidgetId) {
        mManager = manager;
        mInfo = info;
        mAppWidgetId = appWidgetId;
    }

    @Override
    public String getName() {
        return "Edit Widget";
    }

    @Override
    public void execute() {
        mManager.startWidgetConfiguration(mInfo, mAppWidgetId);
    }
}
