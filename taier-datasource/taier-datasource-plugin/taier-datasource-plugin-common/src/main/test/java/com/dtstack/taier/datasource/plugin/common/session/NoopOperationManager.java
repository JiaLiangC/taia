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


import com.dtstack.taier.datasource.plugin.common.operation.Operation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationManager;

import java.util.List;
import java.util.Map;


public class NoopOperationManager extends OperationManager {
    private final String invalid = "invalid";

    public NoopOperationManager() {
        super("noop");
    }

    @Override
    public Operation newExecuteStatementOperation(
            Session session,
            String statement,
            Map<String, String> confOverlay,
            boolean runAsync,
            long queryTimeout) {
        Operation operation = new NoopOperation(session, statement.equals(invalid));
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newSetCurrentCatalogOperation(Session session, String catalog) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetCurrentCatalogOperation(Session session) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newSetCurrentDatabaseOperation(Session session, String database) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetCurrentDatabaseOperation(Session session) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetTypeInfoOperation(Session session) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetCatalogsOperation(Session session) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetSchemasOperation(
            Session session,
            String catalog,
            String schema) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetTablesOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> tableTypes) {
        Operation operation = new NoopOperation(session, schemaName.equals(invalid));
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetTableTypesOperation(Session session) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetColumnsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName,
            String columnName) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetFunctionsOperation(
            Session session,
            String catalogName,
            String schemaName,
            String functionName) {
        Operation operation = new NoopOperation(session);
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetPrimaryKeysOperation(
            Session session,
            String catalogName,
            String schemaName,
            String tableName) {
        Operation operation = new NoopOperation(session, schemaName.equals(invalid));
        addOperation(operation);
        return operation;
    }

    @Override
    public Operation newGetCrossReferenceOperation(
            Session session,
            String primaryCatalog,
            String primarySchema,
            String primaryTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) {
        Operation operation = new NoopOperation(session, primarySchema.equals(invalid));
        addOperation(operation);
        return operation;
    }


    @Override
    public String getQueryId(Operation operation) {
        return "noop_query_id";
    }
}
