package com.chadderbox.launchbox.main.commands;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.data.AppInfo;
import com.chadderbox.launchbox.dialogs.IDialogCommand;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.utils.FavouritesRepository;

public final class ToggleFavouriteCommand
    implements IDialogCommand {

    private final AppInfo mAppInfo;
    private final boolean mIsFavourite;

    public ToggleFavouriteCommand(AppInfo appInfo, boolean isFavourite) {
        mAppInfo = appInfo;
        mIsFavourite = isFavourite;
    }

    @Override
    public String getName() {
        return mIsFavourite
            ? "Unfavourite " + mAppInfo.getLabel()
            : "Favourite " + mAppInfo.getLabel();
    }

    @Override
    public void execute() {
        var activity = ServiceManager.getActivity(MainActivity.class);
        var favouritesHelper = ServiceManager.getService(FavouritesRepository.class);
        favouritesHelper.loadFavouritesAsync(currentFavourites -> {
            if (currentFavourites.contains(mAppInfo.getPackageName())) {
                currentFavourites.remove(mAppInfo.getPackageName());
            } else {
                currentFavourites.add(mAppInfo.getPackageName());
            }

            favouritesHelper.saveFavourites(currentFavourites);

            var viewPagerController = activity.getViewPagerController();
            viewPagerController.refreshFavouritesFragment();

            ServiceManager.getMainHandler().post(viewPagerController::refreshAllVisibleFragments);
        });
    }
}
