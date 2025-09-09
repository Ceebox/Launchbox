package com.chadderbox.launchbox;

public interface IAdapterFetcher {
    CombinedAdapter getAdapter(Class<? extends AppListFragmentBase> fragmentClass);
}
