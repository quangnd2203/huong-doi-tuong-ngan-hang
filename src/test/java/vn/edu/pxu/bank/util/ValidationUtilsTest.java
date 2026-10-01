package vn.edu.pxu.bank.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {
    @Test
    void textAndEmailAreTrimmedAndValidated() {
        assertEquals("KH01", ValidationUtils.requireText("  KH01  ", "Mã"));
        assertEquals("a@bank.vn", ValidationUtils.requireEmail(" a@bank.vn "));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireText("   ", "Mã"));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireEmail("invalid"));
    }

    @Test
    void newUsernamesRequireLowercaseLettersDigitsAndUnderscore() {
        assertEquals("khach_123", ValidationUtils.requireUsername("khach_123"));
        assertEquals("abcd", ValidationUtils.requireUsername("abcd"));
        assertEquals("a1234567890123456789", ValidationUtils.requireUsername("a1234567890123456789"));
        assertEquals("khach02", ValidationUtils.requireUsername("  khach02  "));

        for (String invalid : new String[]{"ád;lkas;d kas;dkas;dkas;",
                "abc", "a12345678901234567890", "1khach", "_khach",
                "Khach", "nguyễn", "khach 123", "khach;123", "khach@123"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.requireUsername(invalid), invalid);
        }
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireUsername(null));
    }

    @Test
    void vndMustBeWholeAndWithinAllowedSign() {
        assertEquals(new BigDecimal("100"), ValidationUtils.requireWholeVnd(new BigDecimal("100.00"), false));
        assertEquals(BigDecimal.ZERO, ValidationUtils.requireWholeVnd(BigDecimal.ZERO, true));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireWholeVnd(BigDecimal.ZERO, false));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireWholeVnd(new BigDecimal("-1"), true));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireWholeVnd(new BigDecimal("100.5"), false));
    }

    @Test
    void ratesMustBePositiveAndAtMostOne() {
        assertEquals(new BigDecimal("0.03"), ValidationUtils.requireRate(new BigDecimal("0.03")));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireRate(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireRate(new BigDecimal("1.01")));
    }
}
