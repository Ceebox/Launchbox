package com.chadderbox.launchbox;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public final class MainPagerAdapter extends FragmentStateAdapter {

    private final List<Class<? extends AppListFragmentBase>> mFragmentClasses;
    private final List<Class<? extends AppListFragmentBase>> mVisibleFragmentClasses;

    public MainPagerAdapter(@NonNull FragmentManager fragmentManager, Lifecycle lifecycle, List<Class<? extends AppListFragmentBase>> fragmentClasses) {
        super(fragmentManager, lifecycle);
        mFragmentClasses = new ArrayList<>(fragmentClasses);
        mVisibleFragmentClasses = new ArrayList<>(fragmentClasses);
    }

    @NonNull
    @Override
    public AppListFragmentBase createFragment(int position) {
        try {
            return mVisibleFragmentClasses.get(position).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create fragment", e);
        }
    }

    @Override
    public long getItemId(int position) {
        return mVisibleFragmentClasses.get(position).getName().hashCode();
    }

    @Override
    public boolean containsItem(long id) {
        for (var cls : mVisibleFragmentClasses) {
            if (cls.getName().hashCode() == id) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return mVisibleFragmentClasses.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateVisibility(IFragmentVisiblePredicate predicate) {
        mVisibleFragmentClasses.clear();
        for (var cls : mFragmentClasses) {
            try {
                var instance = cls.getConstructor().newInstance();
                if (predicate.shouldShow(instance)) {
                    mVisibleFragmentClasses.add(cls);
                }
            } catch (Exception e) {
                // Skip if we can't instantiate it
                // TODO: Maybe log this?
            }
        }
        notifyDataSetChanged();
    }

    public interface IFragmentVisiblePredicate {
        boolean shouldShow(AppListFragmentBase fragment);
    }
}
