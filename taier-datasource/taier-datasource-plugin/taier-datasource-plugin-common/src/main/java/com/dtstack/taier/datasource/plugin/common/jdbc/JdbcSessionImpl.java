package com.dtstack.taier.datasource.plugin.common.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.session.AbstractSession;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcSessionImpl extends AbstractSession {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JdbcSessionImpl.class);
    private SessionHandle handle;

    private Connection sessionConnection;
    private DatabaseMetaData databaseMetaData;
    private  TaierConf sessionConf;

    public JdbcSessionImpl(
            Connection externalConnection,
            String user,
            String password,
            String ipAddress,
            Map<String, String> conf,
            SessionManager sessionManager
            ) {  // 新增参数
        super(user, password, ipAddress, conf, sessionManager);
        this.handle = conf.containsKey("taier.session.handle")
                ? SessionHandle.fromUUID("taier.session.handle")
                : SessionHandle.apply();
        this.sessionConnection = externalConnection;  // 使用外部传入的 Connection
        this.sessionConf = sessionManager.getConf();
    }


    public JdbcSessionImpl(
            String user,
            String password,
            String ipAddress,
            Map<String, String> conf,
            SessionManager sessionManager) {
        super(user, password, ipAddress, conf, sessionManager);
        this.handle = conf.containsKey("taier.session.handle")
                ? SessionHandle.fromUUID("taier.session.handle")
                :  SessionHandle.apply();
    }

    @Override
    public SessionHandle getHandle() {
        return handle;
    }

    public Connection getSessionConnection() {
        return sessionConnection;
    }

    @Override
    public void open() throws SQLException {
        LOGGER.info("Starting to open jdbc session.");
        TaierJdbcUtils.initializeJdbcSession(
                this.sessionConnection,
                Collections.singletonList("SELECT 1"));
        LOGGER.info("The jdbc session is started.");
    }

    @Override
    public void close() {
        try {
            if (sessionConnection != null) {
                sessionConnection.close();
            }
            LOGGER.info("Closed session connection.");
        } catch (SQLException e) {
            LOGGER.warn("Failed to close session connection, ignored it.", e);
        }
        super.close();
    }
}
