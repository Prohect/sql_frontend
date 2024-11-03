package com.prohect.sql_frontend.main.login;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ClientConfig {
    static final File configFile = new File("clientConfig.json");
    private String serverHost = "localhost";
    private int port = 19336;
    private String theUsersTableName;
    private String theUsersDatabaseName;

    public ClientConfig(String serverHost, int port, String theUsersTableName, String theUsersDatabaseName) {
        this.serverHost = serverHost;
        this.port = port;
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
    }

    public static ClientConfig readConfig() throws IOException {
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
            configFile.delete();
            configFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(JSON.toJSONBytes(clientConfig));
        } catch (IOException ignored) {
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
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
}
