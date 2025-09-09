package com.chadderbox.launchbox;

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
import com.chadderbox.launchbox.utils.AppLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsViewModel extends AndroidViewModel {

    private final AppLoader mAppLoader;
    private final MutableLiveData<List<ListItem>> mItems = new MutableLiveData<>();

    public AppsViewModel(@NonNull Application app, AppLoader loader) {
        super(app);
        mAppLoader = loader;
        loadApps();
    }

    public AppLoader getAppLoader() {
        return mAppLoader;
    }

    public LiveData<List<ListItem>> getItems() {
        return mItems;
    }

    public void loadApps() {
        var apps = mAppLoader.getInstalledApps();
        if (apps == null) {
            apps = Collections.emptyList();
        }

        apps.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        var list = new ArrayList<ListItem>();
        list.add(new HeaderItem("Apps"));

        for (var app : apps) {
            list.add(new AppItem(app));
        }

        mItems.setValue(list);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application mApp;
        private final AppLoader mLoader;

        public Factory(Application app, AppLoader loader) {
            mApp = app;
            mLoader = loader;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AppsViewModel.class)) {
                return (T) new AppsViewModel(mApp, mLoader);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}