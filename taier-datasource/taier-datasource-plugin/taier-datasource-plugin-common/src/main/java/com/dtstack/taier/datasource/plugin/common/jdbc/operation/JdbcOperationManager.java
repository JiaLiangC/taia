package com.dtstack.taier.datasource.plugin.common.jdbc.operation;

import java.util.List;
import java.util.Map;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcDialect;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcSessionImpl;
import com.dtstack.taier.datasource.plugin.common.operation.Operation;

import static com.dtstack.taier.datasource.plugin.common.config.TaierConf.ENGINE_JDBC_FETCH_SIZE;

public class JdbcOperationManager extends OperationManager {


    private JdbcDialect dialect;

    public JdbcOperationManager(TaierConf conf) {
        super("JdbcOperationManager");
        this.conf = conf;
    }

    public String name() {
        return "jdbc";
    }

    private JdbcDialect getDialect() {
//        if (dialect == null) {
//            dialect = JdbcDialectsFactory.get();
//        }
        return dialect;
    }

    @Override
    public Operation newExecuteStatementOperation(
            Session session,
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout) {
        JdbcSessionImpl jdbcSession = (JdbcSessionImpl) session;

        boolean incrementalCollect = false;

        int fetchSize = conf.get(ENGINE_JDBC_FETCH_SIZE);
        ExecuteStatement executeStatement = new ExecuteStatement(
                session,
                statement,
                runAsync,
                queryTimeout,
                incrementalCollect,
                fetchSize
        );
        addOperation(executeStatement);
        return executeStatement;
    }

    //todo 待实现和taier 体系打通
    @Override
    public Operation newGetTypeInfoOperation(Session session) throws TaierSQLException {
        return null;
    }

    @Override
    public Operation newGetCatalogsOperation(Session session) throws TaierSQLException {
        return null;
    }

    @Override
    public Operation newGetSchemasOperation(Session session, String catalog, String schema) throws TaierSQLException {
        return null;
    }

    @Override
    public Operation newGetTablesOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes) {
        return null;
    }

    @Override
    public Operation newGetTableTypesOperation(Session session) throws TaierSQLException {
        Operation operation = getDialect().getTableTypesOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetColumnsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            String columnName) {
        return null;
    }

    @Override
    public Operation newGetFunctionsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String functionName) throws TaierSQLException {
        Operation operation = getDialect().getFunctionsOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetPrimaryKeysOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName) throws TaierSQLException {
        Operation operation = getDialect().getPrimaryKeysOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetCrossReferenceOperation(
            Session session,
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) throws TaierSQLException {
        Operation operation = getDialect().getCrossReferenceOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public String getQueryId(Operation operation) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    @Override
    public Operation newSetCurrentCatalogOperation(Session session, String catalog) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    @Override
    public Operation newGetCurrentCatalogOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    @Override
    public Operation newSetCurrentDatabaseOperation(Session session, String database) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    @Override
    public Operation newGetCurrentDatabaseOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }
}
