package com.dtstack.taier.datasource.plugin.common.exception;

public class TaierException extends Exception {
    public TaierException(String message) {
        super(message);
    }

    public TaierException(String message, Throwable cause) {
        super(message, cause);
    }
}
