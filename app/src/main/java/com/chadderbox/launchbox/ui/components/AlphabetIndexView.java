package com.chadderbox.launchbox.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.chadderbox.launchbox.settings.SettingsManager;
import com.chadderbox.launchbox.fonts.FontHelper;
import com.chadderbox.launchbox.ui.ShadowHelper;

import java.util.HashMap;
import java.util.Map;

public final class AlphabetIndexView
    extends View
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final char FAVOURITES_CHARACTER = '*';
    public static final char NUMBER_CHARACTER = '#';
    public static final String LETTERS = String.valueOf(FAVOURITES_CHARACTER)
        + NUMBER_CHARACTER
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final boolean EXPERIMENT_BUBBLE_ENABLED = false;
    private static final int ANIMATION_DURATION = 185;
    private static final float ANIMATION_ORIGINAL_SIZE = 1f;
    private static final float ANIMATION_SCALED_SIZE = 1.55f;
    private static final float ANIMATION_TEXT_OFFSET = 90f;
    private final Map<Integer, ValueAnimator> mAnimators = new HashMap<>();
    private final HashMap<Character, Float> mLetterPositions = new HashMap<>(LETTERS.length());
    private final HashMap<Character, Float> mLetterScales = new HashMap<>(LETTERS.length());
    private final Paint mPaint;
    private int mSelectedIndex = -1;
    private boolean mLeftHanded = false;
    private IOnLetterSelectedListener mListener;
    private final Paint mBubblePaint;
    private final float mBubbleRadius = 60f * getResources().getDisplayMetrics().density / 3;
    private boolean mShowBubble = false;
    private float mBubbleX;
    private float mBubbleY;
    private final RectF mBubbleRect = new RectF();
    private String mLetters = LETTERS;

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);

        for (var i = 0; i < LETTERS.length(); i++) {
            final char character = LETTERS.charAt(i);
            mLetterPositions.put(character, 0f);
            mLetterScales.put(character, ANIMATION_ORIGINAL_SIZE);
        }

        SettingsManager.registerChangeListener(this);
        mLeftHanded = SettingsManager.getLeftHanded();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(48f * getResources().getDisplayMetrics().density / 3);
        mPaint.setTextAlign(mLeftHanded ? Paint.Align.LEFT : Paint.Align.RIGHT);

        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setTextAlign(Paint.Align.CENTER);
        // TODO: Make this customisable?
        mBubblePaint.setColor(Color.parseColor("#AAFF3535"));
        mBubblePaint.setStyle(Paint.Style.FILL);

        applyTextColour(context);
        applyCurrentFont();
        resetLetterScale();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        adjustPositionForHandedness();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        var availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        var cellHeight = (float) availableHeight / mLetters.length();

        for (var i = 0; i < mLetters.length(); i++) {
            final var x = mLeftHanded ? getPaddingLeft() : getWidth() - getPaddingRight();
            final var y = getPaddingTop() + cellHeight * i + cellHeight / 2f + mPaint.getTextSize() / 2f;

            canvas.save();

            // Scale around center of letter
            var letterScale = mLetterScales.get(LETTERS.charAt(i));
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

            canvas.drawText(String.valueOf(mLetters.charAt(i)), x + mLetterPositions.get(LETTERS.charAt(i)), y, mPaint);
            canvas.restore();
        }

        // Draw bubble popup if visible
        // TODO: At the moment, the text inside the bubble is weirdly aligned
        if (EXPERIMENT_BUBBLE_ENABLED && (mShowBubble && mSelectedIndex >= 0 && mSelectedIndex < mLetters.length())) {
            var letter = mLetters.charAt(mSelectedIndex);
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

            mPaint.setTextSize(54f * getResources().getDisplayMetrics().density / 3);
            mPaint.setFakeBoldText(false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        var action = event.getActionMasked();
        var y = event.getY();
        var height = getHeight() - getPaddingTop() - getPaddingBottom();

        var index = (int) ((y - getPaddingTop()) / height * mLetters.length());

        if (y < getPaddingTop() || y > getHeight() - getPaddingBottom()) {
            index = -1;
        } else {
            if (index < 0) {
                index = 0;
            }

            if (index >= mLetters.length()) {
                index = mLetters.length() - 1;
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
                        mListener.onLetterSelected(mLetters.charAt(index));
                    }
                }

                mSelectedIndex = index;
                mShowBubble = index >= 0;

                if (mShowBubble) {
                    mBubbleX = mLeftHanded
                        ? getPaddingLeft() + mBubbleRadius * 2.5f
                        : getWidth() - getPaddingRight() - mBubbleRadius * 2.5f;
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

    public void setLetters(String letters) {
        mLetters = letters;
    }

    private void animateLetterSelection(int newIndex) {
        if (newIndex < 0 || newIndex >= mLetters.length()) {
            return;
        }

        startSelectionAnimation(newIndex, ANIMATION_TEXT_OFFSET * (mLeftHanded ? 1 : -1), ANIMATION_SCALED_SIZE);

        // Set the previously selected letter back
        if (mSelectedIndex >= 0 && mSelectedIndex != newIndex) {
            startSelectionAnimation(mSelectedIndex, 0, ANIMATION_ORIGINAL_SIZE);
        }
    }

    private void animateLetterDeselection(int index) {
        if (index < 0 || index >= mLetters.length()) {
            return;
        }

        startSelectionAnimation(index, 0, ANIMATION_ORIGINAL_SIZE);
    }

    private void startSelectionAnimation(final int index, final float targetPosition, final float targetScale) {
        var running = mAnimators.get(index);
        if (running != null && running.isRunning()) {
            running.cancel();
        }

        final var startScale = mLetterScales.get(LETTERS.charAt(index));
        var startPosition = mLetterPositions.get(LETTERS.charAt(index));
        var animator = ValueAnimator.ofFloat(startScale, targetScale);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            mLetterScales.put(LETTERS.charAt(index), (float)animation.getAnimatedValue());
            mLetterPositions.put(LETTERS.charAt(index), linearInterpolate(startPosition, targetPosition, animation.getAnimatedFraction()));
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                if (targetScale != ANIMATION_ORIGINAL_SIZE) {
                    var startReversePosition = mLetterPositions.get(LETTERS.charAt(index));
                    var revert = ValueAnimator.ofFloat(mLetterScales.get(LETTERS.charAt(index)), ANIMATION_ORIGINAL_SIZE);
                    revert.setDuration(ANIMATION_DURATION);
                    revert.setInterpolator(new DecelerateInterpolator());
                    revert.addUpdateListener(anim -> {
                        mLetterScales.put(LETTERS.charAt(index), (float)anim.getAnimatedValue());
                        mLetterPositions.put(LETTERS.charAt(index), linearInterpolate(startReversePosition, targetPosition, anim.getAnimatedFraction()));
                        invalidate();
                    });

                    revert.start();
                    mAnimators.put(index, revert);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimators.remove(index);
            }
        });

        mAnimators.put(index, animator);
        animator.start();
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

        ShadowHelper.applySettings(getContext(), mPaint);
    }

    private void applyCurrentFont() {
        var fontPackage = SettingsManager.getFont();
        var typeface = fontPackage.isEmpty() ? Typeface.DEFAULT : FontHelper.getFont(fontPackage);

        mBubblePaint.setTypeface(typeface);
        mPaint.setTypeface(typeface);
        invalidate();
    }

    private void adjustPositionForHandedness() {
        if (getLayoutParams() instanceof CoordinatorLayout.LayoutParams params) {
            params.gravity = mLeftHanded
                ? Gravity.START | Gravity.CENTER_VERTICAL
                : Gravity.END | Gravity.CENTER_VERTICAL;

            setLayoutParams(params);
        }

        setPadding(
            mLeftHanded ? 16 : 0,
            getPaddingTop(),
            mLeftHanded ? 0 : 16,
            getPaddingBottom()
        );
    }

    private void resetLetterScale() {
        for (var i = 0; i < LETTERS.length(); i++) {
            mLetterScales.put(LETTERS.charAt(i), ANIMATION_ORIGINAL_SIZE);
        }
    }

    private float linearInterpolate(float a, float b, float f)
    {
        return a * (1.0f - f) + (b * f);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SettingsManager.KEY_FONT.equals(key)) {
            applyCurrentFont();
        }

        if (SettingsManager.KEY_SHADOW_STRENGTH.equals(key)) {
            ShadowHelper.applySettings(getContext(), mPaint);
        }

        if (SettingsManager.KEY_LEFT_HANDED.equals(key)) {
            mLeftHanded = SettingsManager.getLeftHanded();
            mPaint.setTextAlign(mLeftHanded ? Paint.Align.LEFT : Paint.Align.RIGHT);
            adjustPositionForHandedness();
            invalidate();
        }
    }

    public interface IOnLetterSelectedListener {
        void onLetterSelected(char letter);
    }
}
