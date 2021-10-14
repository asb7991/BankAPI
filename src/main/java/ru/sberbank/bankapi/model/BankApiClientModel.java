package ru.sberbank.bankapi.model;

import ru.sberbank.bankapi.DataBaseConnection;
import ru.sberbank.bankapi.ValidationAlgorithms;
import ru.sberbank.bankapi.entity.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

public class BankApiClientModel {
    public static Map<String, Object> createNewCard(String accountId, String type, String pin) throws SQLException {
        // номер карты состоит из цифры, обозначающей платежную систему, БИН, уникального номера и проверочной цифры
        final String bin = "12345";
        Map<String, Object> response = new HashMap<>();
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT id FROM account WHERE id = " + accountId;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            statement.close();
            response.put("status", "FAIL. ACCOUNT NOT FOUND");
            return response;
        }

        // получаем все карты и пытаемся создать уникальную
        query = "SELECT number FROM card";
        resultSet = statement.executeQuery(query);
        List<Integer> partOfNumbersOfCard = new ArrayList<>();
        while (resultSet.next()) {
            partOfNumbersOfCard.add(Integer.parseInt(resultSet.getString("number").substring(6, 15)));
        }
        int partOfNumberOfNewCard = 1;
        while(partOfNumbersOfCard.contains(partOfNumberOfNewCard)) {
            partOfNumberOfNewCard++;
            if(partOfNumberOfNewCard >= 999999999) {
                statement.close();
                response.put("status", "FAIL. UNABLE TO CREAT A NEW CARD");
                return response;
            }
        }

        String payNumber = "2";
        switch (type) {
            case "mastercard":
                payNumber = "5";
                break;
            case "visa":
                payNumber = "4";
                break;
            case "mir":
                payNumber = "2";
                break;
        }

        //вычисляем проверочную цифру с помощью алгоритма Луна
        StringBuilder number = new StringBuilder(payNumber + bin + String.format("%09d", partOfNumberOfNewCard));
        number.append(ValidationAlgorithms.getControlNumberForCardByLuhnAlgorithm(number.toString()));

        String cvv = String.format("%03d", new Random().nextInt(999));
        String monthOfEnd = String.valueOf(LocalDateTime.now().getMonth().getValue());
        String yearOfEnd = String.valueOf(LocalDateTime.now().getYear() + 2);
        query = "INSERT INTO card (account_id, number, cvv, month_of_end, year_of_end, pin, activated) VALUES (?, ?, ?, ?, ?, ?, 0)";
        PreparedStatement preparedStatement = DataBaseConnection.getInstance().getConnection().prepareStatement(query);
        preparedStatement.setString(1, accountId);
        preparedStatement.setString(2, number.toString());
        preparedStatement.setString(3, cvv);
        preparedStatement.setString(4, monthOfEnd);
        preparedStatement.setString(5, yearOfEnd);
        preparedStatement.setString(6, pin);
        preparedStatement.execute();

        Card newCard = new Card(number.toString(), cvv, monthOfEnd, yearOfEnd, pin);
        statement.close();
        preparedStatement.close();
        response.put("status", "OK");
        response.put("card", newCard);
        return response;
    }

    public static Map<String, Object> getListOfCard(String accountId) throws SQLException {
        Map<String, Object> response = new HashMap<>();
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT * FROM card WHERE activated = 1 AND account_id = " + accountId;
        ResultSet resultSet = statement.executeQuery(query);
        List<Card> cards = new ArrayList<>();
        while (resultSet.next()) {
            Card card = new Card();
            card.setNumber(resultSet.getString("number"));
            card.setMonth(resultSet.getString("month_of_end"));
            card.setYear(resultSet.getString("year_of_end"));
            card.setCvv(resultSet.getString("cvv"));
            card.setPin(resultSet.getString("pin"));
            cards.add(card);
        }
        statement.close();
        response.put("status", "OK");
        response.put("cards", cards);
        return response;
    }

    public static String putBalance(String accountId, String sum) throws SQLException {
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT number FROM account WHERE id = " + accountId;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            return "FAIL. ACCOUNT NOT FOUND";
        }

        query = "INSERT INTO operation(to_account_number, sum, status) VALUES(?, ?, ?)";
        PreparedStatement preparedStatement = DataBaseConnection.getInstance().getConnection().prepareStatement(query);
        preparedStatement.setString(1, resultSet.getString("number"));
        preparedStatement.setString(2, sum);
        preparedStatement.setString(3, "WAITING");
        preparedStatement.execute();
        preparedStatement.close();
        statement.close();
        return "OK";
    }

    public static Map<String, String> checkBalance(String accountId) throws SQLException {
        Map<String, String> response = new HashMap<>();
        Statement statement = DataBaseConnection.getInstance().getConnection().createStatement();
        String query = "SELECT id FROM account WHERE id = " + accountId;
        ResultSet resultSet = statement.executeQuery(query);
        if(!resultSet.next()) {
            response.put("status", "FAIL. ACCOUNT NOT FOUND");
            return response;
        }

        query = "SELECT balance FROM account WHERE id = " + accountId;
        resultSet = statement.executeQuery(query);
        resultSet.next();
        String balance = resultSet.getString("balance");

        statement.close();
        response.put("status", "OK");
        response.put("balance", balance);
        return response;
    }
}
