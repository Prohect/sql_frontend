package com.prohect.sql_frontend_server;

public class DatabaseAdmin {
    private String username = "";

    private String password = "";

    public DatabaseAdmin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
