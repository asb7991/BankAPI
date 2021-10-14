package ru.sberbank.bankapi;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DataBaseConnection {
    private static DataBaseConnection instance;
    private static Connection connection;

    private DataBaseConnection() throws SQLException {
        Properties prop = new Properties();
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/application.properties");
            prop.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = prop.getProperty("url");
        String user = prop.getProperty("user");
        String password = prop.getProperty("password");
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        connection = DriverManager.getConnection(url, user, password);
    }

    public static DataBaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DataBaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void firstInit() throws SQLException {
        Statement statement = connection.createStatement();
        String query = "DROP TABLE IF EXISTS operation;" +
                "DROP TABLE IF EXISTS card;" +
                "DROP TABLE IF EXISTS account;" +
                "DROP TABLE IF EXISTS client;";
        statement.execute(query);

         query = "CREATE TABLE client (id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "                   first_name VARCHAR(20) NOT NULL,\n" +
                "                   last_name VARCHAR(20) not NULL, \n " +
                "                   passport CHAR(10) NOT NULL UNIQUE" +
                ")";
        statement.execute(query);

        query = "CREATE TABLE account (id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "                   number CHAR(20) NOT NULL UNIQUE, \n" +
                "                   balance FLOAT NOT NULL, \n" +
                "                   client_id INT NOT NULL, \n" +
                "                   FOREIGN KEY(client_id) references client(id)" +
                ")";
        statement.execute(query);

        query = "CREATE TABLE card (id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "                   account_id INT NOT NULL, \n" +
                "                   number CHAR(16) NOT NULL UNIQUE , \n" +
                "                   cvv CHAR(3) NOT NULL, \n" +
                "                   month_of_end VARCHAR (2) NOT NULL, \n" +
                "                   year_of_end YEAR NOT NULL, \n" +
                "                   pin VARCHAR(12) NOT NULL, \n" +
                "                   activated BIT NOT NULL, \n" +
                "                   FOREIGN KEY(account_id) references account(id)" +
                ")";
        statement.execute(query);

        query = "CREATE TABLE operation (id INT AUTO_INCREMENT PRIMARY KEY, \n" +
                "                   from_account_number CHAR(20), \n" +
                "                   to_account_number CHAR(20) NOT NULL, \n" +
                "                   sum FLOAT NOT NULL, \n" +
                "                   status VARCHAR(10)" +
                ")";
        statement.execute(query);

        query = "INSERT INTO client (id, first_name, last_name, passport) VALUES (1, 'Иван', 'Петров', '1111222222')";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.execute();

        query = "INSERT INTO account (id, number, balance, client_id) VALUES (1, '12345678901234567890', 0, 1)";
        preparedStatement = connection.prepareStatement(query);
        preparedStatement.execute();

        statement.close();
        preparedStatement.close();
    }
}
