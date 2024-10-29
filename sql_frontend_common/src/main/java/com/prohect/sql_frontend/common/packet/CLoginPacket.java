package com.prohect.sql_frontend.common.packet;

import com.prohect.sql_frontend.common.User;

public class CLoginPacket extends AbstractPacket {
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
