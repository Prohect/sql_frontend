package com.prohect.sql_frontend_server.sqlUtil;

import com.prohect.sql_frontend_server.DatabaseAdmin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlUtil {
    public static Connection getConnection4TargetDB(DatabaseAdmin dataBaseAdmin, String theTargetDatabaseName) throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;database=" + theTargetDatabaseName + ";trustServerCertificate=true", dataBaseAdmin.getUsername(), dataBaseAdmin.getPassword());
    }


}
