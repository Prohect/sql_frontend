package com.prohect.sql_frontend.common;

public interface Packet {
    default String getPrefix() {
        return this.getClass().getSimpleName() + "\\";
    }
}
