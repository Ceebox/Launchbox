package com.chadderbox.launchbox.utils;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.settings.SettingsManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public final class FavouritesRepository {
    private final ExecutorService mExecutor;
    private final Handler mMainHandler;

    public FavouritesRepository(ExecutorService executor) {
        // TODO: This probably needs moving over to the DB, and combining with hidden
        mExecutor = executor;
        mMainHandler = ServiceManager.getMainHandler();
    }

    public boolean isFavourite(@NonNull String packageName) {
        var favourites = SettingsManager.getFavourites();
        return favourites.contains(packageName);
    }

    /**
     * Synchronously load favourites from settings
     */
    public List<String> loadFavourites() {
        return new ArrayList<>(SettingsManager.getFavourites());
    }

    // This probably needs to go
    public void loadFavouritesAsync(@NonNull SetFavouritesCallback callback) {
        mExecutor.execute(() -> {
            // Copy here to prevent concurrent modification
            var favourites = new ArrayList<>(SettingsManager.getFavourites());
            mMainHandler.post(() -> callback.onResult(favourites));
        });
    }

    public void saveFavourites(@NonNull List<String> newFavourites) {
        SettingsManager.setFavourites(newFavourites);
    }

    public boolean hasFavourites() {
        return !SettingsManager.getFavourites().isEmpty();
    }

    public interface SetFavouritesCallback {
        void onResult(List<String> favorites);
    }
}
