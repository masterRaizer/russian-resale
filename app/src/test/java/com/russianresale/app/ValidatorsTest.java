package com.russianresale.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.russianresale.app.util.Validators;

import org.junit.Test;

public class ValidatorsTest {
    @Test
    public void email() {
        assertTrue(Validators.isValidEmail("buyer@resale.ru"));
        assertTrue(Validators.isValidEmail(" anna.smirnova@mail.ru "));
        assertFalse(Validators.isValidEmail("anna@"));
        assertFalse(Validators.isValidEmail("anna.ru"));
        assertFalse(Validators.isValidEmail(null));
    }

    @Test
    public void password() {
        assertTrue(Validators.isValidPassword("123456"));
        assertFalse(Validators.isValidPassword("12345"));
        assertFalse(Validators.isValidPassword(null));
    }

    @Test
    public void phone() {
        assertTrue(Validators.isValidPhone("+79161234567"));
        assertTrue(Validators.isValidPhone("+7 (916) 123-45-67"));
        assertTrue(Validators.isValidPhone("89161234567"));
        assertFalse(Validators.isValidPhone("12345"));
        assertFalse(Validators.isValidPhone("телефон"));
    }

    @Test
    public void price() {
        assertTrue(Validators.isValidPrice(100));
        assertTrue(Validators.isValidPrice(32000));
        assertFalse(Validators.isValidPrice(99));
        assertFalse(Validators.isValidPrice(5_000_001));
    }

    @Test
    public void rating() {
        assertTrue(Validators.isValidRating(1));
        assertTrue(Validators.isValidRating(5));
        assertFalse(Validators.isValidRating(0));
        assertFalse(Validators.isValidRating(6));
    }

    @Test
    public void cardNumberUsesLuhn() {
        assertTrue(Validators.isValidCardNumber("4242 4242 4242 4242"));
        assertTrue(Validators.isValidCardNumber("5555555555554444"));
        assertFalse(Validators.isValidCardNumber("4242 4242 4242 4241"));
        assertFalse(Validators.isValidCardNumber("4242"));
    }

    @Test
    public void cardExpiry() {
        assertTrue(Validators.isValidCardExpiry("12/30"));
        assertFalse(Validators.isValidCardExpiry("13/30"));
        assertFalse(Validators.isValidCardExpiry("1230"));
    }

    @Test
    public void parseInt() {
        assertEquals(Integer.valueOf(12500), Validators.parseIntOrNull("12 500"));
        assertNull(Validators.parseIntOrNull(""));
        assertNull(Validators.parseIntOrNull("abc"));
    }
}
