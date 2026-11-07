package com.russianresale.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.util.OrderRules;

import org.junit.Test;

public class OrderRulesTest {
    @Test
    public void sellerFlow() {
        assertTrue(OrderRules.sellerCanAccept(OrderStatus.NEW));
        assertFalse(OrderRules.sellerCanAccept(OrderStatus.ACCEPTED));
        assertTrue(OrderRules.sellerCanShip(OrderStatus.ACCEPTED));
        assertFalse(OrderRules.sellerCanShip(OrderStatus.NEW));
        assertTrue(OrderRules.sellerCanCancel(OrderStatus.ACCEPTED));
        assertFalse(OrderRules.sellerCanCancel(OrderStatus.SHIPPED));
    }

    @Test
    public void buyerFlow() {
        assertTrue(OrderRules.buyerCanCancel(OrderStatus.NEW));
        assertFalse(OrderRules.buyerCanCancel(OrderStatus.ACCEPTED));
        assertTrue(OrderRules.buyerCanConfirm(OrderStatus.SHIPPED));
        assertFalse(OrderRules.buyerCanConfirm(OrderStatus.ACCEPTED));
        assertTrue(OrderRules.buyerCanReview(OrderStatus.DELIVERED));
        assertFalse(OrderRules.buyerCanReview(OrderStatus.SHIPPED));
    }

    @Test
    public void steps() {
        assertEquals(0, OrderRules.step(OrderStatus.NEW));
        assertEquals(3, OrderRules.step(OrderStatus.DELIVERED));
        assertEquals(-1, OrderRules.step(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CANCELLED.isFinal());
        assertFalse(OrderStatus.SHIPPED.isFinal());
    }
}
