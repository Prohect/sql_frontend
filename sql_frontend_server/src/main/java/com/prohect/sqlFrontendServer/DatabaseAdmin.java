package com.prohect.sqlFrontendServer;

public class DatabaseAdmin {
    private final String username;

    private final String password;

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
