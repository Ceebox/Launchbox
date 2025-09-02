package com.chadderbox.launchbox.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public final class AlphabetIndexView extends View {
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Paint mPaint;
    private OnLetterSelectedListener mListener;

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(32f * getResources().getDisplayMetrics().density / 3);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        var width = getWidth();
        var height = getHeight();

        var availableHeight = height - getPaddingTop() - getPaddingBottom();
        var cellHeight = availableHeight / LETTERS.length();

        for (var i = 0; i < LETTERS.length(); i++) {
            var x = width / 2f;
            var y = getPaddingTop() + cellHeight * i + cellHeight / 2f + mPaint.getTextSize() / 2f;
            canvas.drawText(String.valueOf(LETTERS.charAt(i)), x, y, mPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        var action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            var y = event.getY();
            var height = getHeight() - getPaddingTop() - getPaddingBottom();
            var index = (int) ((y - getPaddingTop()) / height * LETTERS.length());

            if (index < 0) {
                index = 0;
            }

            if (index >= LETTERS.length()) {
                index = LETTERS.length() - 1;
            }

            if (mListener != null) {
                mListener.onLetterSelected(LETTERS.charAt(index));
            }

            return true;
        }

        return super.onTouchEvent(event);
    }

    public void setOnLetterSelectedListener(OnLetterSelectedListener listener) {
        mListener = listener;
    }

    public interface OnLetterSelectedListener {
        void onLetterSelected(char letter);
    }
}