package com.chadderbox.launcherbox;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public final class MainPagerAdapter extends FragmentStateAdapter {

    private final AppListFragmentBase[] mFragments;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity, AppListFragmentBase[] fragments) {
        super(fragmentActivity);

        mFragments = fragments;
    }

    @NonNull
    @Override
    public AppListFragmentBase createFragment(int position) {
        return mFragments[position];
    }

    @Override
    public int getItemCount() {
        return mFragments.length;
    }

    public AppListFragmentBase getFragmentAt(int position) {
        return mFragments[position];
    }

    public AppListFragmentBase[] getFragments() {
        return mFragments;
    }

    public void refresh() {
        for (var fragment : mFragments) {
            fragment.refresh();
        }
    }
}
