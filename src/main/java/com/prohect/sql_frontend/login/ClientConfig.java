package com.prohect.mysql_frontend.login;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ClientConfig {
    private String serverIP = "localhost";
    private int port = 19336;
    private String theUsersTableName;
    private String theUsersDatabaseName;

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public ClientConfig(String serverIP, int port, String theUsersTableName, String theUsersDatabaseName) {
        this.serverIP = serverIP;
        this.port = port;
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
    }

    public static ClientConfig readConfig() throws IOException {
        File configFile = new File("clientConfig.json");
        if (configFile.createNewFile()) {
            return resetConfig(configFile);
        } else {
            try {
                return JSON.parseObject(Files.readAllBytes(configFile.toPath()), ClientConfig.class);
            } catch (JSONException e) {
                return resetConfig(configFile);
            }
        }
    }

    private static ClientConfig resetConfig(File configFile) throws IOException {
        ClientConfig clientConfig = new ClientConfig("127.0.0.1", 19336, "users", "users");
        configFile.delete();
        configFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(configFile);
        fos.write(JSON.toJSONBytes(clientConfig));
        fos.close();
        return clientConfig;
    }

    public static void saveConfig(ClientConfig clientConfig) {
        try {
            File configFile = new File("loginConfig.json");
            configFile.delete();
            configFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(JSON.toJSONBytes(clientConfig));
        } catch (IOException ignored) {
        }
    }
}
