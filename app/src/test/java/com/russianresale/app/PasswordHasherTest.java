package com.russianresale.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.russianresale.app.util.PasswordHasher;

import org.junit.Test;

public class PasswordHasherTest {
    @Test
    public void sameSaltGivesSameHash() {
        assertEquals(PasswordHasher.hash("123456", "salt"), PasswordHasher.hash("123456", "salt"));
    }

    @Test
    public void differentSaltsGiveDifferentHashes() {
        assertNotEquals(PasswordHasher.hash("123456", PasswordHasher.newSalt()),
                PasswordHasher.hash("123456", PasswordHasher.newSalt()));
    }

    @Test
    public void verify() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("секрет", salt);
        assertTrue(PasswordHasher.verify("секрет", salt, hash));
        assertFalse(PasswordHasher.verify("другой", salt, hash));
        assertEquals(64, hash.length());
    }
}
