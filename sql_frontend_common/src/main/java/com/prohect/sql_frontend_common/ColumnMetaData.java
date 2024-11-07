package com.prohect.sql_frontend_common;

import java.io.Serializable;

public class ColumnMetaData implements Serializable, Comparable<ColumnMetaData> {
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
    long basicValue4ID;
    /**
     * the delta the value of a column with isAutoIncrement increase every new row
     */
    long deltaValue4ID;
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

    public ColumnMetaData(String columnName, String columnType, boolean isPrimaryKey, boolean isAutoIncrement, boolean isNullable, String defaultValue, long basicValue4ID, long deltaValue4ID, boolean unique, boolean hasDefaultValue) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.isPrimaryKey = isPrimaryKey;
        this.isAutoIncrement = isAutoIncrement;
        this.isNullable = isNullable;
        this.defaultValue = defaultValue;
        this.basicValue4ID = basicValue4ID;
        this.deltaValue4ID = deltaValue4ID;
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

    public long getBasicValue4ID() {
        return basicValue4ID;
    }

    public void setBasicValue4ID(long basicValue4ID) {
        this.basicValue4ID = basicValue4ID;
    }

    public long getDeltaValue4ID() {
        return deltaValue4ID;
    }

    public void setDeltaValue4ID(long deltaValue4ID) {
        this.deltaValue4ID = deltaValue4ID;
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
        if (obj instanceof ColumnMetaData columnMetaData1) {
            return columnMetaData1.getColumnName().equals(this.columnName) && columnMetaData1.getColumnType().equals(this.columnType) &&
                    this.isPrimaryKey() == columnMetaData1.isPrimaryKey() && this.isAutoIncrement() == columnMetaData1.isAutoIncrement() &&
                    this.isNullable() == columnMetaData1.isNullable() && this.getDefaultValue().equals(columnMetaData1.getDefaultValue()) &&
                    this.getBasicValue4ID() == columnMetaData1.getBasicValue4ID() && this.getDeltaValue4ID() == columnMetaData1.getDeltaValue4ID() &&
                    this.isUnique() == columnMetaData1.isUnique() && this.isHasDefaultValue() == columnMetaData1.isHasDefaultValue();
        } else return false;
    }

    public boolean isHasDefaultValue() {
        return hasDefaultValue;
    }

    public void setHasDefaultValue(boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
    }

    @Override
    public int compareTo(ColumnMetaData o) {
        return this.getColumnName().compareTo(o.getColumnName());
    }

    @Override
    public int hashCode() {
        return this.getColumnName().hashCode();
    }
}
