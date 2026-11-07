package com.russianresale.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.model.PaymentMethod;

/** Заказ покупателя у одного продавца. Корзина с товарами нескольких продавцов делится на несколько заказов. */
@Entity(tableName = "orders", indices = {@Index("buyerId"), @Index("sellerId")})
public class Order {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long buyerId;
    public long sellerId;
    @NonNull
    public String status = OrderStatus.NEW.name();
    @NonNull
    public String deliveryMethod = DeliveryMethod.PICKUP.name();
    public int deliveryCost;
    public String address;
    public String phone;
    public String comment;
    /** Мерки покупателя для вещей, которые шьются на заказ. */
    public String measurements;
    @NonNull
    public String paymentMethod = PaymentMethod.CASH.name();
    public boolean paid;
    public int itemsTotal;
    public int total;
    public String trackNumber;
    public String cancelReason;
    public long createdAt;
    public long updatedAt;

    public OrderStatus statusEnum() {
        return OrderStatus.of(status);
    }

    public DeliveryMethod deliveryEnum() {
        return DeliveryMethod.of(deliveryMethod);
    }

    public PaymentMethod paymentEnum() {
        return PaymentMethod.of(paymentMethod);
    }
}
