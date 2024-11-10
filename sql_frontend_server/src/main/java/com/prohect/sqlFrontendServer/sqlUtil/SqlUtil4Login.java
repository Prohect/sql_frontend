package com.prohect.sqlFrontendServer.sqlUtil;

import com.prohect.sqlFrontendCommon.User;
import com.prohect.sqlFrontendCommon.Util;
import com.prohect.sqlFrontendServer.DatabaseAdmin;
import com.prohect.sqlFrontendServer.ServerConfig;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class SqlUtil4Login {

    public static Connection getConnection4UsersDB(ServerConfig serverConfig) throws SQLException {
        String theUsersDatabaseName = serverConfig.getTheUsersDatabaseName();
        DatabaseAdmin dataBaseAdmin = serverConfig.getDataBaseAdmin();
        return SqlUtil.getConnection4TargetDB(dataBaseAdmin, theUsersDatabaseName);
    }

    private static String getSelectQueryByUsername(String theUsersTableName, String username) {
        return "SELECT * from " + theUsersTableName + " where [username] = '" + username + "'";
    }

    public static User getUserByUsername(String theUsersTableName, String username, Statement statement) throws SQLException {
        //in the login ui, we have limited the username could only contain letters and digits, no sql injection may happen
        @SuppressWarnings("all")
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
                    Boolean[] booleans = user.getPermissions().computeIfAbsent(decode[0], _ -> new HashMap<>()).computeIfAbsent(decode[1], _ -> new HashMap<>()).computeIfAbsent(decode[2], _ -> new Boolean[2]);
                    if (decode[3].equals("Read"))
                        booleans[0] = set.getBoolean(columnIndex);
                    else if (decode[3].equals("Write"))
                        booleans[1] = set.getBoolean(columnIndex);
                }
            }
            user.setOp(set.getBoolean("op"));

            return user;
        } else
            return null;
    }

}
