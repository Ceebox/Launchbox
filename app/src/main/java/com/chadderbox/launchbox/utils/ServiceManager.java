package com.chadderbox.launchbox.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServiceManager {

    private static final Map<Class<?>, Object> sServices = new HashMap<>();

    private ServiceManager() { }

    /**
     * Register a service type with a factory.
     */
    public static <T> void register(Class<T> type, Supplier<T> factory) {
        sServices.put(type, factory.get());
    }

    /**
    * Retrieve an instance of a service.
     */
    public static <T> T resolve(Class<T> type) {
        var instance = sServices.get(type);
        if (instance != null) {
            return type.cast(instance);
        }

        throw new IllegalStateException("Service not found: " + type.getName());
    }
}