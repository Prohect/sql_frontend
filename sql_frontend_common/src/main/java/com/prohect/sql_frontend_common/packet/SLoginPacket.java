package com.prohect.sql_frontend_common.packet;

import com.prohect.sql_frontend_common.ColumnMetaData;
import com.prohect.sql_frontend_common.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class SLoginPacket extends AbstractPacket implements Serializable {
    User user;
    HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap = new HashMap<>();
    Info info;
    String theUsersTableName;
    String theUsersDatabaseName;


    public SLoginPacket() {
    }

    public SLoginPacket(User user, HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap, Info info, String theUsersTableName, String theUsersDatabaseName) {
        this.user = user;
        this.db2table2columnMap = db2table2columnMap;
        this.info = info;
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
    }

    public static String toString(Info info) {
        return switch (info) {
            case RS -> "reconnect success, permissions updated";
            case S -> "success";
            case W -> "wrong password";
            case N -> "no such user";
            case UM -> "metadata updated";
            case UP -> "permissions updated";
        };
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> getDb2table2columnMap() {
        return db2table2columnMap;
    }

    public void setDb2table2columnMap(HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap) {
        this.db2table2columnMap = db2table2columnMap;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public String getTheUsersTableName() {
        return theUsersTableName;
    }

    public void setTheUsersTableName(String theUsersTableName) {
        this.theUsersTableName = theUsersTableName;
    }

    public String getTheUsersDatabaseName() {
        return theUsersDatabaseName;
    }

    public void setTheUsersDatabaseName(String theUsersDatabaseName) {
        this.theUsersDatabaseName = theUsersDatabaseName;
    }

    public enum Info {
        RS,//reconnect success
        S,//login success
        W,//wrong password
        N,//no such one
        UM,//update metadata
        UP,//update permission //TODO:
    }
}
