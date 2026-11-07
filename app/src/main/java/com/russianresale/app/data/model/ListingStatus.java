package com.russianresale.app.data.model;

public enum ListingStatus {
    ACTIVE("Активно"),
    SOLD("Продано"),
    ARCHIVED("В архиве"),
    BLOCKED("Заблокировано модератором");

    public final String title;

    ListingStatus(String title) {
        this.title = title;
    }

    public static ListingStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return ACTIVE;
        }
    }
}
