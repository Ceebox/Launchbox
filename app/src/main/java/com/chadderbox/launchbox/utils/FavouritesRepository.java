package com.chadderbox.launchbox.utils;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.settings.SettingsManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public final class FavouritesRepository {
    private final ExecutorService mExecutor;
    private final Handler mMainHandler;

    public FavouritesRepository(ExecutorService executor, Handler mainHandler) {
        mExecutor = executor;
        mMainHandler = mainHandler;
    }

    public boolean isFavourite(@NonNull String packageName) {
        var favourites = SettingsManager.getFavourites();
        return favourites.contains(packageName);
    }

    /**
     * Synchronously load favourites from settings
     */
    public Set<String> loadFavourites() {
        return new HashSet<>(SettingsManager.getFavourites());
    }

    // This probably needs to go
    public void loadFavouritesAsync(@NonNull SetFavouritesCallback callback) {
        mExecutor.execute(() -> {
            // Copy here to prevent concurrent modification
            var favourites = new HashSet<>(SettingsManager.getFavourites());
            mMainHandler.post(() -> callback.onResult(favourites));
        });
    }

    public void saveFavourites(@NonNull Set<String> newFavourites) {
        SettingsManager.setFavourites(newFavourites);
    }

    public boolean hasFavourites() {
        return !SettingsManager.getFavourites().isEmpty();
    }

    public interface SetFavouritesCallback {
        void onResult(Set<String> favorites);
    }
}
