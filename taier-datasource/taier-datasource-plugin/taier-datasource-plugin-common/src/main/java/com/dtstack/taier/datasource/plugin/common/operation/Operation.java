package com.dtstack.taier.datasource.plugin.common.operation;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;

import java.util.*;
import java.util.concurrent.Future;

public interface Operation {

    void run() throws TaierSQLException;
    void cancel();
    void close();

    List<Column> getResultSetMetadata();
    List<Map<String, Object>> getNextRowSet(FetchOrientation order, int rowSetSize);

    Session getSession();
    OperationHandle getHandle();
    OperationStatus getStatus();
    Optional<OperationLog> getOperationLog();

    Future<?> getBackgroundHandle();
    boolean shouldRunAsync();
    boolean isTimedOut();
}

class OperationConstants {
    public static final Set<FetchOrientation> DEFAULT_FETCH_ORIENTATION_SET =
            EnumSet.of(FetchOrientation.FETCH_NEXT, FetchOrientation.FETCH_FIRST, FetchOrientation.FETCH_PRIOR);
}
