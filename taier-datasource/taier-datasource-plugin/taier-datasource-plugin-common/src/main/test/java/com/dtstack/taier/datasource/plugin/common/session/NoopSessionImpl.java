package com.dtstack.taier.datasource.plugin.common.session;



import java.util.Map;

public class NoopSessionImpl extends AbstractSession {

    public NoopSessionImpl(String user, String password, String ipAddress, Map<String, String> conf, SessionManager sessionManager) {
        super(user, password, ipAddress, conf, sessionManager);
    }

    @Override
    public void open() {
        // No-op
    }
}