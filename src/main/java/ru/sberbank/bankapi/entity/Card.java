package ru.sberbank.bankapi.entity;

import java.util.Objects;

public class Card {
    private String number;
    private String cvv;
    private String month;
    private String year;
    private String pin;

    public Card() {}
    public Card(String number, String cvv, String month, String year, String pin) {
        this.number = number;
        this.cvv = cvv;
        this.month = month;
        this.year = year;
        this.pin = pin;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(number, card.number) && Objects.equals(cvv, card.cvv) && Objects.equals(month, card.month) && Objects.equals(year, card.year) && Objects.equals(pin, card.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, cvv, month, year, pin);
    }
}
