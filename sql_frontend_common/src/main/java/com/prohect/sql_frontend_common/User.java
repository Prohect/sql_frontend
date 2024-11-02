package com.prohect.sql_frontend_common;

import java.util.HashMap;

public class User {
    private String username;
    private String password;
    private long uuid;
    /**
     * the first k -> databaseName,
     * the second K -> the tableName,
     * the third k -> the columnName,
     * the value v[0] -> permission4Read,
     * the value v[1] -> permission4Write,
     */
    private HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions;
    private boolean op;

    public User(String username, String password, Long uuid) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = new HashMap<>();
    }

    public User(String username, String password, Long uuid, HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = permissions;
    }

    public User(String username, String password, Long uuid, HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions, boolean op) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = permissions;
        this.op = op;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> getPermissions() {
        return permissions;
    }

    public void setPermissions(HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions) {
        this.permissions = permissions;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof User u)) return false;
        return u.getUuid() == this.getUuid();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.getUuid());
    }
}
