package com.dtstack.taier.datasource.plugin.common.utils;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
    private final String name;
    private final boolean daemon;

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name + ": Thread-" + t.getId());
        t.setDaemon(daemon);
        t.setUncaughtExceptionHandler(NamedThreadFactory.TAIER_UNCAUGHT_EXCEPTION_HANDLER);
        return t;
    }

    private static class NamedThreadFactoryHolder {
        private static final TaierUncaughtExceptionHandler TAIER_UNCAUGHT_EXCEPTION_HANDLER = new TaierUncaughtExceptionHandler();
    }

    public static final TaierUncaughtExceptionHandler TAIER_UNCAUGHT_EXCEPTION_HANDLER = NamedThreadFactoryHolder.TAIER_UNCAUGHT_EXCEPTION_HANDLER;
}
