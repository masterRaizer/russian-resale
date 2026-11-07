package com.russianresale.app.data.model;

/** Роль пользователя. Роль выбирается при регистрации и потом не меняется. */
public enum Role {
    BUYER("Покупатель"),
    SELLER("Продавец"),
    ADMIN("Администратор");

    public final String title;

    Role(String title) {
        this.title = title;
    }

    public static Role of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return BUYER;
        }
    }
}
