package com.dtstack.taier.develop.utils;



import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.develop.dto.devlop.SessionData;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApiUtils {


    public static SessionData sessionData(Session session) {
        return new SessionData(
                session.getHandle().getIdentifier().toString(),
                "",
                session.getUser(),
                session.getIpAddress(),
                new HashMap(),
                session.getCreateTime(),
                session.getLastAccessTime() - session.getCreateTime(),
                session.getNoOperationTime(),
                "",
                "",
                "",
                "",
                "",
                "",
                session.getName().orElse(""),
                0
        );
    }


}
