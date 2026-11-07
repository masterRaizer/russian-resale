package com.russianresale.app.data.model;

/** Состояние вещи. */
public enum Condition {
    NEW_WITH_TAG("Новое с биркой"),
    NEW("Новое без бирки"),
    EXCELLENT("Отличное"),
    GOOD("Хорошее"),
    FAIR("Есть следы носки");

    public final String title;

    Condition(String title) {
        this.title = title;
    }

    public static Condition of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return GOOD;
        }
    }

    public static String[] titles() {
        Condition[] values = values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i].title;
        return result;
    }
}
