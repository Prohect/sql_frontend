package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

/**
 * update a certain value for an exist row
 */
public class CUpdatePacket extends AbstractPacket implements Serializable {
    long uuid;
    String updateCMD;
    String databaseName;

    public CUpdatePacket() {
    }

    public CUpdatePacket(long uuid, String updateCMD, String databaseName) {
        this.uuid = uuid;
        this.updateCMD = updateCMD;
        this.databaseName = databaseName;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getUpdateCMD() {
        return updateCMD;
    }

    public void setUpdateCMD(String updateCMD) {
        this.updateCMD = updateCMD;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
