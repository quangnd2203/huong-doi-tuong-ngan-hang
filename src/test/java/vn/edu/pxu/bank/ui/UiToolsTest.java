package vn.edu.pxu.bank.ui;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UiToolsTest {
    @Test
    void formatVndGroupsThousandsWithoutChangingValue() {
        BigDecimal amount = new BigDecimal("220000000");
        assertEquals("220.000.000 VND", UiTools.formatVnd(amount));
        assertEquals("0 VND", UiTools.formatVnd(BigDecimal.ZERO));
        assertEquals("15.123 VND", UiTools.formatVnd(new BigDecimal("15123")));
        assertEquals(new BigDecimal("220000000"), amount);
    }

    @Test
    void dateInputHasClearValidation() {
        assertEquals(LocalDate.of(2027, 1, 15), UiTools.date("2027-01-15"));
        assertThrows(IllegalArgumentException.class, () -> UiTools.date("15/01/2027"));
        assertThrows(IllegalArgumentException.class, () -> UiTools.date("2027-02-31"));
    }
}
