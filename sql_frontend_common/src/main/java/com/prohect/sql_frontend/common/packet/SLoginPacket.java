package com.prohect.sql_frontend.common.packet;

import com.prohect.sql_frontend.common.ColumnMetaData;
import com.prohect.sql_frontend.common.User;

import java.util.ArrayList;
import java.util.HashMap;

public class SLoginPacket extends AbstractPacket {
    User user;
    HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap = new HashMap<>();
    String info;
    String theUsersTableName;
    String theUsersDatabaseName;

    public SLoginPacket() {
    }

    public SLoginPacket(User user, HashMap<String, HashMap<String, ArrayList<ColumnMetaData>>> db2table2columnMap, String info, String theUsersTableName, String theUsersDatabaseName) {
        this.user = user;
        this.db2table2columnMap = db2table2columnMap;
        this.info = info;
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
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
}
