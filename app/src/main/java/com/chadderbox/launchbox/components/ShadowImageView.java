package com.chadderbox.launchbox.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.R;

@SuppressLint("AppCompatCustomView")
public class ShadowImageView extends ImageView {

    private Bitmap mShadowBitmap;
    private Paint mShadowPaint;
    private float mShadowRadius = 2f;
    private float mShadowDx = 4f;
    private float mShadowDy = 4f;

    public ShadowImageView(Context context) {
        this(context, null, 0);
    }

    public ShadowImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void clearShadowBitmap() {
        // This is generally to handle virtualization screwing me
        // At least this isn't WPF!
        if (mShadowBitmap != null && !mShadowBitmap.isRecycled()) {
            mShadowBitmap.recycle();
        }

        mShadowBitmap = null;
    }

    private void init() {
        // Needed for BlurMaskFilter
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_shadow));
        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        var extraX = (int) (mShadowRadius + Math.abs(mShadowDx));
        var extraY = (int) (mShadowRadius + Math.abs(mShadowDy));

        setMeasuredDimension(getMeasuredWidth() + extraX * 2, getMeasuredHeight() + extraY * 2);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        var drawable = getDrawable();
        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }

        Bitmap originalBitmap;
        if (drawable instanceof BitmapDrawable) {
            originalBitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            // We don't have it easy here
            // Render to another bitmap that we can use
            var width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : getWidth();
            var height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : getHeight();

            originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            var bitmapCanvas = new Canvas(originalBitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(bitmapCanvas);
        }

        if (mShadowBitmap == null || mShadowBitmap.isRecycled()) {
            generateShadowBitmap(originalBitmap);
        }

        var saveCount = canvas.save();
        var extraX = (int)(mShadowRadius + Math.abs(mShadowDx));
        var extraY = (int)(mShadowRadius + Math.abs(mShadowDy));
        canvas.translate(extraX, extraY);

        var imageBounds = calculateImageBounds(
            getWidth() - (2 * extraX),
            getHeight() - (2 * extraY),
            originalBitmap.getWidth(),
            originalBitmap.getHeight()
        );

        @SuppressLint("DrawAllocation") Rect shadowBounds = new Rect(
            imageBounds.left + (int) mShadowDx,
            imageBounds.top + (int) mShadowDy,
            imageBounds.right + (int) mShadowDx,
            imageBounds.bottom + (int) mShadowDy
        );

        canvas.drawBitmap(mShadowBitmap, null, shadowBounds, mShadowPaint);
        canvas.drawBitmap(originalBitmap, null, imageBounds, null);

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        clearShadowBitmap();
        invalidate();
    }

    private void generateShadowBitmap(Bitmap original) {
        if (mShadowBitmap != null && !mShadowBitmap.isRecycled()) {
            mShadowBitmap.recycle();
        }

        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
        mShadowBitmap = original.extractAlpha(mShadowPaint, null);
        mShadowPaint.setMaskFilter(null);
        mShadowPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_shadow));
    }

    private Rect calculateImageBounds2(int viewWidth, int viewHeight, int imageWidth, int imageHeight) {
        // Compute image bounds respecting scaleType and padding (simplified version handles centerCrop, fitCenter etc. needed)

        // For simplicity, handle scaleType CENTER_INSIDE (adjust as needed)
        float scale;
        int left, top, right, bottom;

        if (imageWidth <= 0 || imageHeight <= 0) {
            // Avoid division by zero
            return new Rect(0, 0, viewWidth, viewHeight);
        }

        var aspectRatioImage = (float) imageWidth / imageHeight;
        var aspectRatioView = (float) (viewWidth - getPaddingLeft() - getPaddingRight()) / (viewHeight - getPaddingTop() - getPaddingBottom());

        if (aspectRatioImage > aspectRatioView) {
            scale = (float) (viewWidth - getPaddingLeft() - getPaddingRight()) / imageWidth;
        } else {
            scale = (float) (viewHeight - getPaddingTop() - getPaddingBottom()) / imageHeight;
        }

        var scaledWidth = (int) (imageWidth * scale);
        var scaledHeight = (int) (imageHeight * scale);

        left = getPaddingLeft() + (viewWidth - getPaddingLeft() - getPaddingRight() - scaledWidth) / 2;
        top = getPaddingTop() + (viewHeight - getPaddingTop() - getPaddingBottom() - scaledHeight) / 2;
        right = left + scaledWidth;
        bottom = top + scaledHeight;

        return new Rect(left, top, right, bottom);
    }

    private Rect calculateImageBounds(int contentWidth, int contentHeight, int imageWidth, int imageHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Rect(0, 0, contentWidth, contentHeight);
        }

        float aspectRatioImage = (float) imageWidth / imageHeight;
        float aspectRatioView = (float) contentWidth / contentHeight;

        float scale;
        if (aspectRatioImage > aspectRatioView) {
            scale = (float) contentWidth / imageWidth;
        } else {
            scale = (float) contentHeight / imageHeight;
        }

        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);

        int left = (contentWidth - scaledWidth) / 2;
        int top = (contentHeight - scaledHeight) / 2;
        int right = left + scaledWidth;
        int bottom = top + scaledHeight;

        return new Rect(left, top, right, bottom);
    }

    public void setShadowRadius(float radius) {
        mShadowRadius = radius;
        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowRadius, BlurMaskFilter.Blur.NORMAL));
        mShadowBitmap = null;
        requestLayout();
        invalidate();
    }

    public void setShadowOffset(float dx, float dy) {
        mShadowDx = dx;
        mShadowDy = dy;
        mShadowBitmap = null;
        requestLayout();
        invalidate();
    }
}
