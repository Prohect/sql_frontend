package com.prohect.sql_frontend.main.login;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

@SuppressWarnings("unused")
public class ClientConfig {
    static final File configFile = new File("clientConfig.json");

    private String serverHost = "localhost";
    private int port = 19336;
    private String theUsersTableName;
    private String theUsersDatabaseName;
    private double[] sizeOfMainGUI = new double[]{800, 600};
    private String lastDB;
    private String lastTB;
    private HashSet<String> usernames;

    public ClientConfig(String serverHost, int port, String theUsersTableName, String theUsersDatabaseName) {
        this.serverHost = serverHost;
        this.port = port;
        this.theUsersTableName = theUsersTableName;
        this.theUsersDatabaseName = theUsersDatabaseName;
        usernames = new HashSet<>();
    }

    public ClientConfig() {
        usernames = new HashSet<>();
    }

    public static ClientConfig readConfig() throws IOException {
        if (configFile.createNewFile()) {
            return resetConfig();
        } else {
            try {
                ClientConfig clientConfig = JSON.parseObject(Files.readAllBytes(configFile.toPath()), ClientConfig.class);
                return clientConfig == null ? resetConfig() : clientConfig;
            } catch (JSONException e) {
                return resetConfig();
            }
        }
    }

    @SuppressWarnings("all")
    private static ClientConfig resetConfig() throws IOException {
        ClientConfig clientConfig = new ClientConfig("127.0.0.1", 19336, "users", "users");
        ClientConfig.configFile.delete();
        ClientConfig.configFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(ClientConfig.configFile);
        fos.write(JSON.toJSONBytes(clientConfig));
        fos.close();
        return clientConfig;
    }

    @SuppressWarnings("all")
    public static void saveConfig(ClientConfig clientConfig) {
        if (clientConfig == null) return;
        try {
            configFile.delete();
            configFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(JSON.toJSONBytes(clientConfig, JSONWriter.Feature.PrettyFormat));
            fos.flush();
            fos.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * usernames saved locally, which is used to autofill in login UI
     */
    public HashSet<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(HashSet<String> usernames) {
        this.usernames = usernames;
    }

    public String getLastTB() {
        return lastTB;
    }

    public void setLastTB(String lastTB) {
        this.lastTB = lastTB;
    }

    public String getLastDB() {
        return lastDB;
    }

    public void setLastDB(String lastDB) {
        this.lastDB = lastDB;
    }

    public double[] getSizeOfMainGUI() {
        return sizeOfMainGUI;
    }

    public void setSizeOfMainGUI(double[] sizeOfMainGUI) {
        this.sizeOfMainGUI = sizeOfMainGUI;
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
