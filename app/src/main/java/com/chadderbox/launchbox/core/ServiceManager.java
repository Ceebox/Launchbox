package com.chadderbox.launchbox.core;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServiceManager {

    private static final Map<Class<?>, Object> sServices = new HashMap<>();
    private static final Map<Class<?>, Activity> sActivities = new HashMap<>();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private ServiceManager() { }

    /**
     * Register a service type with a factory.
     */
    public static <T> void registerService(Class<T> type, Supplier<T> factory) {
        sServices.put(type, factory.get());
    }

    /**
    * Retrieve an instance of a service.
     */
    public static <T> T getService(Class<T> type) {
        var instance = sServices.get(type);
        if (instance != null) {
            return type.cast(instance);
        }

        throw new IllegalStateException("Service not found: " + type.getName());
    }

    /**
     * Register an activity.
     */
    public static <T> void registerActivity(Class<T> type, Activity activity) {
        sActivities.put(type, activity);
    }

    /**
     * Retrieve an instance of an activity.
     */
    public static <T> T getActivity(Class<T> type) {
        var instance = sActivities.get(type);
        if (instance != null) {
            return type.cast(instance);
        }

        throw new IllegalStateException("Activity not found: " + type.getName());
    }

    public static Handler getMainHandler() {
        return sMainHandler;
    }
}