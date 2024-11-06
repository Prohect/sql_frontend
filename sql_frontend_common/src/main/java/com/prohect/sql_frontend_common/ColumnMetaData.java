package com.prohect.sql_frontend_common;

public class ColumnMetaData {
    String columnName;
    String columnType;
    boolean isPrimaryKey;
    boolean isAutoIncrement;
    boolean isNullable;

    boolean hasDefaultValue;
    String defaultValue;
    /**
     * the every first value of a column with isAutoIncrement
     */
    long basicValue;
    /**
     * the delta the value of a column with isAutoIncrement increase every new row
     */
    long deltaValue;
    boolean unique;

    public ColumnMetaData() {
    }

    public ColumnMetaData(String columnName, String columnType, boolean isPrimaryKey, boolean isAutoIncrement, boolean isNullable) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isPrimaryKey = isPrimaryKey;
        this.isAutoIncrement = isAutoIncrement;
        this.isNullable = isNullable;
    }

    public ColumnMetaData(String columnName, String columnType, boolean isPrimaryKey, boolean isAutoIncrement, boolean isNullable, String defaultValue, long basicValue, long deltaValue, boolean unique, boolean hasDefaultValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isPrimaryKey = isPrimaryKey;
        this.isAutoIncrement = isAutoIncrement;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.basicValue = basicValue;
        this.deltaValue = deltaValue;
        this.unique = unique;
        this.hasDefaultValue = hasDefaultValue;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public long getBasicValue() {
        return basicValue;
    }

    public void setBasicValue(long basicValue) {
        this.basicValue = basicValue;
    }

    public long getDeltaValue() {
        return deltaValue;
    }

    public void setDeltaValue(long deltaValue) {
        this.deltaValue = deltaValue;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        return obj instanceof ColumnMetaData columnMetaData1 && columnMetaData1.getColumnName().equals(this.columnName);
    }

    public boolean isHasDefaultValue() {
        return hasDefaultValue;
    }

    public void setHasDefaultValue(boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
    }
}
