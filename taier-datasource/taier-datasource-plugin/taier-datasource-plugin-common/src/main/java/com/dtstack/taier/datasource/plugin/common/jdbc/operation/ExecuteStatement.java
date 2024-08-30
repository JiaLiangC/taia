package com.dtstack.taier.datasource.plugin.common.jdbc.operation;


import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
//import com.dtstack.taier.datasource.plugin.common.Logging;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.jdbc.Row;
import com.dtstack.taier.datasource.plugin.common.jdbc.Schema;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcSessionImpl;
import com.dtstack.taier.datasource.plugin.common.jdbc.ResultSetWrapper;
import com.dtstack.taier.datasource.plugin.common.operation.ArrayFetchIterator;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.IterableFetchIterator;
import com.dtstack.taier.datasource.plugin.common.operation.OperationState;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteStatement extends JdbcOperation {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ExecuteStatement.class);
    private final String statement;
    private final boolean shouldRunAsync;
    private final long queryTimeout;
    private final boolean incrementalCollect;
    private final int fetchSize;

//    private final OperationLog operationLog;
    private volatile Statement jdbcStatement;

    public ExecuteStatement(
            Session session,
            String statement,
            boolean shouldRunAsync,
            long queryTimeout,
            boolean incrementalCollect,
            int fetchSize) {
        super(session);
        this.statement = statement;
        this.shouldRunAsync = shouldRunAsync;
        this.queryTimeout = queryTimeout;
        this.incrementalCollect = incrementalCollect;
        this.fetchSize = fetchSize;
    }

    @Override
    protected void runInternal() throws TaierSQLException {
        addTimeoutMonitor(queryTimeout);
        if (shouldRunAsync) {
            Runnable asyncOperation = new Runnable() {
                @Override
                public void run() {
                    try {
                        executeStatement();
                    } catch (TaierSQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            setBackgroundHandle(session.getSessionManager().submitBackgroundOperation(asyncOperation));
        } else {
            executeStatement();
        }
    }


    private void executeStatement() throws TaierSQLException {
        setState(OperationState.RUNNING);
        try {
            Connection connection = ((JdbcSessionImpl) session).getSessionConnection();
            jdbcStatement = dialect.createStatement(connection, fetchSize);
            boolean hasResult = jdbcStatement.execute(statement);
            if (hasResult) {
                ResultSetWrapper resultSetWrapper = new ResultSetWrapper(jdbcStatement);
                schema = Schema.apply(resultSetWrapper.getMetadata());
                if (incrementalCollect) {
                    LOGGER.info("Execute in incremental collect mode");
                    iter = new IterableFetchIterator<>(() -> resultSetWrapper);
                } else {
                    LOGGER.warn("Execute in full collect mode");
                    iter = new ArrayFetchIterator<>(resultSetWrapper.toArray());
                    jdbcStatement.close();
                }
            } else {
                schema = new Schema(Collections.singletonList(
                        new Column("result", "INT", Types.INTEGER, 20, 0, "result", 20)));
                iter = new ArrayFetchIterator<>(new Row[]{new Row(Collections.singletonList(jdbcStatement.getUpdateCount()))});
            }
            setState(OperationState.FINISHED);
        } catch (Exception e) {
            onError(true, e);
        } finally {
            shutdownTimeoutMonitor();
        }
    }

    @Override
    public void validateFetchOrientation(FetchOrientation order) throws TaierSQLException {
        if (incrementalCollect && order != FetchOrientation.FETCH_NEXT) {
            throw new TaierSQLException("The fetch type " + order + " is not supported of incremental collect mode.");
        }
        super.validateFetchOrientation(order);
    }

    @Override
    public void cleanup(OperationState targetState) {
        withLockRequired(() -> {
            try {
                super.cleanup(targetState);
            } finally {
                try {
                    if (jdbcStatement != null && !jdbcStatement.isClosed()) {

                            jdbcStatement.close();
                        }
                        jdbcStatement = null;
                    }
                catch (Exception e) {
                    jdbcStatement = null;
                    // Log the exception
                    LOGGER.error("Error closing JDBC statement", e);
                }
            }
            return null;
        });
    }

    @Override
    public String getStatement() {
        return statement;
    }

    @Override
    public boolean shouldRunAsync() {
        return shouldRunAsync;
    }
}
