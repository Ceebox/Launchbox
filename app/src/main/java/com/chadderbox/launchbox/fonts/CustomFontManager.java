package com.chadderbox.launchbox.fonts;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class CustomFontManager
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final float HEADING_SIZE_MULTIPLIER = 1.35f;
    private static final float OVERRIDE_FONT_SIZE_UNSET = -1f;

    private final TextView mTarget;
    private boolean mIsHeading = false;
    private float mOverrideFontSize = OVERRIDE_FONT_SIZE_UNSET;

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

    public void setOverrideFontSize(float override) {
        mOverrideFontSize = override;
        applyFontAndSize();
    }

    public float getOverrideFontSize() {
        return mOverrideFontSize;
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

        var fontSize = mOverrideFontSize == OVERRIDE_FONT_SIZE_UNSET
            ? SettingsManager.getFontSize()
            : mOverrideFontSize;

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
