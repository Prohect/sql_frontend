package com.prohect.sql_frontend_common.packet;

import com.prohect.sql_frontend_common.User;

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
