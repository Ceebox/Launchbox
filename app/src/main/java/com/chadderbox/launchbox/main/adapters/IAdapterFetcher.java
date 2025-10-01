package com.chadderbox.launchbox.main.adapters;

import com.chadderbox.launchbox.main.fragments.AppListFragmentBase;

public interface IAdapterFetcher {
    CombinedAdapter getAdapter(Class<? extends AppListFragmentBase> fragmentClass);
}
