package com.dtstack.taier.datasource.plugin.common.jdbc.operation;


import java.util.*;
import java.util.stream.Collectors;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.utils.Utils;
import com.dtstack.taier.datasource.plugin.common.operation.AbstractOperation;
import com.dtstack.taier.datasource.plugin.common.operation.FetchIterator;
import com.dtstack.taier.datasource.plugin.common.operation.OperationState;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.jdbc.JdbcDialect;
import com.dtstack.taier.datasource.plugin.common.jdbc.Row;
import com.dtstack.taier.datasource.plugin.common.jdbc.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcOperation extends AbstractOperation {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JdbcOperation.class);
    protected Schema schema;
    protected FetchIterator<Row> iter;

    //    protected final TaierConf conf;
    protected final JdbcDialect dialect;

    public JdbcOperation(Session session) {
        super(session);
//        this.conf = ((JdbcSessionImpl) session).getSessionConf();
        this.dialect = new JdbcDialect();
    }

    public void validateFetchOrientation(FetchOrientation order) throws TaierSQLException {
        validateDefaultFetchOrientation(order);
    }

    @Override
    public List<Map<String, Object>> getNextRowSetInternal(FetchOrientation order, int rowSetSize) throws TaierSQLException {
        validateFetchOrientation(order);
        assertState(OperationState.FINISHED);
        setHasResultSet(true);
        switch (order) {
            case FETCH_NEXT:
                iter.fetchNext();
                break;
            case FETCH_PRIOR:
                iter.fetchPrior(rowSetSize);
                break;
            case FETCH_FIRST:
                iter.fetchAbsolute(0);
                break;
        }

        List<Row> taken = iter.take(rowSetSize).collect(Collectors.toList());
         return  toTResultMap(taken,schema);
    }


    @Override
    public List<Column> getResultSetMetadata() {
        return schema.getColumns();
    }


    public JdbcDialect getDialect() {
        return dialect;
    }

    @Override
    public void cancel() {
        cleanup(OperationState.CANCELED);
    }

    @Override
    public void close() {
        cleanup(OperationState.CLOSED);
    }

    protected void onError(boolean cancel, Throwable e) throws TaierSQLException {
        withLockRequired(() -> {
            String errMsg = Utils.stringifyException(e);
            if (state == OperationState.TIMEOUT) {
                TaierSQLException ke = new TaierSQLException("Timeout operating " + opType + ": " + errMsg);
                setOperationException(ke);
                try {
                    throw ke;
                } catch (TaierSQLException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (isTerminalState(state)) {
                setOperationException(new TaierSQLException(errMsg));
                LOGGER.warn("Ignore exception in terminal state with " + getStatementId() + ": " + errMsg);
            } else {
                LOGGER.error("Error operating " + opType + ": " + errMsg, e);
                TaierSQLException ke = new TaierSQLException("Error operating " + opType + ": " + errMsg, e);
                setOperationException(ke);
                setState(OperationState.ERROR);
                try {
                    throw ke;
                } catch (TaierSQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        });
    }


    @Override
    protected void beforeRun() {
        setState(OperationState.PENDING);
        setHasResultSet(true);
    }

    @Override
    protected void afterRun() {}


    @Override
    public boolean shouldRunAsync() {
        return false;
    }
}

