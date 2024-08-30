package com.dtstack.taier.datasource.plugin.common.jdbc;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultSetWrapper implements Iterator<Row> {

    private Statement statement;
    private ResultSet currentResult;
    private ResultSetMetaData metadata;

    public ResultSetWrapper(Statement statement) throws SQLException {
        this.statement = statement;
        this.currentResult = statement.getResultSet();
        this.metadata = currentResult.getMetaData();
    }

    @Override
    public boolean hasNext() {
        try {
            if (currentResult == null) return false;
            boolean result = currentResult.next();
            if (!result) {
                boolean hasMoreResults = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
                if (hasMoreResults) {
                    currentResult = statement.getResultSet();
                    return currentResult.next();
                } else {
                    currentResult = null;
                    return false;
                }
            } else {
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Row next() {
        try {
            return toRow();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Row[] toArray() throws SQLException {
        List<Row> result = new ArrayList<>();
        while (currentResult.next()) {
            Row row = toRow();
            result.add(row);
        }
        return result.toArray(new Row[0]);
    }

    private Row toRow() throws SQLException {
        List<Object> buffer = new ArrayList<>();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            Object value = currentResult.getObject(i);
            buffer.add(value);
        }
        return new Row(buffer);
    }

    public ResultSetMetaData getMetadata() {
        return this.metadata;
    }
}
