package com.russianresale.app.data.model;

public enum ComplaintStatus {
    OPEN("На рассмотрении"),
    RESOLVED("Объявление заблокировано"),
    REJECTED("Отклонена");

    public final String title;

    ComplaintStatus(String title) {
        this.title = title;
    }

    public static ComplaintStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return OPEN;
        }
    }
}
