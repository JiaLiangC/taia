package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.utils.ThreadUtils;
import com.dtstack.taier.datasource.plugin.common.utils.Utils;
import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.dtstack.taier.datasource.plugin.common.config.TaierConf.*;


public abstract class SessionManager extends CompositeService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private volatile boolean shutdown = false;
    protected volatile Optional<String> operationLogRoot = Optional.empty();
    private volatile long latestLogoutTime = System.currentTimeMillis();
    private final ConcurrentHashMap<SessionHandle, Session> handleToSession = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutChecker = ThreadUtils.newDaemonSingleThreadScheduledExecutor(this.serviceName+"-timeout-checker");
    private ThreadPoolExecutor execPool;
    private Set<String> confRestrictList;
    private Set<String> confIgnoreList;
    private Set<String> batchConfIgnoreList;
    private Set<String> confRestrictMatchList;
    private Set<String> confIgnoreMatchList;
    private Set<String> batchConfIgnoreMatchList;

    protected SessionManager(String name) {
        super(name);
    }

    public Optional<String> getOperationLogRoot() {
        return operationLogRoot;
    }

    private void initOperationLogRootDir() {
        try {
            String logRoot = "/var/log/taier";
            Path logPath = Files.createDirectories(Utils.getAbsolutePathFromWork(logRoot));
            operationLogRoot = Optional.of(logPath.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to initialize operation log root directory: " + operationLogRoot, e);
            operationLogRoot = Optional.empty();
        }
    }

    public long getLatestLogoutTime() {
        return latestLogoutTime;
    }

    protected abstract boolean isServer();

    public Future<?> submitBackgroundOperation(Runnable r) {
        return execPool.submit(r);
    }

    public abstract OperationManager getOperationManager();

    protected abstract Session createSession(Connection connection, String user, String password, String ipAddress, Map<String, String> conf);

    protected void logSessionCountInfo(Session session, String action) {
        LOGGER.info(session.getUser() + "'s " + session.getClass().getSimpleName() + " with " +
                session.getHandle() + session.getName().map(n -> "/" + n).orElse("") + " is " + action +
                ", current opening sessions " + getActiveUserSessionCount());
    }


    public SessionHandle openSession(Connection connection, String user, String password, String ipAddress, Map<String, String> conf) throws TaierSQLException {
        LOGGER.info("Opening session for " + user + "@" + ipAddress);
        Session session = createSession(connection, user, password, ipAddress, conf);
        SessionHandle handle = session.getHandle();
        try {
            setSession(handle, session);
            session.open();
            logSessionCountInfo(session, "opened");
            return handle;
        } catch (Exception e) {
            try {
                closeSession(handle);
            } catch (Throwable t) {
                LOGGER.warn("Error closing session for " + user + " client ip: " + ipAddress, t);
            }
            throw new TaierSQLException(e);
        }
    }

    public void closeSession(SessionHandle sessionHandle) throws TaierSQLException {
        Session session = handleToSession.remove(sessionHandle);
        if (session == null) {
            throw new TaierSQLException("Invalid " + sessionHandle);
        }
        if (!session.isForAliveProbe()) {
            latestLogoutTime = System.currentTimeMillis();
        }
        logSessionCountInfo(session, "closed");
        try {
            session.close();
        } finally {
            deleteOperationLogSessionDir(sessionHandle);
        }
    }

    private void deleteOperationLogSessionDir(SessionHandle sessionHandle) {
        operationLogRoot.ifPresent(logRoot -> {
            Path rootPath = Paths.get(logRoot, sessionHandle.getIdentifier().toString());
            Utils.deleteDirectoryRecursively(rootPath.toFile());
        });
    }

    public Optional<Session> getSessionOption(SessionHandle sessionHandle) {
        return Optional.ofNullable(handleToSession.get(sessionHandle));
    }

    public Session getSession(SessionHandle sessionHandle) throws TaierSQLException {
        return getSessionOption(sessionHandle).orElseThrow(() -> new TaierSQLException("Invalid " + sessionHandle));
    }

    protected final void setSession(SessionHandle sessionHandle, Session session) {
        handleToSession.put(sessionHandle, session);
    }

    public int getActiveUserSessionCount() {
        return (int) handleToSession.values().stream().filter(s -> !s.isForAliveProbe()).count();
    }

    public Iterable<Session> allSessions() {
        return handleToSession.values();
    }

    public int getExecPoolSize() {
        assert execPool != null;
        return execPool.getPoolSize();
    }

    public int getActiveCount() {
        assert execPool != null;
        return execPool.getActiveCount();
    }

    public int getWorkQueueSize() {
        assert execPool != null;
        return execPool.getQueue().size();
    }

    public Optional<Map.Entry<String, String>> validateKey(String key, String value) throws TaierSQLException {
        return Optional.of(new AbstractMap.SimpleEntry<>(key, value));
    }


    private String normalizeKey(String key) throws TaierSQLException {
        if (key.startsWith("env:")) {
            throw new TaierSQLException(key + " is forbidden, env:* variables can not be set.");
        } else if (key.startsWith("system:")) {
            return key.substring("system:".length());
        } else if (key.startsWith("hiveconf:")) {
            return key.substring("hiveconf:".length());
        } else if (key.startsWith("hivevar:")) {
            return key.substring("hivevar:".length());
        } else if (key.startsWith("metaconf:")) {
            return key.substring("metaconf:".length());
        } else {
            return key;
        }
    }

    public Map<String, String> validateAndNormalizeConf(Map<String, String> config) {
        return config.entrySet().stream()
                .map(entry -> {
                    try {
                        return validateKey(entry.getKey(), entry.getValue());
                    } catch (TaierSQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Optional<Map.Entry<String, String>> validateBatchKey(String key, String value) {
        if (batchConfIgnoreMatchList.stream().anyMatch(key::startsWith) || batchConfIgnoreList.contains(key)) {
            LOGGER.warn(key + " is a ignored batch key according to the server-side configuration");
            return Optional.empty();
        } else {
            return Optional.of(new AbstractMap.SimpleEntry<>(key, value));
        }
    }

    public Map<String, String> validateBatchConf(Map<String, String> config) {
        return config.entrySet().stream()
                .map(entry -> validateBatchKey(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public synchronized void initialize(TaierConf conf) {
        addService(getOperationManager());
        initOperationLogRootDir();

        int poolSize = conf.get(SERVER_EXEC_POOL_SIZE);
        int waitQueueSize = conf.get(SERVER_EXEC_WAIT_QUEUE_SIZE);
        long keepAliveMs = conf.get(SERVER_EXEC_KEEPALIVE_TIME);

        confRestrictList = new HashSet<>();
        confIgnoreList = new HashSet<>();
        confIgnoreList.add("taier.session.user.sign.enabled");
        batchConfIgnoreList =  new HashSet<>();

        execPool = ThreadUtils.newDaemonQueuedThreadPool(
                poolSize,
                waitQueueSize,
                keepAliveMs,
                getName() + "-exec-pool");
        super.initialize(conf);
    }

    @Override
    public synchronized void start() {
        startTimeoutChecker();
        super.start();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        shutdown = true;
        long shutdownTimeout = conf.get(SERVER_EXEC_POOL_SHUTDOWN_TIMEOUT);

        ThreadUtils.shutdown(timeoutChecker, Duration.ofMillis(shutdownTimeout));
        ThreadUtils.shutdown(execPool, Duration.ofMillis(shutdownTimeout));
    }

    private void startTimeoutChecker() {
        long interval = conf.get(SESSION_CHECK_INTERVAL);

        Runnable checkTask = () -> {
            LOGGER.info("Checking sessions timeout, current count: " + getActiveUserSessionCount());
            System.out.println("Checking sessions timeout, current count: " + getActiveUserSessionCount());
            long current = System.currentTimeMillis();
            if (!shutdown) {
                for (Session session : handleToSession.values()) {
                    try {
                        if (session.getLastAccessTime() + session.getSessionIdleTimeoutThreshold() <= current &&
                                session.getNoOperationTime() > session.getSessionIdleTimeoutThreshold()) {
                            System.out.println("Closing session " + session.getHandle().getIdentifier() + " that has been idle for more" +
                                    " than " + session.getSessionIdleTimeoutThreshold() + " ms");
                            LOGGER.info("Closing session " + session.getHandle().getIdentifier() + " that has been idle for more" +
                                    " than " + session.getSessionIdleTimeoutThreshold() + " ms");
                            closeSession(session.getHandle());
                        } else {
                            session.closeExpiredOperations();
                        }
                    } catch (Exception e) {
                        System.out.println("Error checking session " + session.getHandle() + " timeout");
                        LOGGER.warn("Error checking session " + session.getHandle() + " timeout", e);
                    }
                }
            }
        };

        ThreadUtils.scheduleTolerableRunnableWithFixedDelay(
                timeoutChecker,
                checkTask,
                interval,
                interval,
                TimeUnit.MILLISECONDS);
    }

    void startTerminatingChecker(Runnable stop) {
        if (!isServer()) {
            latestLogoutTime = System.currentTimeMillis();
            long interval = 60*1000;
            long idleTimeout = 30000L;
            if (idleTimeout > 0) {
                Runnable checkTask = () -> {
                    if (!shutdown && System.currentTimeMillis() - latestLogoutTime > idleTimeout &&
                            getActiveUserSessionCount() <= 0) {
                        LOGGER.info("Idled for more than " + idleTimeout + " ms, terminating");
                        stop.run();
                    }
                };
                ThreadUtils.scheduleTolerableRunnableWithFixedDelay(
                        timeoutChecker,
                        checkTask,
                        interval,
                        interval,
                        TimeUnit.MILLISECONDS);
            }
        }
    }
}
