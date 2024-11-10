package com.prohect.sqlFrontendCommon.packet;

import com.prohect.sqlFrontendCommon.User;

import java.io.Serializable;


@SuppressWarnings("unused")
public class CLoginPacket extends AbstractPacket implements Serializable {
    User user;

    public CLoginPacket() {
    }

    public CLoginPacket(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
