package vn.edu.pxu.bank.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.pxu.bank.di.DI;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.di.DemoData;
import vn.edu.pxu.bank.util.PasswordUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UiStartupTest {
    @TempDir
    Path directory;

    @Test
    void firstLaunchCreatesTwoDemoUsersWithHashedPasswordsAndOneChecking() {
        JsonRepository repository = JsonRepository.getInstance();
        repository.load(directory.resolve("bank-data.json"));

        DemoData.initialize(repository);
        assertEquals(2, repository.getUsers().size());
        Employee staff = assertInstanceOf(Employee.class, repository.getUsers().get(0));
        Customer customer = assertInstanceOf(Customer.class, repository.getUsers().get(1));
        assertTrue(PasswordUtils.verify("Admin@123".toCharArray(), staff.getPasswordHash()));
        assertTrue(PasswordUtils.verify("Khach@123".toCharArray(), customer.getPasswordHash()));
        assertEquals(1, customer.getAccounts().size());
        assertInstanceOf(CheckingAccount.class, customer.getAccounts().get(0));

        repository.reload();
        DemoData.initialize(repository);
        assertEquals(2, repository.getUsers().size());
        assertEquals(1, ((Customer) repository.getUsers().get(1)).getAccounts().size());
    }

    @Test
    void demoInitializerDoesNotModifyExistingUsers() {
        JsonRepository repository = JsonRepository.getInstance();
        repository.load(directory.resolve("bank-data.json"));
        repository.getUsers().add(new Employee("E1", "existing", "anyhash"));
        repository.save();

        DemoData.initialize(repository);
        assertEquals(1, repository.getUsers().size());
        assertEquals("existing", repository.getUsers().get(0).getUsername());
    }
}
