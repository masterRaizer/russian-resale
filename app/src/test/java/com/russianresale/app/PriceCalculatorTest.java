package com.russianresale.app;

import static org.junit.Assert.assertEquals;

import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.util.PriceCalculator;

import org.junit.Test;

public class PriceCalculatorTest {
    @Test
    public void pickupIsFree() {
        assertEquals(0, PriceCalculator.deliveryCost(DeliveryMethod.PICKUP, 1000));
    }

    @Test
    public void deliveryIsPaidBelowThreshold() {
        assertEquals(250, PriceCalculator.deliveryCost(DeliveryMethod.CDEK, 3000));
        assertEquals(3290, PriceCalculator.total(3000, DeliveryMethod.POST));
    }

    @Test
    public void deliveryIsFreeFromThreshold() {
        assertEquals(0, PriceCalculator.deliveryCost(DeliveryMethod.CDEK, PriceCalculator.FREE_DELIVERY_FROM));
        assertEquals(0, PriceCalculator.deliveryCost(DeliveryMethod.POST, 10000));
    }

    @Test
    public void courierIsAlwaysPaid() {
        assertEquals(450, PriceCalculator.deliveryCost(DeliveryMethod.COURIER, 50000));
    }

    @Test
    public void commission() {
        assertEquals(500, PriceCalculator.commission(10000));
    }
}
