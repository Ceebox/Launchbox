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

    public void loadFavouritesAsync(@NonNull SetFavouritesCallback callback) {
        mExecutor.execute(() -> {
            // Copy here to prevent concurrent modification
            var favourites = new HashSet<>(SettingsManager.getFavourites());
            mMainHandler.post(() -> callback.onResult(favourites));
        });
    }

    public void saveFavouritesAsync(@NonNull Set<String> newFavourites) {
        mExecutor.execute(() -> SettingsManager.setFavourites(newFavourites));
    }

    public interface SetFavouritesCallback {
        void onResult(Set<String> favorites);
    }

    public boolean hasFavourites() {
        return !SettingsManager.getFavourites().isEmpty();
    }
}
