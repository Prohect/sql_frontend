package com.prohect.sql_frontend.common.packet;

public class CQueryPacket extends AbstractPacket {
    long uuid;
    String query;
    String databaseName;

    public CQueryPacket() {
    }

    public CQueryPacket(long uuid, String query) {
        this.uuid = uuid;
        this.query = query;
    }

    public CQueryPacket(long uuid, String query, String databaseName) {
        this.uuid = uuid;
        this.query = query;
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
