package com.dtstack.taier.datasource.plugin.common.operation;

import java.util.Objects;
import java.util.UUID;


public class OperationHandle {

    private final UUID identifier;
    private boolean hasResultSet;

    public OperationHandle(UUID identifier) {
        this.identifier = identifier;
    }

    public void setHasResultSet(boolean hasResultSet) {
        this.hasResultSet = hasResultSet;
    }

    public UUID getIdentifier() {
        return identifier;
    }


    @Override
    public String toString() {
        return "OperationHandle [" + identifier + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationHandle that = (OperationHandle) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    public static OperationHandle create() {
        return new OperationHandle(UUID.randomUUID());
    }


    public static OperationHandle fromString(String operationHandleStr) {
        return new OperationHandle(UUID.fromString(operationHandleStr));
    }
}

