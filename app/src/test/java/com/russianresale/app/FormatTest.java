package com.russianresale.app;

import static org.junit.Assert.assertEquals;

import com.russianresale.app.util.Format;

import org.junit.Test;

public class FormatTest {
    @Test
    public void price() {
        assertEquals("500 ₽", Format.price(500));
        assertEquals("12 500 ₽", Format.price(12500));
        assertEquals("1 000 000 ₽", Format.price(1_000_000));
    }

    @Test
    public void plural() {
        assertEquals("1 отзыв", Format.plural(1, "отзыв", "отзыва", "отзывов"));
        assertEquals("3 отзыва", Format.plural(3, "отзыв", "отзыва", "отзывов"));
        assertEquals("5 отзывов", Format.plural(5, "отзыв", "отзыва", "отзывов"));
        assertEquals("11 отзывов", Format.plural(11, "отзыв", "отзыва", "отзывов"));
        assertEquals("21 отзыв", Format.plural(21, "отзыв", "отзыва", "отзывов"));
    }

    @Test
    public void orderNumber() {
        assertEquals("№ 100007", Format.orderNumber(7));
    }
}
