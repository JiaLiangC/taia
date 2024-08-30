package com.dtstack.taier.datasource.plugin.common.exception;

import com.dtstack.taier.datasource.plugin.common.reflect.DynConstructors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaierSQLException extends SQLException {

    private static final String HEAD_MARK = "*";
    private static final char SEPARATOR = ':';

    public TaierSQLException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }

    // for reflection
    public TaierSQLException(String msg, Throwable cause) {
        this(msg, null, 0, cause);
    }
    public TaierSQLException(Throwable cause) {
        this(null, null, 0, cause);
    }

    public TaierSQLException(String msg) {
        this(msg, null, 0, null);
    }

//    public TStatus toTStatus() {
//        TStatus tStatus = new TStatus(TStatusCode.ERROR_STATUS);
//        tStatus.setSqlState(getSQLState());
//        tStatus.setErrorCode(getErrorCode());
//        tStatus.setErrorMessage(getMessage());
//        tStatus.setInfoMessages(TaierSQLException.toString(this));
//        return tStatus;
//    }

    public static TaierSQLException apply(String msg, Throwable cause, String sqlState, int vendorCode) {
        return new TaierSQLException(msg, sqlState, vendorCode, findCause(cause));
    }

    public static TaierSQLException apply(Throwable cause) {
        Throwable theCause = findCause(cause);
        return apply(theCause.getMessage(), theCause, null, 0);
    }
//
//    public static TaierSQLException apply(TStatus tStatus) {
//        String msg = tStatus.getErrorMessage();
//        Throwable cause = toCause(tStatus.getInfoMessages());
//        if (cause instanceof TaierSQLException && ((TaierSQLException) cause).getMessage().equals(msg)) {
//            return (TaierSQLException) cause;
//        } else {
//            return apply(msg, cause, tStatus.getSqlState(), tStatus.getErrorCode());
//        }
//    }

    public static TaierSQLException featureNotSupported() {
        return apply("feature not supported" , null, "0A000", 0);
    }

    public static TaierSQLException featureNotSupported(String message) {
        return apply(message == null ? "feature not supported" : message, null, "0A000", 0);
    }

    public static TaierSQLException connectionDoesNotExist() {
        return new TaierSQLException("connection does not exist", "08003", 91001, null);
    }

//    public static TStatus toTStatus(Exception e, boolean verbose) {
//        if (e instanceof TaierSQLException) {
//            return ((TaierSQLException) e).toTStatus();
//        } else {
//            TStatus tStatus = new TStatus(TStatusCode.ERROR_STATUS);
//            String errMsg = verbose ? Utils.stringifyException(e) : e.getMessage();
//            tStatus.setErrorMessage(errMsg);
//            tStatus.setInfoMessages(toString(e));
//            return tStatus;
//        }
//    }

    public static List<String> toString(Throwable cause) {
        return toString(cause, null);
    }

    public static List<String> toString(Throwable cause, StackTraceElement[] parent) {
        StackTraceElement[] trace = cause.getStackTrace();
        int m = trace.length - 1;

        if (parent != null) {
            int n = parent.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(parent[n])) {
                m--;
                n--;
            }
        }

        List<String> result = new ArrayList<>(enroll(cause, trace, m));
        if (cause.getCause() != null) {
            result.addAll(toString(cause.getCause(), trace));
        }
        return result;
    }

    private static List<String> enroll(Throwable ex, StackTraceElement[] trace, int max) {
        StringBuilder builder = new StringBuilder();
        builder.append(HEAD_MARK).append(ex.getClass().getName()).append(SEPARATOR);
        builder.append(ex.getMessage()).append(SEPARATOR);
        builder.append(trace.length).append(SEPARATOR).append(max);

        List<String> result = new ArrayList<>();
        result.add(builder.toString());

        for (int i = 0; i <= max; i++) {
            builder.setLength(0);
            builder.append(trace[i].getClassName()).append(SEPARATOR);
            builder.append(trace[i].getMethodName()).append(SEPARATOR);
            builder.append(Optional.ofNullable(trace[i].getFileName()).orElse("")).append(SEPARATOR);
            builder.append(trace[i].getLineNumber());
            result.add(builder.toString());
        }

        return result;
    }

    private static Throwable newInstance(String className, String message, Throwable cause) {
        try {
            return (Throwable) DynConstructors.builder()
                    .impl(className, String.class, Throwable.class)
                    .buildChecked()
                    .<Throwable>newInstance(message, cause);
        } catch (Exception e) {
            return new RuntimeException(className + ":" + message, cause);
        }
    }

    private static int[] getCoordinates(String line) {
        int i1 = line.indexOf(SEPARATOR);
        int i3 = line.lastIndexOf(SEPARATOR);
        int i2 = line.substring(0, i3).lastIndexOf(SEPARATOR);
        return new int[]{i1, i2, i3};
    }

    public static Throwable toCause(List<String> details) {
        Throwable ex = null;
        if (details != null && !details.isEmpty()) {
            String head = details.get(0);
            int[] coords = getCoordinates(head);
            String exClz = head.substring(1, coords[0]);
            String msg = head.substring(coords[0] + 1, coords[1]);
            int length = Integer.parseInt(head.substring(coords[2] + 1));

            List<StackTraceElement> stackTraceElements = details.subList(1, length + 2).stream()
                    .map(line -> {
                        int[] c = getCoordinates(line);
                        String clzName = line.substring(0, c[0]);
                        String methodName = line.substring(c[0] + 1, c[1]);
                        String fileName = line.substring(c[1] + 1, c[2]);
                        int lineNum = Integer.parseInt(line.substring(c[2] + 1));
                        return new StackTraceElement(clzName, methodName, fileName, lineNum);
                    })
                    .collect(Collectors.toList());

            ex = newInstance(exClz, msg, toCause(details.subList(length + 2, details.size())));
            ex.setStackTrace(stackTraceElements.toArray(new StackTraceElement[0]));
        }
        return ex;
    }

    public static Throwable findCause(Throwable t) {
        while (t instanceof UndeclaredThrowableException || t instanceof InvocationTargetException) {
            if (t.getCause() == null) {
                break;
            }
            t = t.getCause();
        }
        return t;
    }
}
