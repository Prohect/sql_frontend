package com.prohect.sql_frontend_common.packet;

import java.io.Serializable;

public class CInsertPacket extends AbstractPacket implements Serializable {
    long uuid;
    String cmd;
    String dataBaseName;

    public CInsertPacket() {
    }

    public CInsertPacket(long uuid, String cmd, String dataBaseName) {
        this.uuid = uuid;
        this.cmd = cmd;
        this.dataBaseName = dataBaseName;
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

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }
}
