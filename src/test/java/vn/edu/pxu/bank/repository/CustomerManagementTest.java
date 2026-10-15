package vn.edu.pxu.bank.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.model.user.UserStatus;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.AccountServiceImpl;
import vn.edu.pxu.bank.util.PasswordUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CustomerManagementTest {
    @TempDir
    Path directory;

    private AccountService accounts() {
        JsonRepository repository = JsonRepository.getInstance();
        repository.load(directory.resolve("bank-data.json"));
        repository.getUsers().add(new Employee("U001", "employee", "hash"));
        repository.getUsers().add(new Customer("U002", "existing", "hash", "KH001",
                "Người có sẵn", "0900000001", "old@example.com", "Đà Nẵng"));
        repository.save();
        return new AccountServiceImpl(repository);
    }

    @Test
    void authenticationAndPasswordChangeStayInsideService() {
        AccountService service = accounts();
        char[] temporary = "Temporary2026".toCharArray();
        Customer customer = service.createCustomer("nguyenvanb", "Nguyễn Văn B",
                "0911111111", "new@example.com", "Huế", temporary);

        assertNull(service.authenticate("nguyenvanb", "wrong".toCharArray()));
        assertNull(service.authenticate("nobody", temporary));
        assertEquals(customer.getUserId(), service.authenticate("nguyenvanb", temporary).getUserId());
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(customer.getUserId(), "wrong".toCharArray(), "NewPassword2026".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(customer.getUserId(), temporary, "123".toCharArray()));
        assertTrue(customer.isMustChangePassword());

        char[] changed = "NewPassword2026".toCharArray();
        service.changePassword(customer.getUserId(), temporary, changed);
        assertFalse(customer.isMustChangePassword());
        assertNull(service.authenticate("nguyenvanb", temporary));
        assertNotNull(service.authenticate("nguyenvanb", changed));
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(customer.getUserId(), changed, changed));

        JsonRepository.getInstance().reload();
        assertNotNull(service.authenticate("nguyenvanb", changed));
        assertFalse(service.findCustomer(customer.getCustomerId()).isMustChangePassword());
    }

    @Test
    void customerListingIsReadOnlyAndIncludesNewCustomers() {
        AccountService service = accounts();
        assertEquals(1, service.findAllCustomers().size());
        service.createCustomer("newcustomer", "Nguyễn Văn B", "0911111111",
                "new@example.com", "Huế", "Temporary2026".toCharArray());
        assertEquals(2, service.findAllCustomers().size());
        assertThrows(UnsupportedOperationException.class,
                () -> service.findAllCustomers().clear());
    }

    @Test
    void createsCustomerWithUniqueIdsAndTemporaryHashedPassword() {
        AccountService service = accounts();
        char[] password = "Temporary2026".toCharArray();
        Customer created = service.createCustomer("newcustomer", "Nguyễn Văn B",
                "0911111111", "new@example.com", "Huế", password);
        assertEquals("U000003", created.getUserId());
        assertEquals("KH000002", created.getCustomerId());
        assertEquals(0, created.getAccounts().size());
        assertTrue(created.isMustChangePassword());
        assertTrue(PasswordUtils.verify(password, created.getPasswordHash()));
        assertFalse(created.getPasswordHash().contains("Temporary2026"));

        JsonRepository.getInstance().reload();
        Customer saved = service.findCustomer(created.getCustomerId());
        assertEquals("newcustomer", saved.getUsername());
        assertTrue(saved.isMustChangePassword());
        assertTrue(PasswordUtils.verify(password, saved.getPasswordHash()));
        assertEquals("CK000001", service.openCheckingAccount(saved.getCustomerId()).getAccountNumber());
    }

    @Test
    void duplicateUsernameOrInvalidFieldsDoNotCreateCustomer() {
        AccountService service = accounts();
        int count = JsonRepository.getInstance().getUsers().size();
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("existing", "Nguyễn Văn B", "0911111111",
                        "new@example.com", "Huế", "Temporary2026".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("ád;lkas;d kas;dkas;dkas;", "Nguyễn Văn B",
                        "0911111111", "new@example.com", "Huế", "Temporary2026".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("khach 123", "Nguyễn Văn B",
                        "0911111111", "new@example.com", "Huế", "Temporary2026".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("unique", "Nguyễn Văn B", "0911111111",
                        "invalid-email", "Huế", "Temporary2026".toCharArray()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("unique", "Nguyễn Văn B", "0911111111",
                        "new@example.com", "Huế", "weak".toCharArray()));
        assertEquals(count, JsonRepository.getInstance().getUsers().size());
        JsonRepository.getInstance().reload();
        assertEquals(count, JsonRepository.getInstance().getUsers().size());
    }

    @Test
    void usernameConflictingWithLegacyMixedCaseIsRejected() {
        AccountService service = accounts();
        JsonRepository repository = JsonRepository.getInstance();
        repository.getUsers().add(new Employee("U003", "OldLogin", "hash"));
        repository.save();
        assertThrows(IllegalArgumentException.class,
                () -> service.createCustomer("oldlogin", "Nguyễn Văn B",
                        "0911111111", "new@example.com", "Huế", "Temporary2026".toCharArray()));
        assertEquals(3, repository.getUsers().size());

        // Dữ liệu cũ có tên đăng nhập không hợp lệ vẫn phải nạp lại được.
        String legacyUsername = "ád;lkas;d kas;dkas;dkas;";
        repository.getUsers().add(new Customer("U004", legacyUsername, "hash", "KH002",
                "Khách cũ", "0911111111", "legacy@example.com", "Huế"));
        repository.save();
        repository.reload();
        assertEquals(legacyUsername, service.findCustomer("KH002").getUsername());
    }

    @Test
    void editValidatesEverythingBeforeAnyFieldChanges() {
        AccountService service = accounts();
        Customer customer = service.findCustomer("KH001");
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCustomer("KH001", "Tên mới", "0909999999",
                        "not-email", "Hà Nội"));
        assertEquals("Người có sẵn", customer.getFullName());
        assertEquals("0900000001", customer.getPhone());

        service.updateCustomer("KH001", "Tên mới", "0909999999",
                "updated@example.com", "Hà Nội");
        JsonRepository.getInstance().reload();
        Customer restored = service.findCustomer("KH001");
        assertEquals("Tên mới", restored.getFullName());
        assertEquals("updated@example.com", restored.getEmail());
        assertEquals("Hà Nội", restored.getAddress());
    }

    @Test
    void resetsPasswordAndRequiresChangeOnNextLogin() {
        AccountService service = accounts();
        char[] password = "AnotherPass2026".toCharArray();
        service.resetCustomerPassword("KH001", password);
        JsonRepository.getInstance().reload();
        Customer customer = service.findCustomer("KH001");
        assertTrue(customer.isMustChangePassword());
        assertTrue(PasswordUtils.verify(password, customer.getPasswordHash()));
        assertThrows(IllegalArgumentException.class,
                () -> service.resetCustomerPassword("KH001", "weak".toCharArray()));
    }

    @Test
    void cannotDeactivateCustomerWithActiveAccountButCanAfterClosing() {
        AccountService service = accounts();
        CheckingAccount checking = service.openCheckingAccount("KH001");
        assertThrows(IllegalStateException.class, () -> service.deactivateCustomer("KH001"));
        assertEquals(UserStatus.ACTIVE, service.findCustomer("KH001").getStatus());
        service.closeCheckingAccount(checking.getAccountNumber());
        assertEquals(AccountStatus.INACTIVE, checking.getStatus());
        service.deactivateCustomer("KH001");

        JsonRepository.getInstance().reload();
        assertEquals(UserStatus.INACTIVE, service.findCustomer("KH001").getStatus());
        assertThrows(IllegalStateException.class, () -> service.reopenCheckingAccount(checking.getAccountNumber()));
    }
}
