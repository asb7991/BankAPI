package ru.sberbank.bankapi.entity;

import java.util.ArrayList;
import java.util.List;

public class GetCardsResponse {
    private String status;
    private List<Card> cards = new ArrayList<>();

    public GetCardsResponse() {}
    public GetCardsResponse(String status, List<Card> cards) {
        this.status = status;
        this.cards = cards;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
