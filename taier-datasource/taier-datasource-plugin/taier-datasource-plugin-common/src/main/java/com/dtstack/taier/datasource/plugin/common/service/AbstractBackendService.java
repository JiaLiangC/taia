package com.dtstack.taier.datasource.plugin.common.service;


import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.Operation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import com.dtstack.taier.datasource.plugin.common.operation.OperationStatus;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Shorthand for implementing BackendServices
 */
public abstract class AbstractBackendService extends CompositeService implements BackendService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractBackendService.class);
    private long timeout;
    private Optional<Integer> maxRowsLimit;

    public AbstractBackendService(String name) {
        super(name);
    }


    @Override
    public SessionHandle openSession(Connection conn, String user, String password, String ipAddr, Map<String, String> configs) throws TaierSQLException {
        return getSessionManager().openSession(conn, user, password, ipAddr, configs);
    }

    @Override
    public void closeSession(SessionHandle sessionHandle) throws TaierSQLException {
        getSessionManager().closeSession(sessionHandle);
    }

    @Override
    public OperationHandle executeStatement(
            SessionHandle sessionHandle,
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout) throws TaierSQLException {
        return getSessionManager().getSession(sessionHandle).executeStatement(
                statement,
                confOverlay,
                runAsync,
                queryTimeout);
    }

    @Override
    public OperationHandle getTypeInfo(SessionHandle sessionHandle) throws TaierSQLException {
        return getSessionManager().getSession(sessionHandle).getTypeInfo();
    }

    @Override
    public OperationHandle getCatalogs(SessionHandle sessionHandle) throws TaierSQLException {
        return getSessionManager().getSession(sessionHandle).getCatalogs();
    }

    @Override
    public OperationHandle getSchemas(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getSchemas(catalogName, schemaName);
    }

    @Override
    public OperationHandle getTables(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getTables(catalogName, schemaName, tableName, tableTypes);
    }

    @Override
    public OperationHandle getTableTypes(SessionHandle sessionHandle) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getTableTypes();
    }

    @Override
    public OperationHandle getColumns(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName,
            String columnName) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getColumns(catalogName, schemaName, tableName, columnName);
    }

    @Override
    public OperationHandle getFunctions(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String functionName) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getFunctions(catalogName, schemaName, functionName);
    }

    @Override
    public OperationHandle getPrimaryKeys(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getPrimaryKeys(catalogName, schemaName, tableName);
    }

    @Override
    public OperationHandle getCrossReference(
            SessionHandle sessionHandle,
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) throws TaierSQLException {
        return getSessionManager()
                .getSession(sessionHandle)
                .getCrossReference(
                        primaryCatalog,
                        primarySchema,
                        primaryTable,
                        foreignCatalog,
                        foreignSchema,
                        foreignTable);
    }

    @Override
    public String getQueryId(OperationHandle operationHandle) throws TaierSQLException {
        Operation operation = getSessionManager().getOperationManager().getOperation(operationHandle);
        return getSessionManager().getOperationManager().getQueryId(operation);
    }

    @Override
    public OperationStatus getOperationStatus(
            OperationHandle operationHandle,
            Optional<Long> maxWait) throws TaierSQLException {
        Operation operation = getSessionManager().getOperationManager().getOperation(operationHandle);
        if (operation.shouldRunAsync()) {
            try {
                long waitTime = maxWait.orElse(timeout);
                operation.getBackgroundHandle().get(waitTime, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                debug(operationHandle + ": Long polling timed out, " + e.getMessage());
            } catch (CancellationException e) {
                debug(operationHandle + ": The background operation was cancelled, " + e.getMessage());
            } catch (ExecutionException e) {
                debug(operationHandle + ": The background operation was aborted, " + e.getMessage());
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
        return operation.getStatus();
    }

    @Override
    public void cancelOperation(OperationHandle operationHandle) throws TaierSQLException {
        getSessionManager()
                .getOperationManager()
                .getOperation(operationHandle)
                .getSession()
                .cancelOperation(operationHandle);
    }

    @Override
    public void closeOperation(OperationHandle operationHandle) throws TaierSQLException {
        getSessionManager().getOperationManager()
                .getOperation(operationHandle).getSession().closeOperation(operationHandle);
    }

    @Override
    public List<Column> getResultSetMetadata(OperationHandle operationHandle) throws TaierSQLException {
        return getSessionManager().getOperationManager()
                .getOperation(operationHandle).getSession().getResultSetMetadata(operationHandle);
    }

    @Override
    public  List<Map<String, Object>> fetchResults(
            OperationHandle operationHandle,
            FetchOrientation orientation,
            int maxRows,
            boolean fetchLog) throws TaierSQLException {
        maxRowsLimit.ifPresent(limit -> {
            if (maxRows > limit) {
                throw new IllegalArgumentException("Max rows for fetching results " +
                        "operation should not exceed the limit: " + limit);
            }
        });

        return getSessionManager().getOperationManager()
                .getOperation(operationHandle)
                .getSession()
                .fetchResults(operationHandle, orientation, maxRows, fetchLog);
    }

    @Override
    public void initialize(TaierConf conf) {
        addService(getSessionManager());
        super.initialize(conf);
        this.timeout = Duration.ofSeconds(5).toMillis();
        this.maxRowsLimit = Optional.ofNullable(50000);
    }

    private void debug(String message) {
        // Implement debug logging here
    }
}
