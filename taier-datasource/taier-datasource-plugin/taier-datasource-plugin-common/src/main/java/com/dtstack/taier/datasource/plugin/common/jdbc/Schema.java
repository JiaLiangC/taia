package com.dtstack.taier.datasource.plugin.common.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Schema {
    private final List<Column> columns;

    public Schema(List<Column> columns) {
        this.columns = columns;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public static Schema apply(ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount();
        List<Column> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            String name = metadata.getColumnName(i);
            String typeName = metadata.getColumnTypeName(i);
            int sqlType = metadata.getColumnType(i);
            int precision = metadata.getPrecision(i);
            int scale = metadata.getScale(i);
            String label = metadata.getColumnLabel(i);
            int displaySize = metadata.getColumnDisplaySize(i);
            Column column = new Column(name, typeName, sqlType, precision, scale, label, displaySize);
            columns.add(column);
        }
        return new Schema(columns);
    }
}
