package ru.sberbank.bankapi.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.bankapi.entity.*;
import ru.sberbank.bankapi.model.BankApiClientModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client")
public class BankApiClientController {
    @PostMapping(value = "/create-new-card", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NewCardResponse createNewCard(@RequestBody Map<String, String> map) {
            Map<String, Object> answer;
        try {
        answer = BankApiClientModel.createNewCard(map.get("account_id"), map.get("type"), map.get("pin"));
        } catch (SQLException e) {
            e.printStackTrace();
            return new NewCardResponse("FAIL. DB ERROR", null);
        }
        return new NewCardResponse((String) answer.get("status"), (Card) answer.get("card"));
    }

    @PostMapping(value = "/get-list-of-card", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public GetCardsResponse getListOfCard(@RequestBody Map<String, String> map) {
        Map<String, Object> answer;
        try {
            answer = BankApiClientModel.getListOfCard(map.get("account_id"));
        } catch (SQLException e) {
            e.printStackTrace();
            return new GetCardsResponse("FAIL. DB ERROR", null);
        }
        return new GetCardsResponse((String) answer.get("status"), (List<Card>) answer.get("cards"));
    }

    @PostMapping(value = "/put-balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatusResponse putBalance(@RequestBody Map<String, String> map) {
        try {
            return new StatusResponse(BankApiClientModel.putBalance(map.get("account_id"), map.get("sum")));
        } catch (SQLException e) {
            e.printStackTrace();
            return new StatusResponse("FAIL. DB ERROR");
        }
    }

    @PostMapping(value = "/check-balance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CheckBalanceResponse checkBalance(@RequestBody Map<String, String> map) {
        Map<String, String> answer;
        try {
            answer = BankApiClientModel.checkBalance(map.get("account_id"));
        } catch (SQLException e) {
            e.printStackTrace();
            return new CheckBalanceResponse("FAIL. DB ERROR", null);
        }
        return new CheckBalanceResponse(answer.get("status"), answer.get("balance"));
    }
}
