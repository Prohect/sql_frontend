package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

public class CAlterPacket extends AbstractPacket implements Serializable {
    long uuid;
    String cmd;
    String databaseName;

    public CAlterPacket() {
    }

    public CAlterPacket(long uuid, String cmd, String databaseName) {
        this.uuid = uuid;
        this.cmd = cmd;
        this.databaseName = databaseName;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
