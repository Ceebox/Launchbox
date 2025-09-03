package com.chadderbox.launchbox;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chadderbox.launchbox.data.AppItem;

public abstract class AppListFragmentBase extends Fragment {

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

    public void scrollToLetter(char letter) {
        var items = mAdapter.getItems();
        var layoutManager = (LinearLayoutManager) mListView.getLayoutManager();

        if (layoutManager == null) {
            // Maybe I should throw here?
            // Something has gone wrong, probably, but eh, don't crash
            return;
        }

        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item instanceof AppItem appItem) {
                if (appItem.getAppInfo().getLabel().toUpperCase().startsWith("" + letter)) {
                    // Scroll so that the item appears at the top
                    layoutManager.scrollToPositionWithOffset(i, 0);
                    break;
                }
            }
        }
    }

    public void smoothScrollToPosition(int position) {
        mListView.smoothScrollToPosition(position);
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
