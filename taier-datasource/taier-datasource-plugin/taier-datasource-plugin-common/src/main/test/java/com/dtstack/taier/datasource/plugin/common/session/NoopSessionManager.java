package com.dtstack.taier.datasource.plugin.common.session;

import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;

import java.sql.Connection;
import java.util.Map;

public class NoopSessionManager extends SessionManager {
    private Connection sessionConnection;
    private OperationManager operationManager;

    public NoopSessionManager() {
        super("noop");
        this.operationManager=new NoopOperationManager();
    }

    @Override
    public OperationManager getOperationManager() {
        return new NoopOperationManager();
    }

    @Override
    protected Session createSession(Connection connection, String user, String password, String ipAddress, Map<String, String> conf) {
        this.sessionConnection =connection;
        return new NoopSessionImpl(user, password, ipAddress, conf, this);
    }

    @Override
    protected boolean isServer() {
        return true;
    }
}