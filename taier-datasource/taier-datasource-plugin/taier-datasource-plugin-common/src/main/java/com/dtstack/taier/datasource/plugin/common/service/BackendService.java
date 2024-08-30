package com.dtstack.taier.datasource.plugin.common.service;


import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import com.dtstack.taier.datasource.plugin.common.operation.OperationStatus;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A BackendService in Taier architecture is responsible for talking to the SQL engine
 *
 * 1. Open/Close com.dtstack.taier.common.session.Session <br/>
 * 2. Operate com.dtstack.taier.common.operation.Operation <br/>
 * 3. Manager com.dtstack.taier.common.session.Sessions via SessionManager <br/>
 * 4. Check OperationStatus <br/>
 * 5. Retrieve com.dtstack.taier.common.operation.Operation results and metadata <br/>
 * 6. Cancel/Close com.dtstack.taier.common.operation.Operation <br/>
 */
public interface BackendService {

    SessionHandle openSession(
            Connection connection,
            String user,
            String password,
            String ipAddr,
            Map<String, String> configs) throws TaierSQLException;

    void closeSession(SessionHandle sessionHandle) throws TaierSQLException;

//    TGetInfoValue getInfo(SessionHandle sessionHandle, TGetInfoType infoType);

    OperationHandle executeStatement(
            SessionHandle sessionHandle,
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout) throws TaierSQLException;

    OperationHandle getTypeInfo(SessionHandle sessionHandle) throws TaierSQLException;

    OperationHandle getCatalogs(SessionHandle sessionHandle) throws TaierSQLException;

    OperationHandle getSchemas(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName) throws TaierSQLException;

    OperationHandle getTables(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes) throws TaierSQLException;

    OperationHandle getTableTypes(SessionHandle sessionHandle) throws TaierSQLException;

    OperationHandle getColumns(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName,
            String columnName) throws TaierSQLException;

    OperationHandle getFunctions(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String functionName) throws TaierSQLException;

    OperationHandle getPrimaryKeys(
            SessionHandle sessionHandle,
            String catalogName,
            String schemaName,
            String tableName) throws TaierSQLException;

    OperationHandle getCrossReference(
            SessionHandle sessionHandle,
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) throws TaierSQLException;

    String getQueryId(OperationHandle operationHandle) throws TaierSQLException;

    OperationStatus getOperationStatus(
            OperationHandle operationHandle,
            Optional<Long> maxWait) throws TaierSQLException;

    void cancelOperation(OperationHandle operationHandle) throws TaierSQLException;

    void closeOperation(OperationHandle operationHandle) throws TaierSQLException;

    List<Column> getResultSetMetadata(OperationHandle operationHandle) throws TaierSQLException;

    List<Map<String, Object>> fetchResults(
            OperationHandle operationHandle,
            FetchOrientation orientation,
            int maxRows,
            boolean fetchLog) throws TaierSQLException;

    SessionManager getSessionManager();
}
