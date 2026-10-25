package vn.edu.pxu.bank.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {
    @Test
    void passwordsAreSaltedAndVerified() {
        char[] password = "Hello@123".toCharArray();
        try {
            String first = PasswordUtils.hash(password);
            String second = PasswordUtils.hash(password);
            assertNotEquals(first, second);
            assertTrue(PasswordUtils.verify(password, first));
            assertTrue(PasswordUtils.verify(password, second));
            assertFalse(PasswordUtils.verify("wrong".toCharArray(), first));
            assertFalse(first.contains("Hello@123"));
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @Test
    void emptyAndMalformedPasswordsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.hash(new char[0]));
        assertFalse(PasswordUtils.verify("abc".toCharArray(), "hash1"));
        assertFalse(PasswordUtils.verify("abc".toCharArray(), "pbkdf2$wrong$abc$def"));
        assertFalse(PasswordUtils.verify("abc".toCharArray(), null));
    }
}
