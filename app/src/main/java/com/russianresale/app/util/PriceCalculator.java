package com.russianresale.app.util;

import com.russianresale.app.data.model.DeliveryMethod;

/** Расчёт стоимости заказа. */
public final class PriceCalculator {
    /** Порог бесплатной доставки (кроме курьера). */
    public static final int FREE_DELIVERY_FROM = 7000;
    /** Комиссия площадки с продавца, % (для статистики продавца). */
    public static final int COMMISSION_PERCENT = 5;

    private PriceCalculator() {
    }

    public static int deliveryCost(DeliveryMethod method, int itemsTotal) {
        if (method == null || method == DeliveryMethod.PICKUP) return 0;
        if (method != DeliveryMethod.COURIER && itemsTotal >= FREE_DELIVERY_FROM) return 0;
        return method.baseCost;
    }

    public static int total(int itemsTotal, DeliveryMethod method) {
        return itemsTotal + deliveryCost(method, itemsTotal);
    }

    public static long commission(long revenue) {
        return Math.round(revenue * COMMISSION_PERCENT / 100.0);
    }
}
