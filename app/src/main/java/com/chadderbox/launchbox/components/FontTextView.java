package com.chadderbox.launchbox.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.utils.FontHelper;
import com.chadderbox.launchbox.settings.SettingsManager;

public final class FontTextView extends androidx.appcompat.widget.AppCompatTextView
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final float HEADING_SIZE_MULTIPLIER = 1.35f;
    private boolean mIsHeading = false;

    public FontTextView(Context context) {
        super(context);
        initialise(null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(attrs);
    }

    public void setIsHeading(boolean isHeading) {
        mIsHeading = isHeading;

        applyFont();
        applyTextSize();
    }

    public boolean getIsHeading() {
        return mIsHeading;
    }

    private void initialise(@Nullable AttributeSet attrs) {
        SettingsManager.registerChangeListener(this);

        if (attrs != null) {
            try (var a = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView)) {
                mIsHeading = a.getBoolean(R.styleable.FontTextView_isHeading, false);
                a.recycle();
            } catch (Exception ignored) { }
        }

        // Call this after determining if we're a heading or not
        applyFont();
        applyTextSize();
    }

    private void applyFont() {
        try {
            var fontKey = SettingsManager.getFont();
            var tf = FontHelper.getFont(fontKey);
            if (tf != null) {
                setTypeface(tf);
            }

            if (mIsHeading) {
                setTypeface(getTypeface(), Typeface.BOLD);
            }

        } catch (Exception ignored) { }
    }

    private void applyTextSize() {
        float fontSize = SettingsManager.getFontSize();
        if (mIsHeading) {
            fontSize *= HEADING_SIZE_MULTIPLIER;
        }

        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (SettingsManager.KEY_FONT.equals(key) || SettingsManager.KEY_FONT_SIZE.equals(key)) {
            applyFont();
            applyTextSize();
        }
    }
}