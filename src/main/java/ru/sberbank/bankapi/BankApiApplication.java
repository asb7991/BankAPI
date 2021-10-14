package ru.sberbank.bankapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.sberbank.bankapi.model.BankApiEmployeeModel;

import java.sql.SQLException;

@SpringBootApplication
public class BankApiApplication {

    public static void main(String[] args) throws SQLException {
        SpringApplication.run(BankApiApplication.class, args);
        DataBaseConnection.getInstance().firstInit();
    }

}
