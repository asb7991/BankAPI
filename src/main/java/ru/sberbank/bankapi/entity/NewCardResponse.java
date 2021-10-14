package ru.sberbank.bankapi.entity;

public class NewCardResponse {
    private String status;
    private Card card;

    public NewCardResponse() {}
    public NewCardResponse(String status, Card card) {
        this.status = status;
        this.card = card;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
