package com.chadderbox.launchbox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsManager;

public final class CustomFontFactory
    implements LayoutInflater.Factory2,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private static final boolean EXPERIMENT_FORCE_SETTING_FONT_SIZE = false;

    private Typeface mCurrentTypeface;
    private float mCurrentFontSize;

    private CustomFontFactory() {
        SettingsManager.registerChangeListener(this);
        updateFontSettings();
    }

    public static void initialise(Activity activity) {
        var inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.setFactory2(new CustomFontFactory());
        }
    }

    private void updateFontSettings() {
        mCurrentTypeface = FontHelper.getFont(SettingsManager.getFont());
        mCurrentFontSize = SettingsManager.getFontSize();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_FONT.equals(key) || SettingsManager.KEY_FONT_SIZE.equals(key)) {
            updateFontSettings();
        }
    }

    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = null;
        try {
            view = LayoutInflater.from(context).createView(name, null, attrs);
        } catch (ClassNotFoundException ignored) { }

        if (view instanceof TextView textView) {

            // We don't want to override the font weight for bold titles, etc
            var currentWeight = textView.getTypeface().getWeight();
            var typeface = mCurrentTypeface;
            if (currentWeight != typeface.getWeight()) {
                typeface = Typeface.create(typeface, currentWeight, typeface.isItalic());
            }

            textView.setTypeface(typeface);

            // At the moment, this generally makes everything look terrible
            if (EXPERIMENT_FORCE_SETTING_FONT_SIZE) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mCurrentFontSize);
            }

            // TODO: Make this work?
            // We need some way of programmatically setting something to be a heading
            var isHeadingResult = (Boolean) textView.getTag(R.id.isHeading);
            var overrideFontSizeResult = (Boolean) textView.getTag(R.id.overrideFontSize);

            try (var a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView)) {

                // TODO: Fix the override font stuff
                var isHeading = a.getBoolean(R.styleable.FontTextView_isHeading, false);
                var overrideFontSize = a.getBoolean(R.styleable.FontTextView_overrideFontSize, false);

                if (isHeadingResult != null) {
                    isHeading |= isHeadingResult;
                }

                if (overrideFontSizeResult != null) {
                    overrideFontSize |= overrideFontSizeResult;
                }

                if (isHeading) {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                }
            } catch (Exception ignored) { }
        }

        return view;
    }

    @Override
    public View onCreateView(View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return onCreateView(name, context, attrs);
    }
}
