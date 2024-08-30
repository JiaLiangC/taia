package com.dtstack.taier.datasource.plugin.common.utils;

import com.dtstack.taier.datasource.plugin.common.exception.TaierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.time.Duration;

public class ThreadUtils  {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);

    public static ScheduledExecutorService newDaemonSingleThreadScheduledExecutor(
            String threadName,
            boolean executeExistingDelayedTasksAfterShutdown) {
        NamedThreadFactory threadFactory = new NamedThreadFactory(threadName, true);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, threadFactory);
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(executeExistingDelayedTasksAfterShutdown);
        return executor;
    }

    public static ScheduledExecutorService newDaemonSingleThreadScheduledExecutor(String threadName) {
        return newDaemonSingleThreadScheduledExecutor(threadName, true);
    }

    public static ThreadPoolExecutor newDaemonQueuedThreadPool(
            int poolSize,
            int poolQueueSize,
            long keepAliveMs,
            String threadPoolName) {
        NamedThreadFactory nameFactory = new NamedThreadFactory(threadPoolName, true);
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(poolQueueSize);
        LOGGER.info(String.format("%s: pool size: %d, wait queue size: %d, thread keepalive time: %d ms",
                threadPoolName, poolSize, poolQueueSize, keepAliveMs));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                keepAliveMs,
                TimeUnit.MILLISECONDS,
                queue,
                nameFactory);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static ThreadPoolExecutor newDaemonFixedThreadPool(int nThreads, String prefix) {
        NamedThreadFactory threadFactory = new NamedThreadFactory(prefix, true);
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads, threadFactory);
    }

    public static ThreadPoolExecutor newDaemonCachedThreadPool(String prefix) {
        NamedThreadFactory threadFactory = new NamedThreadFactory(prefix, true);
        return (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
    }

    public static <T> T awaitResult(Future<T> future, Duration timeout) throws TaierException {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new TaierException("Exception thrown in awaitResult: ", e);
        }
    }

    public static void shutdown(ExecutorService executor, Duration gracePeriod) {
        long shutdownTimeout = gracePeriod.toMillis();
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.warn(String.format("Exceeded timeout(%d ms) to wait the exec-pool shutdown gracefully", shutdownTimeout), e);
            }
        }
    }

    public static void shutdown(ExecutorService executor) {
        shutdown(executor, Duration.ofSeconds(30));
    }

    public static void runInNewThread(String threadName, boolean isDaemon, Runnable body) {
        Thread thread = new Thread(threadName) {
            @Override
            public void run() {
                body.run();
            }
        };
        thread.setDaemon(isDaemon);
        thread.setUncaughtExceptionHandler(NamedThreadFactory.TAIER_UNCAUGHT_EXCEPTION_HANDLER);
        thread.start();
    }

    public static void scheduleTolerableRunnableWithFixedDelay(
            ScheduledExecutorService scheduler,
            Runnable runnable,
            long initialDelay,
            long delay,
            TimeUnit timeUnit) {
        scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        LOGGER.error("Uncaught exception in thread " + Thread.currentThread().getName(), t);
                    }
                },
                initialDelay,
                delay,
                timeUnit);
    }
}
