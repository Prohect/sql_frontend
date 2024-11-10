package com.prohect.sqlFrontendServer;

import java.util.List;

@SuppressWarnings("unused")
public class ServerConfig {
    private String theUsersTableName;
    private String theUsersDatabaseName;
    private List<String> theTargetDatabaseNameList;
    private List<String> theTargetDatabaseNameBlackList;
    private DatabaseAdmin dataBaseAdmin;
    private int serverPort;

    public ServerConfig(String theUsersTableName, String theUsersDatabaseName, List<String> theTargetDatabaseNameList, List<String> theTargetDatabaseNameBlackList, DatabaseAdmin dataBaseAdmin, int serverPort) {
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
        this.theTargetDatabaseNameList = theTargetDatabaseNameList;
        this.theTargetDatabaseNameBlackList = theTargetDatabaseNameBlackList;
        this.dataBaseAdmin = dataBaseAdmin;
        this.serverPort = serverPort;
    }

    public String getTheUsersTableName() {
        return theUsersTableName.toLowerCase();
    }

    public void setTheUsersTableName(String theUsersTableName) {
        this.theUsersTableName = theUsersTableName;
    }

    public String getTheUsersDatabaseName() {
        return theUsersDatabaseName.toLowerCase();
    }

    public void setTheUsersDatabaseName(String theUsersDatabaseName) {
        this.theUsersDatabaseName = theUsersDatabaseName;
    }

    public List<String> getTheTargetDatabaseNameList() {
        return theTargetDatabaseNameList;
    }

    public void setTheTargetDatabaseNameList(List<String> theTargetDatabaseNameList) {
        this.theTargetDatabaseNameList = theTargetDatabaseNameList;
    }

    public List<String> getTheTargetDatabaseNameBlackList() {
        return theTargetDatabaseNameBlackList;
    }

    public void setTheTargetDatabaseNameBlackList(List<String> theTargetDatabaseNameBlackList) {
        this.theTargetDatabaseNameBlackList = theTargetDatabaseNameBlackList;
    }

    public DatabaseAdmin getDataBaseAdmin() {
        return dataBaseAdmin;
    }

    public void setDataBaseAdmin(DatabaseAdmin dataBaseAdmin) {
        this.dataBaseAdmin = dataBaseAdmin;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
