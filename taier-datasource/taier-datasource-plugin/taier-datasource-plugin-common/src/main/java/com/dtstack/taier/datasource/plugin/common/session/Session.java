package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Session {

    SessionHandle getHandle();
    Optional<String> getName();

    Map<String, String> getConf();

    String getUser();
    String getPassword();
    String getIpAddress();

    long getCreateTime();
    long getLastAccessTime();
    long getLastIdleTime();
    long getNoOperationTime();
    long getSessionIdleTimeoutThreshold();

    SessionManager getSessionManager();

    void open() throws SQLException;
    void close();


    OperationHandle executeStatement(
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout);

    OperationHandle getTableTypes();
    OperationHandle getTypeInfo();
    OperationHandle getCatalogs();
    OperationHandle getSchemas(String catalogName, String schemaName);
    OperationHandle getTables(
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes);
    OperationHandle getColumns(
            String catalogName,
            String schemaName,
            String tableName,
            String columnName);
    OperationHandle getFunctions(
            String catalogName,
            String schemaName,
            String functionName);
    OperationHandle getPrimaryKeys(
            String catalogName,
            String schemaName,
            String tableName);
    OperationHandle getCrossReference(
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable);
    String getQueryId(OperationHandle operationHandle) throws TaierSQLException;

    void cancelOperation(OperationHandle operationHandle);
    void closeOperation(OperationHandle operationHandle);

    List<Column> getResultSetMetadata(OperationHandle operationHandle);
    List<Map<String, Object>> fetchResults(
            OperationHandle operationHandle,
            FetchOrientation orientation,
            int maxRows,
            boolean fetchLog);

    void closeExpiredOperations();

    boolean isForAliveProbe();
}
