package com.chadderbox.launchbox.main.controllers;

import androidx.fragment.app.Fragment;

public final class FragmentController {
    private Fragment mCurrentFragment;

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public void setCurrentFragment(Fragment fragment) {
        mCurrentFragment = fragment;
    }

}
