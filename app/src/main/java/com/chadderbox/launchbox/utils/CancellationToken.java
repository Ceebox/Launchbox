package com.chadderbox.launchbox.utils;

import java.util.concurrent.atomic.AtomicBoolean;

// Hey .NET devs, seen this one before?
public final class CancellationToken {
    private final AtomicBoolean mCancelled = new AtomicBoolean(false);

    public void cancel() {
        mCancelled.set(true);
    }

    public boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * Cancels the current cancellation token and returns a new one.
     */
    public CancellationToken regenerate() {
        cancel();
        return new CancellationToken();
    }
}
