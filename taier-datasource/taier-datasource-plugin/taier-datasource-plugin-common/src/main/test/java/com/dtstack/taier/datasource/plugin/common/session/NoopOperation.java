package com.dtstack.taier.datasource.plugin.common.session;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.jdbc.Column;
import com.dtstack.taier.datasource.plugin.common.jdbc.Row;
import com.dtstack.taier.datasource.plugin.common.jdbc.Schema;
import com.dtstack.taier.datasource.plugin.common.operation.AbstractOperation;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationLog;
import com.dtstack.taier.datasource.plugin.common.operation.OperationState;

import java.nio.ByteBuffer;
import java.util.*;


public class NoopOperation extends AbstractOperation {
    private final boolean shouldFail;

    public NoopOperation(Session session) {
        this(session, false);
    }

    public NoopOperation(Session session, boolean shouldFail) {
        super(session);
        this.shouldFail = shouldFail;
    }

    @Override
    protected void runInternal() {
        setState(OperationState.RUNNING);
        if (shouldFail) {
            TaierSQLException exception = new TaierSQLException("noop operation err");
            setOperationException(exception);
            setState(OperationState.ERROR);
        }
        setHasResultSet(true);
    }

    @Override
    protected void beforeRun() {
        setState(OperationState.PENDING);
    }

    @Override
    protected void afterRun() {
        if (!OperationState.isTerminal(getState())) {
            setState(OperationState.FINISHED);
        }
    }

    @Override
    public void cancel() {
        setState(OperationState.CANCELED);
    }

    @Override
    public void close() {
        setState(OperationState.CLOSED);
    }

    @Override
    public List<Column> getResultSetMetadata() {
        return Collections.emptyList();
    }


    @Override
    public List<Map<String, Object>> getNextRowSetInternal(FetchOrientation order, int rowSetSize) {

        // Step 1: Create Schema
        List<Column> columns = Arrays.asList(
                new Column("id", "ID", 1, 0, 0, "", 100),
                new Column("name", "Name", 1, 0, 0, "", 100),
                new Column("age", "Age", 1, 0, 0, "", 100)
        );
        Schema schema = new Schema(columns);

        // Step 2: Create list of Row objects
        List<Row> rowList = Arrays.asList(
                new Row(Arrays.asList(1, "Alice", 30)),
                new Row(Arrays.asList(2, "Bob", 25)),
                new Row(Arrays.asList(3, "Charlie", 35))
        );

        List<Map<String, Object>> result = toTResultMap(rowList, schema);

        return result;
    }

    @Override
    public boolean shouldRunAsync() {
        return false;
    }

    @Override
    public Optional<OperationLog> getOperationLog() {
        return Optional.empty();
    }
}