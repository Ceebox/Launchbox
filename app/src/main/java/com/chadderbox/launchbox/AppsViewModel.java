package com.chadderbox.launchbox;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.chadderbox.launchbox.utils.AppLoader;

public class AppsViewModel extends ListItemViewModelBase {
    private final AppLoader mAppLoader;

    public AppsViewModel(Application app, AppLoader loader, CombinedAdapter adapter) {
        super(app, adapter);
        mAppLoader = loader;
    }

    public AppLoader getAppLoader() {
        return mAppLoader;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application mApplication;
        private final AppLoader mAppLoader;
        private final CombinedAdapter mAdapter;

        public Factory(Application application, AppLoader appLoader, CombinedAdapter adapter) {
            mApplication = application;
            mAppLoader = appLoader;
            mAdapter = adapter;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AppsViewModel.class)) {
                return (T) new AppsViewModel(mApplication, mAppLoader, mAdapter);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
