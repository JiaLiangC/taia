package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.Operation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.dtstack.taier.datasource.plugin.common.config.TaierConf.SESSION_IDLE_TIMEOUT;

public abstract class AbstractSession implements Session {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractSession.class);
    protected final String user;
    protected final String password;
    protected final String ipAddress;
    protected final Map<String, String> conf;
    protected final SessionManager sessionManager;
    protected final SessionHandle handle;

    private final long createTime;
    private volatile long lastAccessTime;
    private volatile long lastIdleTime;
    private final Set<OperationHandle> opHandleSet;

    protected final Map<String, String> normalizedConf;
    protected final Optional<String> name;

    public AbstractSession( String user, String password,
                           String ipAddress, Map<String, String> conf,
                           SessionManager sessionManager) {
        this.user = user;
        this.password = password;
        this.ipAddress = ipAddress;
        this.conf = conf;
        this.sessionManager = sessionManager;
        this.handle = SessionHandle.apply();

        this.createTime = System.currentTimeMillis();
        this.lastAccessTime = createTime;
        this.lastIdleTime = createTime;
        this.opHandleSet = ConcurrentHashMap.newKeySet();

        this.normalizedConf = sessionManager.validateAndNormalizeConf(conf);
        this.name = Optional.ofNullable(normalizedConf.get("session_name"));
    }



    @Override
    public SessionHandle getHandle() {
        return handle;
    }

    @Override
    public Optional<String> getName() {
        return name;
    }

    @Override
    public Map<String, String> getConf() {
        return conf;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public String getClientIpAddress() {
        return conf.getOrDefault("taier.client.ipAddress", ipAddress);
    }

    protected void logSessionInfo(String msg) {
        LOGGER.info(String.format("[%s:%s] %s - %s", user, getClientIpAddress(), handle, msg));
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public long getLastIdleTime() {
        return lastIdleTime;
    }

    @Override
    public long getNoOperationTime() {
        return lastIdleTime > 0 ? System.currentTimeMillis() - lastIdleTime : 0;
    }

    @Override
    public long getSessionIdleTimeoutThreshold() {
        return sessionManager.getConf().get(SESSION_IDLE_TIMEOUT) ;
    }

    private synchronized void acquire(boolean userAccess) {
        if (userAccess) {
            lastAccessTime = System.currentTimeMillis();
        }
        lastIdleTime = 0;
    }

    private synchronized void release(boolean userAccess) {
        if (userAccess) {
            lastAccessTime = System.currentTimeMillis();
        }
        if (opHandleSet.isEmpty()) {
            lastIdleTime = System.currentTimeMillis();
        }
    }

    protected <T> T withAcquireRelease(boolean userAccess, Supplier<T> f) {
        acquire(userAccess);
        try {
            return f.get();
        } finally {
            release(userAccess);
        }
    }

    @Override
    public void close() {
        withAcquireRelease(true, () -> {
            for (OperationHandle opHandle : opHandleSet) {
                try {
                    sessionManager.getOperationManager().closeOperation(opHandle);
                } catch (Exception e) {
                    LOGGER.warn("Error closing operation " + opHandle + " during closing " + handle, e);
                }
            }
            return null;
        });
    }

    protected OperationHandle runOperation(Operation operation)  {
        OperationHandle opHandle = operation.getHandle();
        opHandleSet.add(opHandle);
        try {
            operation.run();
        } catch (TaierSQLException e) {
            throw new RuntimeException(e);
        }
        return opHandle;
    }


    @Override
    public OperationHandle executeStatement(String statement, Map<String, String> confOverlay,
                                            boolean runAsync, long queryTimeout) {
        return withAcquireRelease(true, () -> {
            Operation operation = sessionManager.getOperationManager()
                    .newExecuteStatementOperation(this, statement, confOverlay, runAsync, queryTimeout);
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getTableTypes() {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager().newGetTableTypesOperation(this);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getTypeInfo() {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager().newGetTypeInfoOperation(this);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getCatalogs() {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager().newGetCatalogsOperation(this);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getSchemas(String catalogName, String schemaName) {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager()
                        .newGetSchemasOperation(this, catalogName, schemaName);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getTables(String catalogName, String schemaName,
                                     String tableName, List<String> tableTypes) {
        return withAcquireRelease(true, () -> {
            Operation operation = sessionManager.getOperationManager()
                    .newGetTablesOperation(this, catalogName, schemaName, tableName, tableTypes);
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getColumns(String catalogName, String schemaName,
                                      String tableName, String columnName) {
        return withAcquireRelease(true, () -> {
            Operation operation = sessionManager.getOperationManager()
                    .newGetColumnsOperation(this, catalogName, schemaName, tableName, columnName);
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getFunctions(String catalogName, String schemaName, String functionName) {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager()
                        .newGetFunctionsOperation(this, catalogName, schemaName, functionName);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getPrimaryKeys(String catalogName, String schemaName, String tableName) {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager()
                        .newGetPrimaryKeysOperation(this, catalogName, schemaName, tableName);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public OperationHandle getCrossReference(String primaryCatalog, String primarySchema,
                                             String primaryTable, String foreignCatalog,
                                             String foreignSchema, String foreignTable) {
        return withAcquireRelease(true, () -> {
            Operation operation = null;
            try {
                operation = sessionManager.getOperationManager()
                        .newGetCrossReferenceOperation(this, primaryCatalog, primarySchema, primaryTable,
                                foreignCatalog, foreignSchema, foreignTable);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return runOperation(operation);
        });
    }

    @Override
    public String getQueryId(OperationHandle operationHandle) throws TaierSQLException {
        Operation operation = sessionManager.getOperationManager().getOperation(operationHandle);
        return sessionManager.getOperationManager().getQueryId(operation);
    }

    @Override
    public void cancelOperation(OperationHandle operationHandle) {
        withAcquireRelease(true, () -> {
            try {
                sessionManager.getOperationManager().cancelOperation(operationHandle);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void closeOperation(OperationHandle operationHandle) {
        withAcquireRelease(true, () -> {
            opHandleSet.remove(operationHandle);
            try {
                sessionManager.getOperationManager().closeOperation(operationHandle);
            } catch (TaierSQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public List<Column> getResultSetMetadata(OperationHandle operationHandle) {
        return withAcquireRelease(true, () ->
                {
                    try {
                        return sessionManager.getOperationManager().getOperationResultSetSchema(operationHandle);
                    } catch (TaierSQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Override
    public  List<Map<String, Object>> fetchResults(OperationHandle operationHandle,
                                                   FetchOrientation orientation,
                                                   int maxRows, boolean fetchLog) {
        return withAcquireRelease(true, () -> {
            if (fetchLog) {
                try {
                    return sessionManager.getOperationManager()
                            .getOperationLogRowSet(operationHandle, orientation, maxRows);
                } catch (TaierSQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    return sessionManager.getOperationManager()
                            .getOperationNextRowSet(operationHandle, orientation, maxRows);
                } catch (TaierSQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void closeExpiredOperations() {
        List<Operation> operations = sessionManager.getOperationManager()
                .removeExpiredOperations(new ArrayList<>(opHandleSet));
        for (Operation op : operations) {
            withAcquireRelease(false, () -> {
                    opHandleSet.remove(op.getHandle());
                    try {
                        op.close();
                    } catch (Exception e) {
                        LOGGER.warn("Error closing timed-out operation " + op.getHandle(), e);
                    }

                return null;
            });
        }
    }

    public boolean isForAliveProbe() {
        return false;
    }


}
