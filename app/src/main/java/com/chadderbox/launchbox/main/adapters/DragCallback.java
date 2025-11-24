package com.chadderbox.launchbox.main.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.data.HeaderItem;

public class DragCallback extends ItemTouchHelper.Callback {

    private final CombinedAdapter mAdapter;
    private final RecyclerView mRecyclerView;
    private boolean mDragging = false;
    private boolean mEditModeEnabled = false;

    public DragCallback(CombinedAdapter adapter, RecyclerView recyclerView) {
        mAdapter = adapter;
        mRecyclerView = recyclerView;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // We want this!
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(
        @NonNull RecyclerView recyclerView,
        @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        if (!mEditModeEnabled) {
            // Nope!
            return makeMovementFlags(0, 0);
        }

        var item = mAdapter.getItem(viewHolder.getBindingAdapterPosition());

        if (item instanceof HeaderItem) {
            // Do not allow dragging headers
            return makeMovementFlags(0, 0);
        }

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(
        @NonNull RecyclerView recyclerView,
        @NonNull RecyclerView.ViewHolder viewHolder,
        @NonNull RecyclerView.ViewHolder target
    ) {
        var fromPos = viewHolder.getBindingAdapterPosition();
        var toPos = target.getBindingAdapterPosition();

        mAdapter.moveItem(fromPos, toPos);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // No swipe action
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        mDragging = actionState != ItemTouchHelper.ACTION_STATE_IDLE;

        // Prevent the ViewPager from intercepting touch while dragging
        if (mDragging && mRecyclerView != null) {
            mRecyclerView.getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public void clearView(
        @NonNull RecyclerView recyclerView,
        @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        super.clearView(recyclerView, viewHolder);

        mDragging = false;

        // Do this here rather than onMove to avoid doing it repeatedly
        mAdapter.saveFavouritesOrder();

        // Restore parent touch interception after drag
        if (mRecyclerView != null) {
            mRecyclerView.getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    public boolean isDragging() {
        return mDragging;
    }

    public void setEditMode(boolean enabled) {
        mEditModeEnabled = enabled;
    }

    public boolean getEditModeEnabled() {
        return mEditModeEnabled;
    }
}
