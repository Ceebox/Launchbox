package com.chadderbox.launchbox.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared background executors for asynchronous tasks.
 */
public final class AppExecutors {

    private static final ExecutorService sDiskIoExecutor = Executors.newSingleThreadExecutor();

    private AppExecutors() { }

    public static ExecutorService getDiskIo() {
        return sDiskIoExecutor;
    }
}
