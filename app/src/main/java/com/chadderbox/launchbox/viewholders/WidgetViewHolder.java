package com.chadderbox.launchbox.viewholders;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.data.WidgetListItem;
import com.chadderbox.launchbox.ui.components.ResizableWidgetFrame;
import com.chadderbox.launchbox.widgets.WidgetHostManager;

public class WidgetViewHolder extends RecyclerView.ViewHolder {
    private final ResizableWidgetFrame mFrame;
    private final WidgetHostManager mWidgetHostManager;

    public WidgetViewHolder(@NonNull ResizableWidgetFrame frame, WidgetHostManager widgetHostManager) {
        super(frame);
        mFrame = frame;
        mWidgetHostManager = widgetHostManager;
    }

    public void bind(WidgetListItem item, boolean isEditMode) {
        mFrame.removeAllViews();
        mFrame.setEditing(isEditMode);

        var widgetData = item.getWidgetItem();
        var widgetView = mWidgetHostManager.getWidgetView(widgetData.appWidgetId);
        if (widgetView != null) {
            var density = mFrame.getResources().getDisplayMetrics().density;

            var params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                widgetData.heightDp > 0 ? (int) (widgetData.heightDp * density) : ViewGroup.LayoutParams.WRAP_CONTENT
            );

            mFrame.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            mFrame.addView(widgetView, params);

            mFrame.setOnResizeListener((width, height) -> {
                var newWidthDp = mWidgetHostManager.snapToCellDp((int) (width / density));
                var newHeightDp = mWidgetHostManager.snapToCellDp((int) (height / density));

                mWidgetHostManager.updateWidgetSize(widgetData.appWidgetId, newWidthDp, newHeightDp);
            });

            mFrame.setOnLongClickListener(v -> {
                item.performHoldAction(v);
                return true;
            });
        }
    }
}