package com.russianresale.app.data.model;

public enum PaymentMethod {
    CARD("Банковской картой онлайн"),
    SBP("СБП"),
    CASH("При получении");

    public final String title;

    PaymentMethod(String title) {
        this.title = title;
    }

    public static PaymentMethod of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return CASH;
        }
    }
}
