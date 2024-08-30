package com.dtstack.taier.datasource.plugin.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaierUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TaierUncaughtExceptionHandler.class);
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Uncaught exception in thread " + t.getName(), e);
    }
}
