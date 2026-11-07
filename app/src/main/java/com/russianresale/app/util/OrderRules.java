package com.russianresale.app.util;

import com.russianresale.app.data.model.OrderStatus;

/**
 * Кто и когда может менять статус заказа.
 * <ul>
 *   <li>Продавец: NEW → ACCEPTED, ACCEPTED → SHIPPED, отмена в NEW и ACCEPTED.</li>
 *   <li>Покупатель: отмена в NEW, подтверждение получения в SHIPPED (→ DELIVERED).</li>
 * </ul>
 */
public final class OrderRules {
    private OrderRules() {
    }

    public static boolean sellerCanAccept(OrderStatus status) {
        return status == OrderStatus.NEW;
    }

    public static boolean sellerCanShip(OrderStatus status) {
        return status == OrderStatus.ACCEPTED;
    }

    public static boolean sellerCanCancel(OrderStatus status) {
        return status == OrderStatus.NEW || status == OrderStatus.ACCEPTED;
    }

    public static boolean buyerCanCancel(OrderStatus status) {
        return status == OrderStatus.NEW;
    }

    public static boolean buyerCanConfirm(OrderStatus status) {
        return status == OrderStatus.SHIPPED;
    }

    public static boolean buyerCanReview(OrderStatus status) {
        return status == OrderStatus.DELIVERED;
    }

    /** Шаг заказа для индикатора прогресса: 0..3, -1 — отменён. */
    public static int step(OrderStatus status) {
        switch (status) {
            case NEW:
                return 0;
            case ACCEPTED:
                return 1;
            case SHIPPED:
                return 2;
            case DELIVERED:
                return 3;
            default:
                return -1;
        }
    }
}
