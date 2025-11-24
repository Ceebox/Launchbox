package com.chadderbox.launchbox.main.controllers;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.main.MainActivity;
import com.chadderbox.launchbox.main.adapters.MainPagerAdapter;
import com.chadderbox.launchbox.main.fragments.AppListFragmentBase;
import com.chadderbox.launchbox.main.fragments.AppsFragment;
import com.chadderbox.launchbox.main.fragments.FavouritesFragment;
import com.chadderbox.launchbox.utils.FavouritesRepository;

public final class ViewPagerController {

    private final MainPagerAdapter mPagerAdapter;
    private final ViewPager2 mViewPager;
    private final MainActivity mActivity;

    public ViewPagerController(MainPagerAdapter pagerAdapter, ViewPager2 viewPager) {
        mPagerAdapter = pagerAdapter;
        mViewPager = viewPager;
        mActivity = ServiceManager.getActivity(MainActivity.class);

        refreshFavouritesFragment();

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mActivity.getFragmentController().setCurrentFragment(findPagerFragment(position));
            }
        });
    }

    public void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position);
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        mViewPager.setCurrentItem(position, smoothScroll);
    }

    public int getItemCount() {
        return mPagerAdapter.getItemCount();
    }

    public void refreshFavouritesFragment() {
        mPagerAdapter.updateVisibility(fragment -> {
            if (fragment instanceof FavouritesFragment) {
                return ServiceManager.getService(FavouritesRepository.class).hasFavourites();
            }

            return true;
        });
    }

    public Fragment findPagerFragment(final int position) {
        var itemId = mPagerAdapter.getItemId(position);
        return mActivity.getSupportFragmentManager().findFragmentByTag("f" + itemId);
    }

    public void scrollToFavourites() {
        var firstFragment = findPagerFragment(0);
        var activity = ServiceManager.getActivity(MainActivity.class);
        var fragmentController = activity.getFragmentController();
        var currentFragment = fragmentController.getCurrentFragment();
        if (currentFragment != firstFragment) {
            setCurrentItem(0, true);
            fragmentController.setCurrentFragment(firstFragment);

            if (currentFragment instanceof AppsFragment appsFragment) {
                appsFragment.scrollToPosition(0);
            }
        }
    }

    public void refreshAllVisibleFragments() {
        for (var i = 0; i < getItemCount(); i++) {
            var fragment = findPagerFragment(i);
            if (fragment instanceof AppListFragmentBase base) {
                base.refresh();
            }
        }
    }

    public void enterEditMode() {
        for (var i = 0; i < getItemCount(); i++) {
            var fragment = findPagerFragment(i);
            if (fragment instanceof FavouritesFragment favourites) {
                favourites.enterEditMode();
            }
        }
    }

    public void exitEditMode() {
        for (var i = 0; i < getItemCount(); i++) {
            var fragment = findPagerFragment(i);
            if (fragment instanceof FavouritesFragment favourites) {
                favourites.exitEditMode();
            }
        }
    }
}
