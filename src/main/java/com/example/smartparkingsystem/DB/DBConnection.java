package com.example.smartparkingsystem.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection connection = null;
        String url = "jdbc:mariadb://localhost:3306/smart_parking_system";
        String user = "system_user";
        String pass = "0220";

        Class.forName("org.mariadb.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, pass);
        return connection;
    }
}
