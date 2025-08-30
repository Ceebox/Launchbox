package com.chadderbox.launcherbox.components;

import android.content.Context;
import android.util.AttributeSet;

import com.chadderbox.launcherbox.utils.FontHelper;
import com.chadderbox.launcherbox.settings.SettingsManager;

public final class FontTextView extends androidx.appcompat.widget.AppCompatTextView {

    public FontTextView(Context context) {
        super(context);
        applyFont();
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyFont();
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyFont();
    }

    private void applyFont() {
        try {
            var fontKey = SettingsManager.getFont();
            var tf = FontHelper.getFont(fontKey);
            if (tf != null) {
                setTypeface(tf);
            }
        } catch (Exception ignored) { }
    }
}