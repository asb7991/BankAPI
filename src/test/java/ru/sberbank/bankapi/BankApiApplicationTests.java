package ru.sberbank.bankapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.sberbank.bankapi.entity.*;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankApiApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeMethod() throws Exception {
        // очистка базы перед каждым тестом
        DataBaseConnection.getInstance().firstInit();
    }

    @Test
    public void createNewCardAndCheckTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        List<String> listOfPayTypes = new ArrayList<>();
        listOfPayTypes.add("visa");
        listOfPayTypes.add("mastercard");
        listOfPayTypes.add("mir");
        List<Card> cardsToCheck = new ArrayList<>();

        // создаем три вида карт
        for (String currentType : listOfPayTypes) {
            request.put("account_id", 1);
            request.put("type", currentType);
            request.put("pin", "0000");

            MvcResult result = this.mockMvc.perform(post("/client/create-new-card").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andDo(print()).andExpect(status().isOk()).andReturn();
            StringReader reader = new StringReader(result.getResponse().getContentAsString());
            NewCardResponse response = mapper.readValue(reader, NewCardResponse.class);
            cardsToCheck.add(response.getCard());

            Assertions.assertEquals("OK", response.getStatus());
            Assertions.assertEquals(String.valueOf(LocalDateTime.now().getMonth().getValue()), response.getCard().getMonth());
            Assertions.assertEquals(String.valueOf(LocalDateTime.now().getYear() + 2), response.getCard().getYear());
            Assertions.assertEquals("0000", response.getCard().getPin());
            Assertions.assertTrue(ValidationAlgorithms.checkValidityCardByLuhnAlgorithm(response.getCard().getNumber()), "error validation a card number");
            Assertions.assertEquals(3, response.getCard().getCvv().length(), "wrong cvv");
        }

        // проверяем, что карты не появились у клиента до подтверждения банком
        request.clear();
        request.put("account_id", 1);
        GetCardsResponse expectedResponse = new GetCardsResponse("OK", new ArrayList<>());
        this.mockMvc.perform(post("/client/get-list-of-card").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponse)));

        // подтверждаем выпуск карт
        for (Card card : cardsToCheck) {
            request.clear();
            request.put("card_number", card.getNumber());
            StatusResponse expectedStatusResponse = new StatusResponse("OK");
            this.mockMvc.perform(post("/employee/confirm-card").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));
        }

        // проверяем, что карты появились у клиента
        request.clear();
        request.put("account_id", 1);
        expectedResponse = new GetCardsResponse("OK", cardsToCheck);
        this.mockMvc.perform(post("/client/get-list-of-card").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void createNewCardForNonExistentClient() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        request.put("account_id", 2);
        request.put("type", "mir");
        request.put("pin", "0000");
        NewCardResponse expectedResponse = new NewCardResponse("FAIL. ACCOUNT NOT FOUND", null);

        this.mockMvc.perform(post("/client/create-new-card").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void confirmNonExistentOperation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        request.put("operation_id", 1);
        StatusResponse expectedStatusResponse = new StatusResponse("FAIL. OPERATION NOT FOUND");
        this.mockMvc.perform(post("/employee/confirm-operation").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));
    }

    @Test
    public void putAndCheckBalanceTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // проверяем, что у только что созданного клиента нулевой баланс
        Map<String, Object> request = new HashMap<>();
        request.put("account_id", 1);
        CheckBalanceResponse expectedResponseCheckBalance = new CheckBalanceResponse("OK", "0.0");
        this.mockMvc.perform(post("/client/check-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponseCheckBalance)));

        // пополняем баланс клиента
        request.clear();
        request.put("account_id", 1);
        request.put("sum", 458.5);
        StatusResponse expectedStatusResponse = new StatusResponse("OK");
        this.mockMvc.perform(post("/client/put-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));

        // проверяем, что деньги не зачислены до подтверждения банком
        request.clear();
        request.put("account_id", 1);
        expectedResponseCheckBalance = new CheckBalanceResponse("OK", "0.0");
        this.mockMvc.perform(post("/client/check-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponseCheckBalance)));

        // подтверждаем операцию
        request.clear();
        request.put("operation_id", 1);
        expectedStatusResponse = new StatusResponse("OK");
        this.mockMvc.perform(post("/employee/confirm-operation").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));

        // проверяем баланс после подтверждения
        request.clear();
        request.put("account_id", 1);
        expectedResponseCheckBalance = new CheckBalanceResponse("OK", "458.5");
        this.mockMvc.perform(post("/client/check-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponseCheckBalance)));
    }

    @Test
    public void checkAndPutBalanceNonExistentAccountTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        request.put("account_id", 2);
        request.put("sum", 458.5);
        StatusResponse expectedStatusResponse = new StatusResponse("FAIL. ACCOUNT NOT FOUND");
        this.mockMvc.perform(post("/client/put-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));

        request.clear();
        request.put("account_id", 2);
        CheckBalanceResponse expectedResponseCheckBalance = new CheckBalanceResponse("FAIL. ACCOUNT NOT FOUND", null);
        this.mockMvc.perform(post("/client/check-balance").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponseCheckBalance)));

    }

    @Test
    public void addNewClientAndAddExistentClientTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        request.put("first_name", "Андрей");
        request.put("last_name", "Свиридов");
        request.put("passport", "9999999999");

        // добавляем нового клиента
        StatusResponse expectedStatusResponse = new StatusResponse("OK");
        this.mockMvc.perform(post("/employee/add-new-client").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));

        // пытаемся еще раз добавить этого же клиента еще раз
        expectedStatusResponse = new StatusResponse("FAIL. CLIENT ALREADY EXIST");
        this.mockMvc.perform(post("/employee/add-new-client").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedStatusResponse)));
    }

    @Test
    public void addNewAccountAndAddNewAccountNonExistentClient() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> request = new HashMap<>();
        request.put("client_id", 1);

        // открываем новый счет
        MvcResult result = this.mockMvc.perform(post("/employee/add-new-account").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        StringReader reader = new StringReader(result.getResponse().getContentAsString());
        AddNewAccountResponse response = mapper.readValue(reader, AddNewAccountResponse.class);
        Assertions.assertEquals("OK", response.getStatus());
        Assertions.assertTrue(ValidationAlgorithms.checkValidityAccountNumber("713" + response.getAccountNumber()),
                "wrong account number");

        // пытаемся открыть новый счет у несуществующего клиента
        request.clear();
        request.put("client_id", 2);
        AddNewAccountResponse expectedResponse = new AddNewAccountResponse("FAIL. CLIENT NOT FOUND", null);
        this.mockMvc.perform(post("/employee/add-new-account").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print()).andExpect(status().isOk()).andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }
}
