package ru.sberbank.bankapi.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.bankapi.entity.AddNewAccountResponse;
import ru.sberbank.bankapi.entity.StatusResponse;
import ru.sberbank.bankapi.model.BankApiEmployeeModel;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/employee")
public class BankApiEmployeeController {
    @PostMapping(value = "/add-new-client", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse addNewClient(@RequestBody Map<String, String> map) {
        String firstName = map.get("first_name");
        String lastName = map.get("last_name");
        String passport = map.get("passport");
        try {
           return new StatusResponse(BankApiEmployeeModel.addNewClient(firstName, lastName, passport));
        } catch (SQLException e) {
            e.printStackTrace();
            return new StatusResponse("FAIL. DB ERROR");
        }
    }

    @PostMapping(value = "/add-new-account", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AddNewAccountResponse addNewAccount(@RequestBody Map<String, String> map) {
        String clientId = map.get("client_id");
        Map<String, String> answer;
        try {
            answer = BankApiEmployeeModel.addNewAccount(clientId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new AddNewAccountResponse("FAIL. DB ERROR", null);
        }
        return new AddNewAccountResponse(answer.get("status"), answer.get("number"));
    }

    @PostMapping(value = "/confirm-card", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse confirmNewCard(@RequestBody Map<String, String> map) {
        String cardNumber = map.get("card_number");
        try {
            return new StatusResponse(BankApiEmployeeModel.confirmNewCard(cardNumber));
        } catch (SQLException e) {
            e.printStackTrace();
            return new StatusResponse("FAIL. DB ERROR");
        }
    }

    @PostMapping(value = "/confirm-operation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse confirmOperation(@RequestBody Map<String, String> map) {
        String operationId = map.get("operation_id");
        try {
            return new StatusResponse(BankApiEmployeeModel.confirmOperation(operationId));
        } catch (SQLException e) {
            e.printStackTrace();
            return new StatusResponse("FAIL. DB ERROR");
        }
    }
}
