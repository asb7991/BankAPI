package ru.sberbank.bankapi.model;

import ru.sberbank.bankapi.DataBaseConnection;
import ru.sberbank.bankapi.ValidationAlgorithms;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankApiEmployeeModel {
    public static String addNewClient(String firstName, String lastName, String passport) throws SQLException {
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT id FROM client WHERE passport = " + passport;
        ResultSet resultSet = statement.executeQuery(query);
        if(resultSet.next()) {
            statement.close();
            return "FAIL. CLIENT ALREADY EXIST";
        }

        query = "INSERT INTO client(first_name, last_name, passport) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = DataBaseConnection.getInstance().getConnection().prepareStatement(query);
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        preparedStatement.setString(3, passport);
        preparedStatement.execute();
        preparedStatement.close();
        statement.close();
        return "OK";
    }

    public static Map<String, String> addNewAccount(String clientId) throws SQLException {
        final String bankNumber = "40817810K9991";
        Map<String, String> response = new HashMap<>();
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT id FROM client WHERE id = " + clientId;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            statement.close();
            response.put("status", "FAIL. CLIENT NOT FOUND");
            return response;
        }

        query = "SELECT number FROM account";
        resultSet = statement.executeQuery(query);
        List<Integer> partOfNumbersOfAccount = new ArrayList<>();
        while (resultSet.next()) {
            partOfNumbersOfAccount.add(Integer.parseInt(resultSet.getString("number").substring(13, 20)));
        }
        int partOfNumberOfNewAccount = 1;
        while(partOfNumbersOfAccount.contains(partOfNumberOfNewAccount)) {
            partOfNumberOfNewAccount++;
            if(partOfNumberOfNewAccount >= 9999999) {
                statement.close();
                response.put("status", "FAIL. UNABLE TO CREAT A NEW ACCOUNT");
                return response;
            }
        }
        int controlNumber = ValidationAlgorithms.getControlNumberForAccountNumber("045" + bankNumber
                + String.format("%07d",partOfNumberOfNewAccount));
        String newNumber = bankNumber.replace("K", String.valueOf(controlNumber)) + String.format("%07d",partOfNumberOfNewAccount);

        query = "INSERT INTO account(number, balance, client_id) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = DataBaseConnection.getInstance().getConnection().prepareStatement(query);
        preparedStatement.setString(1, newNumber);
        preparedStatement.setString(2, "0.0");
        preparedStatement.setString(3, clientId);
        preparedStatement.execute();
        preparedStatement.close();
        statement.close();

        response.put("status", "OK");
        response.put("number", newNumber);
        return response;
    }

    public static String confirmNewCard(String cardNumber) throws SQLException {
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT activated FROM card WHERE number = " + cardNumber;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            return "FAIL. CARD NOT FOUND";
        } else {
            if (resultSet.getString("activated").equals("1")) {
                return "FAIL. CARD ALREADY ACTIVATED";
            }
        }

        query = "UPDATE card SET activated = 1 WHERE number = " + cardNumber;
        statement.execute(query);
        return "OK";
    }

    public static String confirmOperation(String operationId) throws SQLException {
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT * FROM operation WHERE id = " + operationId;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            return "FAIL. OPERATION NOT FOUND";
        } else {
            if(!resultSet.getString("status").equals("WAITING")) {
                return "FAIL. OPERATION ALREADY CONFIRMED";
            }
        }

        String sum = resultSet.getString("sum");
        String fromAccountNumber = resultSet.getString("from_account_number");
        String toAccountNumber = resultSet.getString("to_account_number");

        Savepoint savepoint = DataBaseConnection.getInstance().getConnection().setSavepoint("savepoint");
        try {
            if (fromAccountNumber != null) {
                query = "UPDATE account SET balance = balance - " + sum + " WHERE number = " + fromAccountNumber;
                statement.execute(query);
            }

            query = "SELECT number FROM account WHERE number = " + toAccountNumber;
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                query = "UPDATE operation SET status = 'DENIED' WHERE id = " + operationId;
                statement.execute(query);
            } else {
                //вызов метода для перевода на счет другого банка
                return "OK";
            }

        query = "UPDATE account SET balance = balance + " + sum + " WHERE number = " + toAccountNumber;
        statement.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
            DataBaseConnection.getInstance().getConnection().rollback(savepoint);
            return "FAIL. DB ERROR";
        }
        DataBaseConnection.getInstance().getConnection().releaseSavepoint(savepoint);
        statement.close();
        return "OK";
    }
}
