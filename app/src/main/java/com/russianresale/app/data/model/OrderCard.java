package com.russianresale.app.data.model;

import androidx.room.Embedded;

import com.russianresale.app.data.entity.Order;

/** Заказ для списка: + имена сторон, первая позиция и количество вещей. */
public class OrderCard {
    @Embedded
    public Order order;
    public String buyerName;
    public String sellerName;
    public String firstTitle;
    public String photo;
    public int itemsCount;
}
