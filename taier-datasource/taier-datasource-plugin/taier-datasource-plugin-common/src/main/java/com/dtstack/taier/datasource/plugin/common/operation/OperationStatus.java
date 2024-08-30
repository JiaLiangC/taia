package com.dtstack.taier.datasource.plugin.common.operation;

import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import java.util.Optional;

public class OperationStatus {
    private final OperationState state;
    private final long create;
    private final long start;
    private final long lastModified;
    private final long completed;
    private final boolean hasResultSet;
    private final Optional<TaierSQLException> exception;

    public OperationStatus(
            OperationState state,
            long create,
            long start,
            long lastModified,
            long completed,
            boolean hasResultSet,
            Optional<TaierSQLException> exception
            ) {
        this.state = state;
        this.create = create;
        this.start = start;
        this.lastModified = lastModified;
        this.completed = completed;
        this.hasResultSet = hasResultSet;
        this.exception = exception;
    }

    // Getters
    public OperationState getState() {
        return state;
    }

    public long getCreate() {
        return create;
    }

    public long getStart() {
        return start;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getCompleted() {
        return completed;
    }

    public boolean isHasResultSet() {
        return hasResultSet;
    }

    public Optional<TaierSQLException> getException() {
        return exception;
    }

//    public Optional<TProgressUpdateResp> getOperationProgressUpdate() {
//        return operationProgressUpdate;
//    }

    // Builder pattern for easier object creation
    public static class Builder {
        private OperationState state;
        private long create;
        private long start;
        private long lastModified;
        private long completed;
        private boolean hasResultSet;
        private Optional<TaierSQLException> exception = Optional.empty();
//        private Optional<TProgressUpdateResp> operationProgressUpdate = Optional.empty();

        public Builder setState(OperationState state) {
            this.state = state;
            return this;
        }

        public Builder setCreate(long create) {
            this.create = create;
            return this;
        }

        public Builder setStart(long start) {
            this.start = start;
            return this;
        }

        public Builder setLastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setCompleted(long completed) {
            this.completed = completed;
            return this;
        }

        public Builder setHasResultSet(boolean hasResultSet) {
            this.hasResultSet = hasResultSet;
            return this;
        }

        public Builder setException(TaierSQLException exception) {
            this.exception = Optional.ofNullable(exception);
            return this;
        }

        public OperationStatus build() {
            return new OperationStatus(state, create, start, lastModified, completed, hasResultSet, exception);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
