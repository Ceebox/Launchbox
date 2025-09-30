package com.chadderbox.launchbox.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.R;
import com.chadderbox.launchbox.settings.SettingsManager;

@SuppressLint("AppCompatCustomView")
public class ShadowImageView
    extends ImageView
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final Paint mShadowPaint;
    private Paint mTintPaint;
    private Bitmap mShadowBitmap;
    private final float mShadowRadius = 2f;
    private float mShadowDx = 4f;
    private float mShadowDy = 4f;
    private Bitmap mOriginalBitmap;
    private Rect mImageBounds;
    private Rect mShadowBounds;

    public ShadowImageView(Context context) {
        this(context, null, 0);
    }

    public ShadowImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        SettingsManager.registerChangeListener(this);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_shadow));
        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));

        setTintPaint();

        var shadowStrength = SettingsManager.getShadowStrength();
        mShadowDx = shadowStrength;
        mShadowDy = shadowStrength;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        cacheOriginalBitmap(drawable);
        regenerateShadow();

        if (getWidth() > 0 && getHeight() > 0 && mOriginalBitmap != null) {
            calculateBounds(getWidth(), getHeight());
        }

        requestLayout();
        invalidate();
    }

    public void clearShadowBitmap() {
        // This is generally to handle virtualization screwing me
        // At least this isn't WPF!
        if (mShadowBitmap != null && !mShadowBitmap.isRecycled()) {
            mShadowBitmap.recycle();
        }

        mShadowBitmap = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        var extraX = (int) (mShadowRadius + Math.abs(mShadowDx));
        var extraY = (int) (mShadowRadius + Math.abs(mShadowDy));

        setMeasuredDimension(getMeasuredWidth() + extraX * 2, getMeasuredHeight() + extraY * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOriginalBitmap == null || mShadowBitmap == null) {
            super.onDraw(canvas);
            return;
        }

        var extraX = (int)(mShadowRadius + Math.abs(mShadowDx));
        var extraY = (int)(mShadowRadius + Math.abs(mShadowDy));

        canvas.save();
        canvas.translate(extraX, extraY);

        // Don't draw the shadow if we don't have one!
        if (mShadowDx != 0 || mShadowDy != 0) {
            canvas.drawBitmap(mShadowBitmap, null, mShadowBounds, mShadowPaint);
        }

        canvas.drawBitmap(mOriginalBitmap, null, mImageBounds, mTintPaint);

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOriginalBitmap == null) {
            return;
        }

        calculateBounds(w, h);
    }

    private void calculateBounds(int w, int h) {
        var extraX = (int)(mShadowRadius + Math.abs(mShadowDx));
        var extraY = (int)(mShadowRadius + Math.abs(mShadowDy));

        mImageBounds = calculateImageBounds(
            w - (2 * extraX),
            h - (2 * extraY),
            mOriginalBitmap.getWidth(),
            mOriginalBitmap.getHeight()
        );

        mShadowBounds = new Rect(
            mImageBounds.left + (int)mShadowDx,
            mImageBounds.top + (int)mShadowDy,
            mImageBounds.right + (int)mShadowDx,
            mImageBounds.bottom + (int)mShadowDy
        );
    }

    private void cacheOriginalBitmap(Drawable drawable) {
        if (drawable == null) {
            mOriginalBitmap = null;
            return;
        }

        if (drawable instanceof BitmapDrawable) {
            mOriginalBitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            var width = Math.max(1, drawable.getIntrinsicWidth());
            var height = Math.max(1, drawable.getIntrinsicHeight());
            mOriginalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            var c = new Canvas(mOriginalBitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(c);
        }
    }

    private void regenerateShadow() {
        if (mOriginalBitmap == null) {
            mShadowBitmap = null;
            return;
        }

        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
        mShadowBitmap = mOriginalBitmap.extractAlpha(mShadowPaint, null);
        mShadowPaint.setMaskFilter(null);
        mShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_shadow));
    }

    private Rect calculateImageBounds(int contentWidth, int contentHeight, int imageWidth, int imageHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Rect(0, 0, contentWidth, contentHeight);
        }

        var aspectRatioImage = (float) imageWidth / imageHeight;
        var aspectRatioView = (float) contentWidth / contentHeight;

        float scale;
        if (aspectRatioImage > aspectRatioView) {
            scale = (float) contentWidth / imageWidth;
        } else {
            scale = (float) contentHeight / imageHeight;
        }

        var scaledWidth = (int) (imageWidth * scale);
        var scaledHeight = (int) (imageHeight * scale);

        var left = (contentWidth - scaledWidth) / 2;
        var top = (contentHeight - scaledHeight) / 2;
        var right = left + scaledWidth;
        var bottom = top + scaledHeight;

        return new Rect(left, top, right, bottom);
    }

    private void setTintPaint() {
        var tintIconsMode = SettingsManager.getTintIconsMode();
        switch (tintIconsMode) {
            case SettingsManager.TINT_ICONS_DISABLED:
                mTintPaint = null;
                break;
            case SettingsManager.TINT_ICONS_MATCH_FONT:
                mTintPaint = new Paint();
                mTintPaint.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.text_primary), PorterDuff.Mode.SRC_IN));
                break;
            case SettingsManager.TINT_ICONS_PASTEL:
            case SettingsManager.TINT_ICONS_SYSTEM:
            default: break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_SHADOW_STRENGTH.equals(key)) {
            var shadowStrength = SettingsManager.getShadowStrength();
            mShadowDx = shadowStrength;
            mShadowDy = shadowStrength;
            invalidate();
        }

        if (SettingsManager.KEY_TINT_ICONS.equals(key)) {
            setTintPaint();
            invalidate();
        }
    }
}
