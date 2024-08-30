package com.dtstack.taier.datasource.plugin.common.operation;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.jdbc.Row;
import com.dtstack.taier.datasource.plugin.common.jdbc.Schema;
import com.dtstack.taier.datasource.plugin.common.utils.ThreadUtils;
import com.dtstack.taier.datasource.plugin.common.utils.Utils;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.dtstack.taier.datasource.plugin.common.config.TaierConf.OPERATION_IDLE_TIMEOUT;

public abstract class AbstractOperation implements Operation {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractOperation.class);
    protected final String opType;
    protected final long createTime;
    protected final OperationHandle handle;
    private final long operationTimeout;



    final String statementId;

    private Optional<ScheduledExecutorService> statementTimeoutCleaner = Optional.empty();

    private final ReentrantLock lock = new ReentrantLock();

    protected final Session session;

    protected volatile OperationState state = OperationState.INITIALIZED;
    protected volatile long startTime;
    protected volatile long completedTime;
    protected volatile long lastAccessTime;

    protected volatile TaierSQLException operationException;

    protected volatile boolean hasResultSet = false;

    private volatile Future<?> backgroundHandle;

    public AbstractOperation(Session session) {
        this.session = session;
        this.opType = getClass().getSimpleName();
        this.createTime = System.currentTimeMillis();
        this.handle =  OperationHandle.create();
        this.operationTimeout = session.getSessionManager().getConf().get(OPERATION_IDLE_TIMEOUT);
        this.statementId = handle.getIdentifier().toString();
        this.lastAccessTime = createTime;

    }

    public OperationState getState() {
        return state;
    }

    public String getStatementId() {
        return statementId;
    }
    protected <T> T withLockRequired(Supplier<T> block) {
        return Utils.withLockRequired(lock, block);
    }


    protected void cleanup(OperationState targetState) {
        try {
            withLockRequired(() -> {
                if (!isTerminalState(state)) {
                    setState(targetState);
                    Optional.ofNullable(getBackgroundHandle()).ifPresent(h -> h.cancel(true));
                }
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("Error during cleanup", e);
        }
    }

    protected void addTimeoutMonitor(long queryTimeout) {
        if (queryTimeout > 0) {
            ScheduledExecutorService timeoutExecutor =
                    ThreadUtils.newDaemonSingleThreadScheduledExecutor("query-timeout-thread", false);
            Runnable action = () -> cleanup(OperationState.TIMEOUT);
            timeoutExecutor.schedule(action, queryTimeout, TimeUnit.SECONDS);
            statementTimeoutCleaner = Optional.of(timeoutExecutor);
        }
    }

    protected void shutdownTimeoutMonitor() {
        statementTimeoutCleaner.ifPresent(ScheduledExecutorService::shutdown);
    }

    @Override
    public Optional<OperationLog> getOperationLog() {
        return Optional.empty();
    }

    public String getStatement() {
        return opType;
    }

    public String getRedactedStatement() {
        return getStatement();
    }

    protected void setHasResultSet(boolean hasResultSet) {
        this.hasResultSet = hasResultSet;
        handle.setHasResultSet(hasResultSet);
    }

    protected void setOperationException(TaierSQLException opEx) {
        this.operationException = opEx;
    }


    protected void setState(OperationState newState) {
        OperationState.validateTransition(state, newState);
        switch (newState) {
            case RUNNING:
                LOGGER.info(String.format("Processing %s's query[%s]: %s -> %s, statement:%n%s",
                        session.getUser(), statementId, state.name(), newState.name(), getRedactedStatement()));
                startTime = System.currentTimeMillis();
                break;
            case ERROR:
            case FINISHED:
            case CANCELED:
            case TIMEOUT:
            case CLOSED:
                completedTime = System.currentTimeMillis();
                String timeCost = String.format(", time taken: %.1f seconds", (completedTime - startTime) / 1000.0);
                LOGGER.info(String.format("Processing %s's query[%s]: %s -> %s%s",
                        session.getUser(), statementId, state.name(), newState.name(), timeCost));
                break;
        }
        state = newState;
        lastAccessTime = System.currentTimeMillis();
//        OperationAuditLogger.audit(this, state);
    }

    protected boolean isClosedOrCanceled() {
        return state == OperationState.CLOSED || state == OperationState.CANCELED;
    }

    protected boolean isTerminalState(OperationState operationState) {
        return OperationState.isTerminal(operationState);
    }

    protected void assertState(OperationState state) {
        if (this.state != state) {
            throw new IllegalStateException("Expected state " + state + ", but found " + this.state);
        }
        lastAccessTime = System.currentTimeMillis();
    }

    protected void validateDefaultFetchOrientation(FetchOrientation orientation) throws TaierSQLException {
        validateFetchOrientation(orientation, OperationConstants.DEFAULT_FETCH_ORIENTATION_SET);
    }

    private void validateFetchOrientation(FetchOrientation orientation, Set<FetchOrientation> supportedOrientations) throws TaierSQLException {
        if (!supportedOrientations.contains(orientation)) {
            throw new TaierSQLException("The fetch type " + orientation + " is not supported for this ResultSet.");
        }
    }

    protected List<Map<String, Object>> toTResultMap(List<Row> rowList, Schema schema) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Column> columns = schema.getColumns();

        for (Row row : rowList) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            List<Object> values = row.getValues();

            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                String columnName = column.getLabel() != null ? column.getLabel() : column.getName();
                Object value = i < values.size() ? values.get(i) : null;
                // 处理重复列名
                columnName = dealRepeatColumn(rowMap, columnName, new HashMap<>());

                rowMap.put(columnName, value);
            }

            result.add(rowMap);
        }

        return result;
    }

    private String dealRepeatColumn(Map<String, Object> row, String column, Map<String, Integer> columnRepeatSign) {
        if (row.containsKey(column)) {
            int repeatNum = columnRepeatSign.getOrDefault(column, 0) + 1;
            columnRepeatSign.put(column, repeatNum);
            return column + "_" + repeatNum;
        }
        return column;
    }


    protected abstract void runInternal() throws TaierSQLException;

    protected abstract void beforeRun();

    protected abstract void afterRun();

    @Override
    public void run() throws TaierSQLException {
        beforeRun();
        try {
            runInternal();
        } finally {
            afterRun();
        }
    }

    @Override
    public abstract void cancel();

    @Override
    public abstract void close();

    protected abstract List<Map<String, Object>> getNextRowSetInternal(FetchOrientation order, int rowSetSize) throws TaierSQLException;

    @Override
    public List<Map<String, Object>> getNextRowSet(FetchOrientation order, int rowSetSize) {
        try {
            return withLockRequired(() -> {
                try {
                    return getNextRowSetInternal(order, rowSetSize);
                } catch (TaierSQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error getting next row set", e);
            return null;
        }
    }

    protected String toJavaRegex(String input) {
        String res = StringUtils.isEmpty(input) || input.equals("*") ? "%" : input;
        String wStr = ".*";
        return res
                .replaceAll("([^\\\\])%", "$1" + wStr).replaceAll("\\\\%", "%").replaceAll("^%", wStr)
                .replaceAll("([^\\\\])_", "$1.").replaceAll("\\\\_", "_").replaceAll("^_", ".");
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public OperationHandle getHandle() {
        return handle;
    }

    @Override
    public OperationStatus getStatus() {
        lastAccessTime = System.currentTimeMillis();
        return new OperationStatus(
                state,
                createTime,
                startTime,
                lastAccessTime,
                completedTime,
                hasResultSet,
                Optional.ofNullable(operationException)
                );
    }

    @Override
    public abstract boolean shouldRunAsync();

    @Override
    public boolean isTimedOut() {
        if (operationTimeout <= 0) {
            return false;
        } else {
            return OperationState.isTerminal(state) &&
                    lastAccessTime + operationTimeout <= System.currentTimeMillis();
        }
    }


    protected void setBackgroundHandle(Future<?> backgroundHandle) {
        this.backgroundHandle = backgroundHandle;
    }

    public Future<?> getBackgroundHandle() {
        return backgroundHandle;
    }
}
