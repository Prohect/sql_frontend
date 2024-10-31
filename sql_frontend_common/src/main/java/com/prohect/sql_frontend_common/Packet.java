package com.prohect.sql_frontend_common;

public interface Packet {
    default String getPrefix() {
        return "%s".formatted(this.getClass().getSimpleName());
    }
}
