package com.chadderbox.launchbox;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public final class MainPagerAdapter extends FragmentStateAdapter {

    private final List<AppListFragmentBase> mFragments;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<AppListFragmentBase> fragments) {
        super(fragmentActivity);

        mFragments = fragments;
    }

    @NonNull
    @Override
    public AppListFragmentBase createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFragments.get(position).hashCode();
    }

    @Override
    public boolean containsItem(long id) {
        for (var fragment : mFragments) {
            if (fragment.hashCode() == id) {
                return true;
            }
        }

        return false;
    }

    public void addFragment(AppListFragmentBase fragment, int position) {
        mFragments.add(position, fragment);
        notifyItemInserted(position);
    }

    public void removeFragment(int position) {
        mFragments.remove(position);
        notifyItemRemoved(position);
    }

    public void removeFragment(AppListFragmentBase item) {
        var position = mFragments.indexOf(item);
        mFragments.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public AppListFragmentBase getFragmentAt(int position) {
        return mFragments.get(position);
    }

    public List<AppListFragmentBase> getFragments() {
        return mFragments;
    }

    public void refresh() {
        for (var fragment : mFragments) {
            fragment.refresh();
        }
    }
}
