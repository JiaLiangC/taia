package com.dtstack.taier.develop.service.session;

import com.dtstack.taier.datasource.plugin.common.config.TaierConf;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcSessionManager;
import com.dtstack.taier.datasource.plugin.common.service.AbstractBackendService;
import com.dtstack.taier.datasource.plugin.common.session.SessionManager;
import org.springframework.stereotype.Service;

@Service
public class JdbcBackendService extends AbstractBackendService {
    private final SessionManager sessionManager;

    public JdbcBackendService() {
        super("JdbcBackendService");
        this.sessionManager = new JdbcSessionManager();
        TaierConf tconf = new TaierConf().loadFileDefaults();
        initialize(tconf);
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

}
