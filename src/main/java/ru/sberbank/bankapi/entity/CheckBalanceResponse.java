package ru.sberbank.bankapi.entity;

public class CheckBalanceResponse {
    private String status;
    private String balance;

    public CheckBalanceResponse() {}

    public CheckBalanceResponse(String status, String balance) {
        this.status = status;
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
