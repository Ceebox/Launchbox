package com.chadderbox.launchbox.main.commands;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.utils.HiddenAppsRepository;

public final class ToggleHideCommand
    implements IDialogCommand {

    private final AppInfo mAppInfo;
    private final boolean mIsHidden;

    public ToggleHideCommand(AppInfo appInfo, boolean isHidden) {
        mAppInfo = appInfo;
        mIsHidden = isHidden;
    }

    @Override
    public String getName() {
        return mIsHidden
            ? "Unhide " + mAppInfo.getLabel()
            : "Hide " + mAppInfo.getLabel();
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        var hiddenAppsHelper = ServiceManager.getService(HiddenAppsRepository.class);
        var hiddenApps = hiddenAppsHelper.loadHiddenApps();

        if (hiddenApps.contains(mAppInfo.getPackageName())) {
            hiddenApps.remove(mAppInfo.getPackageName());
        } else {
            hiddenApps.add(mAppInfo.getPackageName());
        }

        hiddenAppsHelper.saveHiddenApps(hiddenApps);

        var viewPagerController = activity.getViewPagerController();
        ServiceManager.getMainHandler().post(viewPagerController::refreshAllVisibleFragments);
    }
}
