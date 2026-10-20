package com.chadderbox.launchbox.utils;

import com.chadderbox.launchbox.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SettingsFavouritesAccessor implements IFavouriteAccessor{

    private List<String> mCache = new ArrayList<>();

    public SettingsFavouritesAccessor() {
    }

    @Override
    public List<String> getFavourites() {
        if(mCache.isEmpty()) {
            mCache = SettingsManager.getFavourites();
        }

        return mCache;
    }

    @Override
    public void setFavourites(List<String> favourites) {
        mCache = favourites;
        SettingsManager.setFavourites(favourites);
    }
}
