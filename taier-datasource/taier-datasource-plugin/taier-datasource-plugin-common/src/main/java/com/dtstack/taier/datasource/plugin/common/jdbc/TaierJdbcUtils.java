package com.dtstack.taier.datasource.plugin.common.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaierJdbcUtils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TaierJdbcUtils.class);

 /*   public static void initializeJdbcSession(List<String> initializationSQLs) {
        try (Connection connection = ConnectionProvider.getInstance().getConnection()) {
            initializeJdbcSession(connection, initializationSQLs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JDBC session", e);
        }
    }*/

    public static void initializeJdbcSession(
            Connection connection,
            List<String> initializationSQLs) throws TaierSQLException {
        if (initializationSQLs == null || initializationSQLs.isEmpty()) {
            return;
        }
        try {
            JdbcDialect dialect = new JdbcDialect();
            try (Statement statement = dialect.createStatement(connection)) {
                for (String sql : initializationSQLs) {
                    LOGGER.debug("Execute initialization sql: " + sql);
                    statement.execute(sql);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to execute initialization sql.", e);
            throw new TaierSQLException(e);
        }
    }
}
