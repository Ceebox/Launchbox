package com.chadderbox.launchbox;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.data.AppItem;

public abstract class AppListFragmentBase extends Fragment {

    private static final int SCROLL_THRESHOLD_PERCENT = 10;

    protected CombinedAdapter mAdapter;
    protected RecyclerView mListView;

    protected AppListFragmentBase(CombinedAdapter adapter) {
        mAdapter = adapter;
    }

    abstract void refresh();

    protected void initialiseList(RecyclerView recyclerView) {
        mListView = recyclerView;

        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setNestedScrollingEnabled(true);
        mListView.addOnItemTouchListener(new NestedTouchListener());

        mListView.setAdapter(mAdapter);
    }

    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    public CombinedAdapter getAdapter() {
        return mAdapter;
    }

    public void scrollToLetter(final char letter) {
        final var items = mAdapter.getItems();
        final var layoutManager = (LinearLayoutManager) mListView.getLayoutManager();

        if (layoutManager == null || items.isEmpty()) {
            // Maybe I should throw here?
            // Something has gone wrong, probably, but eh, don't crash
            return;
        }

        char firstLetter = 0;
        char lastLetter = 0;

        // TODO: If these end up not alphabetical in the future, sort that!
        // Even though these are sorted alphabetically,
        // since I made the wise decision to conflate various types of view item,
        // we need to search through to find a valid one...
        // I guess only the future will find out if that was a smart decision, but here I regret it
        for (var item : items) {
            if (item instanceof AppItem appItem) {
                var label = appItem.getAppInfo().getLabel();
                if (label != null && !label.isEmpty()) {
                    firstLetter = Character.toUpperCase(label.charAt(0));
                    break;
                }
            }
        }

        for (int i = items.size() - 1; i >= 0; i--) {
            var item = items.get(i);
            if (item instanceof AppItem appItem) {
                var label = appItem.getAppInfo().getLabel();
                if (label != null && !label.isEmpty()) {
                    lastLetter = Character.toUpperCase(label.charAt(0));
                    break;
                }
            }
        }

        // If we've overshot in either direction, scroll to the top or bottom
        if (letter < firstLetter) {
            scrollToPositionWithOffset(layoutManager, 0);
            return;
        }

        if (letter > lastLetter) {
            scrollToPositionWithOffset(layoutManager, items.size() - 1);
            return;
        }

        // Right, look for the letter inside the items
        // Find exact or closest item position for the letter
        Integer closestPosBefore = null;
        Integer closestPosAfter = null;
        for (int i = 0; i < items.size(); i++) {
            if (!(items.get(i) instanceof AppItem appItem)) {
                continue;
            }

            var label = appItem.getAppInfo().getLabel();
            if (label == null || label.isEmpty()) {
                continue;
            }

            var currentLetter = Character.toUpperCase(label.charAt(0));
            if (currentLetter == letter) {
                if (shouldScrollToPosition(layoutManager, i)) {
                    scrollToPositionWithOffset(layoutManager, i);
                }

                return;
            } else if (currentLetter < letter) {
                closestPosBefore = i;
            } else {
                closestPosAfter = i;
                break;
            }
        }

        // If we don't have an exact match, pick the closest before or after
        final var targetPos = closestPosBefore != null ? closestPosBefore : closestPosAfter;
        if (targetPos != null && shouldScrollToPosition(layoutManager, targetPos)) {
            scrollToPositionWithOffset(layoutManager, targetPos);
        }
    }

    public void smoothScrollToPosition(int position) {
        mListView.smoothScrollToPosition(position);
    }

    public void scrollToPosition(int position) {
        mListView.scrollToPosition(position);
    }

    private void scrollToPositionWithOffset(final LinearLayoutManager layoutManager, final int position) {
        final var recyclerViewHeight = mListView.getHeight();
        final var itemView = layoutManager.findViewByPosition(position);

        var itemHeight = 100;
        if (itemView != null) {
            itemHeight = itemView.getHeight();
        }

        // Go a little above half way so it is easier to focus on
        final var offset = (recyclerViewHeight / 3.33f) - (itemHeight / 2f);
        layoutManager.scrollToPositionWithOffset(position, Math.round(offset));
    }

    /**
     * Check if an item is within a certain threshold to the center of the screen
     */
    private boolean shouldScrollToPosition(final LinearLayoutManager layoutManager, final int position) {
        final var recyclerViewHeight = mListView.getHeight();
        final var itemView = layoutManager.findViewByPosition(position);

        if (itemView == null) {
            // Item is offscreen, so scroll
            return true;
        }

        final var itemTop = itemView.getTop();
        final var itemBottom = itemView.getBottom();
        final var itemCenter = (itemTop + itemBottom) / 2;
        final var recyclerCenter = recyclerViewHeight / 2;

        final var threshold = recyclerViewHeight / SCROLL_THRESHOLD_PERCENT;

        return Math.abs(itemCenter - recyclerCenter) > threshold;
    }

    private static final class NestedTouchListener implements RecyclerView.OnItemTouchListener {
        private float mLastY;

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            // The aim here is to allow nested scrolling, while still being able to transition from the parent
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mLastY = e.getY();
                    rv.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_MOVE:
                    var deltaY = e.getY() - mLastY;
                    rv.getParent().requestDisallowInterceptTouchEvent((!(deltaY > 0) || rv.canScrollVertically(-1)) && (!(deltaY < 0) || rv.canScrollVertically(1)));
                    mLastY = e.getY();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    rv.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Allow the RecyclerView to handle the touch here
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) { }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
    }
}
