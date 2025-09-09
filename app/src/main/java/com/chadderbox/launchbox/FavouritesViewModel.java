package com.chadderbox.launchbox;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.data.AppItem;
import com.chadderbox.launchbox.data.HeaderItem;
import com.chadderbox.launchbox.data.ListItem;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.AppLoader;
import com.chadderbox.launchbox.utils.FavouritesRepository;

import java.util.ArrayList;
import java.util.List;

public class FavouritesViewModel extends AndroidViewModel {

    private final AppLoader mAppLoader;
    private final FavouritesRepository mFavouritesRepository;
    private final MutableLiveData<List<ListItem>> mItems = new MutableLiveData<>();

    public FavouritesViewModel(
        @NonNull Application application,
        AppLoader appLoader,
        FavouritesRepository favouritesRepository
    ) {
        super(application);
        mAppLoader = appLoader;
        mFavouritesRepository = favouritesRepository;

        loadFavourites();
    }

    public LiveData<List<ListItem>> getItems() {
        return mItems;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadFavourites() {
        var favourites = mFavouritesRepository.loadFavourites();
        var apps = mAppLoader.getInstalledApps();
        var list = new ArrayList<ListItem>();

        list.add(new HeaderItem("Favourites"));
        for (var app : apps) {
            if (favourites.contains(app.getPackageName())) {
                list.add(new AppItem(app));
            }
        }

        mItems.setValue(list);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application mApp;
        private final AppLoader mLoader;
        private final FavouritesRepository mRepo;

        public Factory(Application app, AppLoader loader, FavouritesRepository repo) {
            mApp = app;
            mLoader = loader;
            mRepo = repo;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FavouritesViewModel.class)) {
                return (T) new FavouritesViewModel(mApp, mLoader, mRepo);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
