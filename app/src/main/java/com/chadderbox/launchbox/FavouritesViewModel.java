package com.chadderbox.launchbox;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;

public class FavouritesViewModel extends ListItemViewModelBase {

    private final AppLoader mAppLoader;
    private final FavouritesRepository mFavouritesRepository;

    public FavouritesViewModel(
        @NonNull Application application,
        AppLoader appLoader,
        FavouritesRepository favouritesRepository,
        CombinedAdapter favouritesAdapter
    ) {
        super(application, favouritesAdapter);

        mAppLoader = appLoader;
        mFavouritesRepository = favouritesRepository;
    }

    public AppLoader getAppLoader() {
        return mAppLoader;
    }

    public FavouritesRepository getFavouritesRepository() {
        return mFavouritesRepository;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application mApp;
        private final AppLoader mLoader;
        private final FavouritesRepository mRepo;
        private final CombinedAdapter mAdapter;

        public Factory(Application app, AppLoader loader, FavouritesRepository repo, CombinedAdapter adapter) {
            mApp = app;
            mLoader = loader;
            mRepo = repo;
            mAdapter = adapter;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FavouritesViewModel.class)) {
                return (T) new FavouritesViewModel(mApp, mLoader, mRepo, mAdapter);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

