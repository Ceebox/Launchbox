package com.chadderbox.launchbox.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.chadderbox.launchbox.R;

public final class FontTextView extends AppCompatTextView {

    private final CustomFontManager mFontManager;

    public FontTextView(Context context) {
        super(context);
        mFontManager = new CustomFontManager(this);
        initialise(null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFontManager = new CustomFontManager(this);
        initialise(attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFontManager = new CustomFontManager(this);
        initialise(attrs);
    }

    private void initialise(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            try (var a = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView)) {
                mFontManager.setIsHeading(a.getBoolean(R.styleable.FontTextView_isHeading, false));
            } catch (Exception ignored) { }

            var shouldOverride = false;
            try (var a = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView)) {
                shouldOverride = a.getBoolean(R.styleable.FontTextView_overrideFontSize, false);
            } catch (Exception ignored) { }

            if (shouldOverride) {
                try (var a = getContext().obtainStyledAttributes(attrs, new int[] { android.R.attr.textSize })) {
                    if (a.hasValue(0)) {
                        var pxSize = a.getDimensionPixelSize(0, (int) getTextSize());
                        var spSize = TypedValue.deriveDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            pxSize,
                            getResources().getDisplayMetrics()
                        );

                        mFontManager.setOverrideFontSize(spSize);
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    public void setIsHeading(boolean isHeading) {
        mFontManager.setIsHeading(isHeading);
    }
}
