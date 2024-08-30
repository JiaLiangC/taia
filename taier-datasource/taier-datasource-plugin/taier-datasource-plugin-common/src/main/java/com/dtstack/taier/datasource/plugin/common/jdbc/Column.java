package com.dtstack.taier.datasource.plugin.common.jdbc;


import java.util.Objects;

public class Column {
    private final String name;
    private final String typeName;
    private final int sqlType;
    private final int precision;
    private final int scale;
    private final String label;
    private final int displaySize;

    public Column(String name, String typeName, int sqlType, int precision, int scale, String label, int displaySize) {
        this.name = name;
        this.typeName = typeName;
        this.sqlType = sqlType;
        this.precision = precision;
        this.scale = scale;
        this.label = label;
        this.displaySize = displaySize;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getSqlType() {
        return sqlType;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public String getLabel() {
        return label;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return sqlType == column.sqlType &&
                precision == column.precision &&
                scale == column.scale &&
                displaySize == column.displaySize &&
                Objects.equals(name, column.name) &&
                Objects.equals(typeName, column.typeName) &&
                Objects.equals(label, column.label);
    }

    // hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(name, typeName, sqlType, precision, scale, label, displaySize);
    }

    // toString method
    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", typeName='" + typeName + '\'' +
                ", sqlType=" + sqlType +
                ", precision=" + precision +
                ", scale=" + scale +
                ", label='" + label + '\'' +
                ", displaySize=" + displaySize +
                '}';
    }
}
