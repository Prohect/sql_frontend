package com.prohect.sqlFrontendCommon.packet;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class SQueryReplyPacket extends AbstractPacket implements Serializable {

    private String databaseName;
    private String tableName;
    private String primaryKeyName;
    private ArrayList<String> columnNames;
    private ArrayList<Object[]> rows = new ArrayList<>();

    public SQueryReplyPacket() {
    }

    public SQueryReplyPacket(String databaseName, String tableName, ArrayList<String> columnNames, ArrayList<Object[]> rows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public SQueryReplyPacket(String databaseName, String tableName, String primaryKeyName, ArrayList<String> columnNames, ArrayList<Object[]> rows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public SQueryReplyPacket optPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
        return this;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public ArrayList<Object[]> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Object[]> rows) {
        this.rows = rows;
    }
}
