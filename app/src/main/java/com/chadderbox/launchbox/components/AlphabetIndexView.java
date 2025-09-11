package com.chadderbox.launchbox.components;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.settings.SettingsManager;

public final class AlphabetIndexView extends View {
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final boolean EXPERIMENT_BUBBLE_ENABLED = false;

    private final Paint mPaint;
    private final Paint mBubblePaint;
    private IOnLetterSelectedListener mListener;

    private int mSelectedIndex = -1;
    private final float[] mLetterScales = new float[LETTERS.length()];
    private ValueAnimator mScaleAnimator;

    private final float mBubbleRadius = 60f * getResources().getDisplayMetrics().density / 3;
    private boolean mShowBubble = false;
    private float mBubbleX;
    private float mBubbleY;
    private final RectF mBubbleRect = new RectF();

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(48f * getResources().getDisplayMetrics().density / 3);
        mPaint.setTextAlign(Paint.Align.RIGHT);
        applyTextColour(context);
        applyCurrentFont();

        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // TODO: Make this customisable?
        mBubblePaint.setColor(Color.parseColor("#AAFF3535"));
        mBubblePaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < LETTERS.length(); i++) {
            mLetterScales[i] = 1f;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        var width = getWidth();
        var height = getHeight();

        var availableHeight = height - getPaddingTop() - getPaddingBottom();
        var cellHeight = (float) availableHeight / LETTERS.length();

        for (var i = 0; i < LETTERS.length(); i++) {
            float x = getWidth() - getPaddingRight();
            var y = getPaddingTop() + cellHeight * i + cellHeight / 2f + mPaint.getTextSize() / 2f;

            canvas.save();

            // Scale around center of letter
            float letterScale = mLetterScales[i];
            canvas.translate(x, y);
            canvas.scale(letterScale, letterScale);
            canvas.translate(-x, -y);

            if (i == mSelectedIndex) {
                mPaint.setAlpha(255);
                mPaint.setFakeBoldText(true);
            } else {
                mPaint.setAlpha(180);
                mPaint.setFakeBoldText(false);
            }

            canvas.drawText(String.valueOf(LETTERS.charAt(i)), x, y, mPaint);
            canvas.restore();
        }

        // Draw bubble popup if visible
        // TODO: At the moment, the text inside the bubble is weirdly aligned
        if (EXPERIMENT_BUBBLE_ENABLED && (mShowBubble && mSelectedIndex >= 0 && mSelectedIndex < LETTERS.length())) {
            char letter = LETTERS.charAt(mSelectedIndex);

            mBubbleRect.set(
                mBubbleX - mBubbleRadius,
                mBubbleY - mBubbleRadius,
                mBubbleX + mBubbleRadius,
                mBubbleY + mBubbleRadius
            );

            canvas.drawRoundRect(mBubbleRect, mBubbleRadius / 2, mBubbleRadius / 2, mBubblePaint);

            mPaint.setAlpha(255);
            mPaint.setTextSize(64f * getResources().getDisplayMetrics().density / 3);
            mPaint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(letter), mBubbleX, mBubbleY + mPaint.getTextSize() / 3, mPaint);

            mPaint.setTextSize(48f * getResources().getDisplayMetrics().density / 3);
            mPaint.setFakeBoldText(false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        var action = event.getActionMasked();
        var y = event.getY();
        var height = getHeight() - getPaddingTop() - getPaddingBottom();

        var index = (int) ((y - getPaddingTop()) / height * LETTERS.length());

        if (y < getPaddingTop() || y > getHeight() - getPaddingBottom()) {
            index = -1;
        } else {
            if (index < 0) {
                index = 0;
            }

            if (index >= LETTERS.length()) {
                index = LETTERS.length() - 1;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (index != mSelectedIndex) {
                    if (mSelectedIndex >= 0) {
                        animateLetterDeselection(mSelectedIndex);
                    }
                    if (index >= 0) {
                        animateLetterSelection(index);
                    }

                    if (index >= 0 && mListener != null) {
                        mListener.onLetterSelected(LETTERS.charAt(index));
                    }
                }

                mSelectedIndex = index;
                mShowBubble = index >= 0;

                if (mShowBubble) {
                    mBubbleX = getWidth() - getPaddingRight() - mBubbleRadius * 2.5f;
                    mBubbleY = y;
                }

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mSelectedIndex >= 0) {
                    animateLetterDeselection(mSelectedIndex);
                }

                mSelectedIndex = -1;
                mShowBubble = false;
                invalidate();
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void animateLetterSelection(int newIndex) {
        if (newIndex < 0 || newIndex >= LETTERS.length()) {
            return;
        }

        startScaleAnimation(newIndex, 1.5f);

        // Set the previously selected letter back
        if (mSelectedIndex >= 0 && mSelectedIndex != newIndex) {
            startScaleAnimation(mSelectedIndex, 1f);
        }
    }

    private void animateLetterDeselection(int index) {
        if (index < 0 || index >= LETTERS.length()) {
            return;
        }

        startScaleAnimation(index, 1f);
    }

    private void startScaleAnimation(final int index, final float targetScale) {
        if (mScaleAnimator != null && mScaleAnimator.isRunning()) {
            mScaleAnimator.cancel();
        }

        final var startScale = mLetterScales[index];
        mScaleAnimator = ValueAnimator.ofFloat(startScale, targetScale);
        mScaleAnimator.setDuration(200);
        mScaleAnimator.setInterpolator(new DecelerateInterpolator());
        mScaleAnimator.addUpdateListener(animation -> {
            mLetterScales[index] = (float) animation.getAnimatedValue();
            invalidate();
        });

        mScaleAnimator.start();
    }

    public void setOnLetterSelectedListener(IOnLetterSelectedListener listener) {
        mListener = listener;
    }

    private void applyTextColour(Context context) {
        var typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)) {
            int colour;
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                colour = typedValue.data;
            } else {
                // Colour resource reference
                colour = ContextCompat.getColor(context, typedValue.resourceId);
            }

            mPaint.setColor(colour);
        } else {
            // Fallback
            mPaint.setColor(Color.WHITE);
        }
    }

    private void applyCurrentFont() {
        // This doesn't work properly at the moment
        var fontPackage = SettingsManager.getFont();
        var typeface = Typeface.DEFAULT;

        if (!fontPackage.isEmpty()) {
            try {
                typeface = Typeface.create(fontPackage, Typeface.NORMAL);
            } catch (Exception ignored) {
                // Use default, I guess
            }
        }

        mPaint.setTypeface(typeface);
        invalidate();
    }

    public interface IOnLetterSelectedListener {
        void onLetterSelected(char letter);
    }
}
