package com.dtstack.taier.datasource.plugin.common.operation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public enum OperationState {
    INITIALIZED, PENDING, RUNNING, COMPILED, FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR, UNKNOWN;

    private static final List<OperationState> terminalStates = Arrays.asList(
            FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR);

    public static void validateTransition(OperationState oldState, OperationState newState)  {
        switch (oldState) {
            case INITIALIZED:
                if (EnumSet.of(PENDING, RUNNING, TIMEOUT, CANCELED, CLOSED).contains(newState)) return;
                break;
            case PENDING:
                if (EnumSet.of(RUNNING, COMPILED, FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR).contains(newState)) return;
                break;
            case RUNNING:
                if (EnumSet.of(COMPILED, FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR).contains(newState)) return;
                break;
            case COMPILED:
                if (EnumSet.of(FINISHED, TIMEOUT, CANCELED, CLOSED, ERROR).contains(newState)) return;
                break;
            case FINISHED:
            case CANCELED:
            case TIMEOUT:
            case ERROR:
                if (newState == CLOSED) return;
                break;
        }
//        throw new TaierSQLException(
//                String.format("Illegal Operation state transition from %s to %s", oldState, newState));
    }

    public static boolean isTerminal(OperationState state) {
        return terminalStates.contains(state);
    }
}
