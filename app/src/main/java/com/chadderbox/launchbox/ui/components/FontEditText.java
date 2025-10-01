package com.chadderbox.launchbox.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.fonts.CustomFontManager;

@SuppressLint("AppCompatCustomView")
public final class FontEditText extends EditText {

    private static final int DEFAULT_STYLE_ATTR = R.attr.fontEditTextStyle;
    private static final int DEFAULT_STYLE_RES = R.style.Theme_Launcherbox_FontEditText;

    private final CustomFontManager mFontManager;

    public FontEditText(Context context) {
        this(context, null);
    }

    public FontEditText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, DEFAULT_STYLE_ATTR);
    }

    public FontEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, DEFAULT_STYLE_RES);
        mFontManager = new CustomFontManager(this);
        initialise(attrs, defStyleAttr);
    }

    private void initialise(@Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            try (@SuppressLint("CustomViewStyleable") var a = getContext().obtainStyledAttributes(attrs, R.styleable.FontEditText, defStyleAttr, DEFAULT_STYLE_RES)) {
                mFontManager.setIsHeading(a.getBoolean(R.styleable.FontEditText_isHeading, false));
            } catch (Exception ignored) { }
        }
    }
}
