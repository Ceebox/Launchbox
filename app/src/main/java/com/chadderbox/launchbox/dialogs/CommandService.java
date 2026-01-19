package com.chadderbox.launchbox.dialogs;

import android.app.Activity;

import com.chadderbox.launchbox.R;

import java.util.List;

public final class CommandService {
    private final Activity mContext;

    public CommandService(Activity context) {
        mContext = context;
    }

    public void showCommandMenu(String title, List<IDialogCommand> commands) {
        var options = commands.stream().map(IDialogCommand::getName).toArray(String[]::new);
        new android.app.AlertDialog.Builder(mContext, R.style.Theme_Launcherbox_Dialog)
            .setTitle(title)
            .setItems(options, (dialog, which) -> commands.get(which).execute())
            .show();
    }

    public void showCommandMenu(String title, IDialogCommand... commands) {
        var options = java.util.Arrays.stream(commands)
            .map(IDialogCommand::getName)
            .toArray(String[]::new);

        new android.app.AlertDialog.Builder(mContext, R.style.Theme_Launcherbox_Dialog)
            .setTitle(title)
            .setItems(options, (dialog, which) -> commands[which].execute())
            .show();
    }
}
