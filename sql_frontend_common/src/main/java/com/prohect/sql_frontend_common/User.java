package com.prohect.sql_frontend_common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
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
    private int ip;

    public User() {//as for jsonb unpack or map.getOrDefault, dont initIP
    }

    public User(String username, String password, Long uuid) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = new HashMap<>();
        initIP();
    }

    public User(String username, String password, Long uuid, HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = permissions;
        initIP();
    }

    public User(String username, String password, Long uuid, HashMap<String, HashMap<String, HashMap<String, Boolean[]>>> permissions, boolean op) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.permissions = permissions;
        this.op = op;
        initIP();
    }

    private static int ipToInteger(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid IP address format");

        int ipAsInt = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(parts[i]);
            if (part < 0 || part > 255) throw new IllegalArgumentException("Invalid IP address format");
            ipAsInt |= part << (24 - (i * 8));
        }
        return ipAsInt;
    }

    private static String integerToIp(int ipAsInt) {
        return "%d.%d.%d.%d".formatted((ipAsInt >> 24) & 0xFF, (ipAsInt >> 16) & 0xFF, (ipAsInt >> 8) & 0xFF, ipAsInt & 0xFF);
    }

    public int getIp() {
        return ip;
    }

    public User setIp(int ip) {
        this.ip = ip;
        return this;
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

    private void initIP() {
        try {
            // 使用一个免费的IP查询API
            URI uri = new URI("http://checkip.amazonaws.com/");
            URL url = uri.toURL();

            // 创建一个不使用代理的连接
            Proxy proxy = Proxy.NO_PROXY;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder ipAddress = new StringBuilder();

            while (in.ready()) {
                ipAddress.append(in.readLine());
            }

            in.close();
            connection.disconnect();

            // 将字符串形式的IP地址转换为整数
            this.ip = ipToInteger(ipAddress.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIpAddress() {
        return integerToIp(ip);
    }
}
