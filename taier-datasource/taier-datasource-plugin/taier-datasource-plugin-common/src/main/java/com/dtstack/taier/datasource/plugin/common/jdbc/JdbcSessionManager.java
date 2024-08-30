package com.dtstack.taier.datasource.plugin.common.jdbc;

 
import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;
import com.dtstack.taier.datasource.plugin.common.jdbc.operation.JdbcOperationManager;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

public class JdbcSessionManager extends SessionManager {
    private OperationManager operationManager;


    public JdbcSessionManager() {
        this(JdbcSessionManager.class.getSimpleName());
        this.operationManager = new JdbcOperationManager(conf);
    }

    public JdbcSessionManager(String name) {
        super(name);
    }

    @Override
    protected boolean isServer() {
        return false;
    }

    @Override
    public OperationManager getOperationManager() {
        return operationManager;
    }

    @Override
    public void initialize(TaierConf conf) {
        this.conf =conf;
        super.initialize(conf);
    }

    @Override
    protected Session createSession(
            Connection connection,
            String user,
            String password,
            String ipAddress,
            Map<String, String> conf) {
        Optional<SessionHandle> sessionHandleOpt = Optional.ofNullable(conf.get("taier.session.handle"))
                .map(SessionHandle::fromUUID);
        return sessionHandleOpt.flatMap(this::getSessionOption)
                .orElseGet(() -> new JdbcSessionImpl(connection, user, password, ipAddress, conf, this));
    }

    @Override
    public void closeSession(SessionHandle sessionHandle) throws TaierSQLException {
        super.closeSession(sessionHandle);
    }


}
