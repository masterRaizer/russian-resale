package com.russianresale.app.data.model;

public enum Gender {
    WOMEN("Женское"),
    MEN("Мужское"),
    UNISEX("Унисекс"),
    KIDS("Детское");

    public final String title;

    Gender(String title) {
        this.title = title;
    }

    public static Gender of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNISEX;
        }
    }

    public static String[] titles() {
        Gender[] values = values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i].title;
        return result;
    }
}
