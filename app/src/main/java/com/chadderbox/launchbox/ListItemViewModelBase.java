package com.chadderbox.launchbox;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jspecify.annotations.NonNull;

public abstract class ListItemViewModelBase extends AndroidViewModel {
    protected MutableLiveData<CombinedAdapter> mAdapterData = new MutableLiveData<>();

    public ListItemViewModelBase(@NonNull Application application, CombinedAdapter adapter)
    {
        super(application);
        mAdapterData.setValue(adapter);
    }

    public LiveData<CombinedAdapter> getAdapter() {
        return mAdapterData;
    }
}
