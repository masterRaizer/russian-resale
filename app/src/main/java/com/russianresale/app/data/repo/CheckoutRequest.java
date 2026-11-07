package com.russianresale.app.data.repo;

import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.data.model.PaymentMethod;

/** Данные оформления заказа. Если buyNowListingId задан — покупка одной вещи, иначе вся корзина. */
public class CheckoutRequest {
    public Long buyNowListingId;
    public int buyNowQuantity = 1;
    public DeliveryMethod delivery = DeliveryMethod.PICKUP;
    public PaymentMethod payment = PaymentMethod.CARD;
    public String address;
    public String phone;
    public String comment;
    public String measurements;
}
