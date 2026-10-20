package com.chadderbox.launchbox.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class FavouritesRepository {

    private final IFavouriteAccessor mAccessor;

    public FavouritesRepository(IFavouriteAccessor accessor) {
        mAccessor = accessor;
    }

    /**
     * Determine if a specific package is the favourites.
     */
    public boolean isFavourite(@NonNull String packageName) {
        return mAccessor
            .getFavourites()
            .contains(packageName);
    }

    /**
     * Synchronously load favourites from settings.
     */
    public List<String> loadFavourites() {
        return new ArrayList<>(mAccessor.getFavourites());
    }

    /**
     * Save a new set of favourites to the settings.
    */
    public void saveFavourites(@NonNull List<String> newFavourites) {
        mAccessor.setFavourites(newFavourites);
    }

    /**
     * Determine if there are any favourites saved.
     */
    public boolean hasFavourites() {
        return !mAccessor.getFavourites().isEmpty();
    }
}
