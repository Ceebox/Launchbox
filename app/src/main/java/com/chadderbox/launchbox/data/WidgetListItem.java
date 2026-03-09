package com.chadderbox.launchbox.data;

import android.view.View;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.dialogs.CommandService;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.widgets.WidgetHostManager;
import com.chadderbox.launchbox.widgets.commands.RemoveWidgetCommand;
import com.chadderbox.launchbox.widgets.data.WidgetItem;

import java.util.ArrayList;

public class WidgetListItem extends ListItem {
    private final WidgetItem mWidgetItem;
    private boolean mActionsEnabled = true;

    public WidgetListItem(WidgetItem widgetItem) {
        super(ListItemType.WIDGET);
        mWidgetItem = widgetItem;
    }

    public WidgetItem getWidgetItem() {
        return mWidgetItem;
    }

    @Override
    public ListItemType getType() {
        return ListItemType.WIDGET;
    }

    @Override
    public void setActionsEnabled(boolean enabled) {
        mActionsEnabled = enabled;
    }

    @Override
    public boolean getActionsEnabled() {
        return mActionsEnabled;
    }

    @Override
    protected void executeOpenAction(View view) {

    }

    @Override
    protected void executeHoldAction(View view) {
        var commands = new ArrayList<IDialogCommand>();
        commands.add(new RemoveWidgetCommand(ServiceManager.getService(WidgetHostManager.class), mWidgetItem.appWidgetId));
        ServiceManager.getService(CommandService.class).showCommandDialog(
            "Edit Widget",
            commands
        );
    }
}