package com.diploma.service;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class DatabaseConnectorService {

    public boolean testConnection(String databaseType, String url, String username, String password, String driver) {
        try {
            Class.forName(driver);

            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Подключение к " + databaseType + " успешно!");
                return connection.isValid(2);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC-драйвер не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Ошибка соединения с " + databaseType + ": " + e.getMessage());
        }
        return false;
    }
}
