package com.russianresale.app.data.model;

public enum SortOrder {
    NEWEST("Сначала новые", "l.createdAt DESC"),
    CHEAPEST("Сначала дешёвые", "l.price ASC"),
    EXPENSIVE("Сначала дорогие", "l.price DESC"),
    POPULAR("Популярные", "l.views DESC");

    public final String title;
    public final String sql;

    SortOrder(String title, String sql) {
        this.title = title;
        this.sql = sql;
    }
}
