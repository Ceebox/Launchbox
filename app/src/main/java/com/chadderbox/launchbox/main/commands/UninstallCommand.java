package com.chadderbox.launchbox.main.commands;

import android.content.Intent;
import android.net.Uri;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.main.MainActivity;

public final class UninstallCommand
    implements IDialogCommand {

    private final AppInfo mAppInfo;

    public UninstallCommand(AppInfo appInfo) {
        mAppInfo = appInfo;
    }

    @Override
    public String getName() {
        return "Uninstall " + mAppInfo.getLabel();
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        var packageUri = Uri.parse("package:" + mAppInfo.getPackageName());
        var uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(uninstallIntent);
    }
}
