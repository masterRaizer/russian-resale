package com.russianresale.app.data.model;

/**
 * Статусы заказа.
 * NEW → ACCEPTED → SHIPPED → DELIVERED, отмена возможна до отправки.
 * Правила переходов — в {@link com.russianresale.app.util.OrderRules}.
 */
public enum OrderStatus {
    NEW("Ждёт подтверждения"),
    ACCEPTED("Принят продавцом"),
    SHIPPED("Отправлен"),
    DELIVERED("Получен"),
    CANCELLED("Отменён");

    public final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public static OrderStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return NEW;
        }
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
