package com.dtstack.taier.datasource.plugin.common.jdbc;



import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.Operation;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.dtstack.taier.datasource.plugin.common.util.ReflectUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public  class JdbcDialect  {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JdbcDialect.class);

    public Statement createStatement(Connection connection, int fetchSize) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(fetchSize);
        return statement;
    }

    public Statement createStatement(Connection connection) throws SQLException {
        return createStatement(connection, 1000);
    }



    public Operation getTableTypesOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }


    public Operation getFunctionsOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    public Operation getPrimaryKeysOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

    public Operation getCrossReferenceOperation(Session session) throws TaierSQLException {
        throw TaierSQLException.featureNotSupported();
    }

}

