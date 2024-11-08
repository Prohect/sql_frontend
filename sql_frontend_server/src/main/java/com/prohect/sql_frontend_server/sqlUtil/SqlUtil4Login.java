package com.prohect.sql_frontend_server.sqlUtil;

import com.prohect.sql_frontend_common.Util;
import com.prohect.sql_frontend_common.User;
import com.prohect.sql_frontend_server.DatabaseAdmin;
import com.prohect.sql_frontend_server.ServerConfig;

import java.sql.*;
import java.util.HashMap;

public class SqlUtil4Login {

    public static Connection getConnection4UsersDB(ServerConfig serverConfig) throws SQLException {
        String theUsersDatabaseName = serverConfig.getTheUsersDatabaseName();
        String theUsersTableName = serverConfig.getTheUsersTableName();
        DatabaseAdmin dataBaseAdmin = serverConfig.getDataBaseAdmin();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;database=" + theUsersDatabaseName + ";trustServerCertificate=true", dataBaseAdmin.getUsername(), dataBaseAdmin.getPassword());
    }

    private static String getSelectQueryByUsername(String theUsersTableName, String username) {
        return "SELECT * from " + theUsersTableName + " where [username] = '" + username + "'";
    }

    public static User getUserByUsername(String theUsersTableName, String username, Statement statement) throws SQLException {
        var set = statement.executeQuery(getSelectQueryByUsername(theUsersTableName, username));
        if (set.next()) {
            User user = new User(set.getString("username"), set.getString("passWord"), set.getLong("uuid"));

            //permissions decode
            ResultSetMetaData metaData = set.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                String columnName = metaData.getColumnName(columnIndex);
                if (columnName.startsWith("P_")) {
                    String[] decode = Util.permissionColumnNameDecode(columnName);//0->databaseName; 1->tableName; 2->columnName; 3->ReadOrWrite
                    Boolean[] booleans = user.getPermissions().computeIfAbsent(decode[0], k -> new HashMap<>()).computeIfAbsent(decode[1], k -> new HashMap<>()).computeIfAbsent(decode[2], k -> new Boolean[2]);
                    if (decode[3].equals("Read")) {
                        booleans[0] = set.getBoolean(columnIndex);
                    } else if (decode[3].equals("Write")) {
                        booleans[1] = set.getBoolean(columnIndex);
                    }
                }
            }
            user.setOp(set.getBoolean("op"));

            return user;
        } else
            return null;
    }

}
