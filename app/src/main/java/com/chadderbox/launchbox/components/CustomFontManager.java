package com.chadderbox.launchbox.components;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.utils.FontHelper;

public final class CustomFontManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final float HEADING_SIZE_MULTIPLIER = 1.35f;

    private final TextView mTarget;
    private boolean mIsHeading = false;

    public CustomFontManager(TextView target) {
        mTarget = target;
        SettingsManager.registerChangeListener(this);
        applyFontAndSize();
    }

    public void setIsHeading(boolean isHeading) {
        mIsHeading = isHeading;
        applyFontAndSize();
    }

    public boolean getIsHeading() {
        return mIsHeading;
    }

    public void applyFontAndSize() {
        applyFont();
        applyTextSize();
    }

    private void applyFont() {
        try {
            var fontKey = SettingsManager.getFont();
            var tf = FontHelper.getFont(fontKey);
            if (tf != null) {
                mTarget.setTypeface(tf);
            }

            if (mIsHeading) {
                mTarget.setTypeface(mTarget.getTypeface(), Typeface.BOLD);
            }

        } catch (Exception ignored) { }
    }

    private void applyTextSize() {
        var fontSize = SettingsManager.getFontSize();
        if (mIsHeading) {
            fontSize *= HEADING_SIZE_MULTIPLIER;
        }

        mTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_FONT.equals(key) || SettingsManager.KEY_FONT_SIZE.equals(key)) {
            applyFontAndSize();
        }
    }
}
