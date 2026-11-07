package com.russianresale.app.data.model;

/** Способы получения заказа и базовая стоимость доставки в рублях. */
public enum DeliveryMethod {
    PICKUP("Самовывоз у продавца", 0, false),
    CDEK("Пункт выдачи СДЭК", 250, true),
    POST("Почта России", 290, true),
    COURIER("Курьер до двери", 450, true);

    public final String title;
    public final int baseCost;
    public final boolean needsAddress;

    DeliveryMethod(String title, int baseCost, boolean needsAddress) {
        this.title = title;
        this.baseCost = baseCost;
        this.needsAddress = needsAddress;
    }

    public static DeliveryMethod of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return PICKUP;
        }
    }
}
