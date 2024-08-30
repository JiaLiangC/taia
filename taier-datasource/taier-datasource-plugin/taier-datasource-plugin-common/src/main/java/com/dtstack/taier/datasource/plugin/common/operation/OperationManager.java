package com.dtstack.taier.datasource.plugin.common.operation;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.service.AbstractService;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The {@link OperationManager} manages all the operations during their lifecycle.
 */
public abstract class OperationManager extends AbstractService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OperationManager.class);
    private final HashMap<OperationHandle, Operation> handleToOperation = new HashMap<>();

    protected boolean skipOperationLog = false;

    private static final String PATTERN_FOR_SET_CATALOG = "_SET_CATALOG";
    private static final String PATTERN_FOR_GET_CATALOG = "_GET_CATALOG";
    private static final Pattern PATTERN_FOR_SET_SCHEMA = Pattern.compile("(?i)use (.*)");
    private static final String PATTERN_FOR_GET_SCHEMA = "select current_database()";

    public OperationManager(String name) {
        super(name);
    }

    public int getOperationCount() {
        return handleToOperation.size();
    }

    public Iterable<Operation> allOperations() {
        return handleToOperation.values();
    }

    @Override
    public void initialize(TaierConf conf) {
        super.initialize(conf);
    }

    public abstract Operation newExecuteStatementOperation(
            Session session,
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout);

    public abstract Operation newSetCurrentCatalogOperation(Session session, String catalog) throws TaierSQLException;
    public abstract Operation newGetCurrentCatalogOperation(Session session) throws TaierSQLException;
    public abstract Operation newSetCurrentDatabaseOperation(Session session, String database) throws TaierSQLException;
    public abstract Operation newGetCurrentDatabaseOperation(Session session) throws TaierSQLException;
    public abstract Operation newGetTypeInfoOperation(Session session) throws TaierSQLException;
    public abstract Operation newGetCatalogsOperation(Session session) throws TaierSQLException;
    public abstract Operation newGetSchemasOperation(Session session, String catalog, String schema) throws TaierSQLException;
    public abstract Operation newGetTablesOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes);
    public abstract Operation newGetTableTypesOperation(Session session) throws TaierSQLException;
    public abstract Operation newGetColumnsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            String columnName);
    public abstract Operation newGetFunctionsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String functionName) throws TaierSQLException;
    public abstract Operation newGetPrimaryKeysOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName) throws TaierSQLException;
    public abstract Operation newGetCrossReferenceOperation(
            Session session,
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) throws TaierSQLException;
    public abstract String getQueryId(Operation operation) throws TaierSQLException;

    public final synchronized Operation addOperation(Operation operation) {
        handleToOperation.put(operation.getHandle(), operation);
        return operation;
    }

    public final Operation getOperation(OperationHandle opHandle) throws TaierSQLException {
        Operation operation;
        synchronized (this) {
            operation = handleToOperation.get(opHandle);
        }
        if (operation == null) {
            throw new TaierSQLException("Invalid " + opHandle);
        }
        return operation;
    }

    public final synchronized Operation removeOperation(OperationHandle opHandle) throws TaierSQLException {
        Operation operation = handleToOperation.remove(opHandle);
        if (operation == null) {
            throw new TaierSQLException("Invalid " + opHandle);
        }
        return operation;
    }

    public final void cancelOperation(OperationHandle opHandle) throws TaierSQLException {
        Operation operation = getOperation(opHandle);
        switch (operation.getStatus().getState()) {
            case CANCELED:
            case CLOSED:
            case FINISHED:
            case ERROR:
            case UNKNOWN:
                break;
            default:
                operation.cancel();
        }
    }

    public final void closeOperation(OperationHandle opHandle) throws TaierSQLException {
        Operation operation = removeOperation(opHandle);
        operation.close();
    }

    public final List<Column> getOperationResultSetSchema(OperationHandle opHandle) throws TaierSQLException {
        return getOperation(opHandle).getResultSetMetadata();
    }

    public final List<Map<String, Object>> getOperationNextRowSet(
            OperationHandle opHandle,
            FetchOrientation order,
            int maxRows) throws TaierSQLException {
        return getOperation(opHandle).getNextRowSet(order, maxRows);
    }


    //todo
    public List<Map<String, Object>> getOperationLogRowSet(
            OperationHandle opHandle,
            FetchOrientation order,
            int maxRows) throws TaierSQLException {
        Operation operation = getOperation(opHandle);
        OperationLog operationLog = operation.getOperationLog().get();
        if (operationLog == null) {
            throw new TaierSQLException(opHandle + " failed to generate operation log");
        }
        return new ArrayList<>();
    }

    public final synchronized List<Operation> removeExpiredOperations(List<OperationHandle> handles) {
        return handles.stream()
                .map(handleToOperation::get)
                .filter(operation -> {
                    if (operation != null && operation.isTimedOut()) {
                        handleToOperation.remove(operation.getHandle());
                        LOGGER.warn("Operation " + operation.getHandle() + " is timed-out and will be closed");
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
    }

}




