package com.chadderbox.launchbox.dialogs;

import android.app.Activity;
import android.view.View;

import com.chadderbox.launchbox.R;

import java.util.List;

public final class CommandService {
    private final Activity mContext;

    public CommandService(Activity context) {
        mContext = context;
    }

    public void showCommandDialog(String title, List<IDialogCommand> commands) {
        buildAndShowDialog(title, commands.toArray(new IDialogCommand[0]));
    }

    public void showCommandDialog(String title, IDialogCommand... commands) {
        buildAndShowDialog(title, commands);
    }

    public void showCommandPopup(View anchor, List<IDialogCommand> commands) {
        buildAndShowPopup(anchor, commands.toArray(new IDialogCommand[0]));
    }

    public void showCommandPopup(View anchor, IDialogCommand... commands) {
        buildAndShowPopup(anchor, commands);
    }

    private void buildAndShowDialog(String title, IDialogCommand[] commands) {
        var options = java.util.Arrays.stream(commands)
            .map(IDialogCommand::getName)
            .toArray(String[]::new);

        new android.app.AlertDialog.Builder(mContext, R.style.Theme_Launcherbox_Dialog)
            .setTitle(title)
            .setItems(options, (dialog, which) -> commands[which].execute())
            .show();
    }

    private void buildAndShowPopup(View anchor, IDialogCommand[] commands) {
        var popup = new android.widget.PopupMenu(mContext, anchor);
        var menu = popup.getMenu();

        for (var i = 0; i < commands.length; i++) {
            menu.add(0, i, 0, commands[i].getName());
        }

        popup.setOnMenuItemClickListener(item -> {
            commands[item.getItemId()].execute();
            return true;
        });

        popup.show();
    }
}
