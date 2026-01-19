package com.chadderbox.launchbox.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chadderbox.launchbox.R;

public class ResizableWidgetFrame extends FrameLayout {

    private View mHandle;
    private OnResizeListener mListener;
    private float mLastX;
    private float mLastY;

    private final GestureDetector mGestureDetector = new GestureDetector(
        getContext(),
        new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                performLongClick();
            }
        }
    );

    public ResizableWidgetFrame(@NonNull Context context) {
        super(context);
        addResizeHandle(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO: We may possibly only want this during edit mode
        mGestureDetector.onTouchEvent(ev);
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addResizeHandle(Context context) {
        mHandle = new ImageView(context);
        // TODO: Replace with a drawable handle icon
        mHandle.setBackgroundColor(Color.LTGRAY);

        var handleSize = (int) (24 * context.getResources().getDisplayMetrics().density);
        var params = new LayoutParams(handleSize, handleSize);
        params.gravity = Gravity.BOTTOM | Gravity.END;

        mHandle.setLayoutParams(params);
        mHandle.setVisibility(GONE);

        mHandle.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = event.getRawX();
                    mLastY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    var deltaX = event.getRawX() - mLastX;
                    var deltaY = event.getRawY() - mLastY;

                    var newWidth = getWidth() + (int) deltaX;
                    var newHeight = getHeight() + (int) deltaY;

                    updateDimensions(newWidth, newHeight);

                    mLastX = event.getRawX();
                    mLastY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_UP:
                    if (mListener != null) {
                        mListener.onResized(getWidth(), getHeight());
                    }
                    return true;
            }
            return false;
        });

        addView(mHandle);
    }

    private void updateDimensions(int width, int height) {
        var params = getLayoutParams();
        params.width = Math.max(width, 100);
        params.height = Math.max(height, 100);
        setLayoutParams(params);
    }

    public void setEditing(boolean editing) {
        mHandle.setVisibility(editing ? VISIBLE : GONE);
        setBackgroundResource(editing ? R.drawable.widget_resize_border : 0);
    }

    public void setOnResizeListener(OnResizeListener listener) {
        mListener = listener;
    }

    public interface OnResizeListener {
        void onResized(int width, int height);
    }
}