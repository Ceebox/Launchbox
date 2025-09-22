package com.chadderbox.launchbox.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.utils.ThemeHelper;

@SuppressLint("AppCompatCustomView")
public final class FontEditText extends EditText {

    private final CustomFontManager mFontManager;

    public FontEditText(Context context) {
        super(context);
        mFontManager = new CustomFontManager(this);
        initialise(null);
    }

    public FontEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mFontManager = new CustomFontManager(this);
        initialise(attrs);
    }

    public FontEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFontManager = new CustomFontManager(this);
        initialise(attrs);
    }

    private void initialise(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            // I can't see myself ever doing this, and I'm not actually sure if it works lol
            try (@SuppressLint("CustomViewStyleable") var a = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView)) {
                mFontManager.setIsHeading(a.getBoolean(R.styleable.FontTextView_isHeading, false));
            } catch (Exception ignored) { }
        }

        setTextColor(ThemeHelper.resolveColorAttr(getContext(), android.R.attr.textColorPrimary));
    }
}
