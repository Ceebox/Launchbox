package com.chadderbox.launchbox.main.controllers;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.chadderbox.launchbox.core.ServiceManager;
import com.chadderbox.launchbox.main.fragments.AppsFragment;
import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.ui.components.AlphabetIndexView;
import com.chadderbox.launchbox.utils.AppLoader;

import java.util.HashSet;

public final class AlphabetViewController {

    private final ViewPagerController mViewPagerController;
    private final AlphabetIndexView mIndexView;

    public AlphabetViewController(ViewPagerController viewPagerController, AlphabetIndexView indexView) {
        mViewPagerController = viewPagerController;
        mIndexView = indexView;

        mIndexView.setOnLetterSelectedListener(letter -> {
            var position = letter == AlphabetIndexView.FAVOURITES_CHARACTER
                ? 0
                : mViewPagerController.getItemCount() - 1;

            mViewPagerController.setCurrentItem(position);

            var fragment = mViewPagerController.findPagerFragment(position);
            if (fragment instanceof AppsFragment appsFragment) {
                appsFragment.scrollToLetter(letter);
            }
        });

        populateAlphabetViewLetters();
    }

    public void populateAlphabetViewLetters() {
        if (SettingsManager.getShowOnlyInstalled()) {
            mIndexView.setLetters(getAlphabetViewLetters());
        } else {
            mIndexView.setLetters(AlphabetIndexView.LETTERS);
        }
    }

    public String getAlphabetViewLetters() {
        var appLoader = ServiceManager.getService(AppLoader.class);
        var chars = new HashSet<Character>();
        var apps = appLoader.getInstalledApps();

        for (var app : apps) {
            var label = app.getLabel();
            if (label == null || label.isEmpty()) {
                continue;
            }

            var character = label.charAt(0);
            if (Character.isLetter(character)) {
                character = Character.toUpperCase(character);
            } else if (Character.isDigit(character)) {
                character = AlphabetIndexView.NUMBER_CHARACTER;
            } else {
                continue;
            }

            chars.add(character);
        }

        var newChars = chars.stream().sorted().toList();
        var result = new StringBuilder();
        result.append(AlphabetIndexView.FAVOURITES_CHARACTER);
        for (var character : newChars) {
            result.append(character);
        }

        return result.toString();
    }

    public void setVisible(boolean visible) {
        mIndexView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
