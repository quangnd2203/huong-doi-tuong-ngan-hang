package vn.edu.pxu.bank.di;

import org.junit.jupiter.api.Test;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.LoanService;
import vn.edu.pxu.bank.service.ReportService;
import vn.edu.pxu.bank.service.SavingsService;
import vn.edu.pxu.bank.service.TransactionService;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class DITest {
    // Chỉ kiểm tra cấu trúc bằng reflection: không khởi động DI trong JUnit,
    // tránh nạp/ghi file data/bank-data.json thật của người dùng.
    @Test
    void constructorIsPrivateForSingleton() {
        assertEquals(1, DI.class.getDeclaredConstructors().length);
        assertTrue(Modifier.isPrivate(DI.class.getDeclaredConstructors()[0].getModifiers()));
    }

    @Test
    void singletonAccessorIsStaticAndNoCreateDefaultExists() throws Exception {
        assertTrue(Modifier.isStatic(DI.class.getDeclaredMethod("getInstance").getModifiers()));
        assertEquals(DI.class, DI.class.getDeclaredMethod("getInstance").getReturnType());
        assertThrows(NoSuchMethodException.class, () -> DI.class.getDeclaredMethod("createDefault"));
    }

    @Test
    void onlyInternalCodeCanRegisterDependencies() throws Exception {
        assertTrue(Modifier.isPrivate(DI.class
                .getDeclaredMethod("register", Class.class, Object.class).getModifiers()));
        assertTrue(Modifier.isPublic(DI.class.getDeclaredMethod("get", Class.class).getModifiers()));
    }

    @Test
    void singletonInstanceIsPrivateAndNotAssignableFromOutside() throws Exception {
        var field = DI.class.getDeclaredField("instance");
        assertTrue(Modifier.isPrivate(field.getModifiers()));
        assertTrue(Modifier.isStatic(field.getModifiers()));
        assertEquals(DI.class, field.getType());
        assertTrue(Modifier.isFinal(DI.class.getModifiers()));
    }
}
