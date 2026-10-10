package vn.edu.pxu.bank.repository;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.model.user.User;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonRepositoryTest {
    @TempDir
    Path folder;

    @Test
    void singletonIsStable() {
        assertSame(JsonRepository.getInstance(), JsonRepository.getInstance());
    }

    @Test
    void missingFileStartsEmptyAndDoesNotCreateFile() {
        Path file = folder.resolve("data/bank-data.json");
        JsonRepository repo = JsonRepository.getInstance();
        repo.load(file);

        assertTrue(repo.getUsers().isEmpty());
        assertTrue(repo.getTransactions().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void roundTripRestoresRealSubclassesAccountsDatesAndTransactions() throws Exception {
        Path file = folder.resolve("data/bank-data.json");
        JsonRepository repo = JsonRepository.getInstance();
        repo.load(file);

        Customer customer = new Customer("U01", "customer", "hashed", "KH01",
                "Nguyễn Văn A", "0123456789", "a@example.com", "Hà Nội");
        CheckingAccount checking = new CheckingAccount("CK01", "KH01");
        checking.deposit(new BigDecimal("2000000"));
        customer.addAccount(checking);
        customer.addAccount(new SavingsAccount("ST01", "KH01",
                new BigDecimal("1000000"), 3, LocalDate.of(2026, 1, 31)));
        customer.addAccount(new LoanAccount("LN01", "KH01",
                new BigDecimal("5000000"), LocalDate.of(2026, 1, 31), 6));

        repo.getUsers().add(customer);
        repo.getUsers().add(new Employee("E01", "staff", "hash-staff"));
        LocalDateTime date = LocalDateTime.of(2026, 2, 2, 14, 25);
        repo.getTransactions().add(new Transaction("TX01", new BigDecimal("2000000"),
                null, "CK01", TransactionType.DEPOSIT, date, "Nộp tại quầy"));
        repo.getTransactions().add(new Transaction("TX02", new BigDecimal("250000"),
                "CK01", "CK02", TransactionType.TRANSFER, date, "Chuyển khoản"));

        repo.save();
        String contents = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(contents.contains("\"kind\": \"CUSTOMER\""));
        assertTrue(contents.contains("\"kind\": \"SAVINGS\""));
        assertTrue(contents.contains("\"kind\": \"LOAN\""));
        JsonObject root = JsonParser.parseString(contents).getAsJsonObject();
        assertFalse(root.has("accounts")); // Không lưu tài khoản thêm ở cấp gốc.
        assertEquals(2, root.getAsJsonArray("users").size());
        assertEquals(3, root.getAsJsonArray("users").get(0)
                .getAsJsonObject().getAsJsonArray("accounts").size());
        assertFalse(contents.matches("(?s).*\\n\\s*\"accounts\"\\s*:\\s*\\[.*")
                && !contents.contains("\"users\"")); // accounts only inside users

        repo.load(file);
        assertEquals(2, repo.getUsers().size());
        assertInstanceOf(Employee.class, repo.getUsers().get(1));
        Customer restored = assertInstanceOf(Customer.class, repo.getUsers().get(0));
        assertEquals(3, restored.getAccounts().size());
        BankAccount restoredChecking = restored.getAccounts().get(0);
        SavingsAccount restoredSavings = assertInstanceOf(SavingsAccount.class, restored.getAccounts().get(1));
        LoanAccount restoredLoan = assertInstanceOf(LoanAccount.class, restored.getAccounts().get(2));
        assertInstanceOf(CheckingAccount.class, restoredChecking);
        assertEquals(new BigDecimal("2000000"), restoredChecking.getBalance());
        assertEquals(LocalDate.of(2026, 4, 30), restoredSavings.getEndDate());
        assertEquals(new BigDecimal("7315"), restoredSavings.calculateInterest(restoredSavings.getEndDate()));
        assertEquals(new BigDecimal("250000"), restoredLoan.calculateInterest());
        assertEquals(AccountStatus.ACTIVE, restoredLoan.getStatus());
        assertEquals(2, repo.getTransactions().size());
        assertEquals(date, repo.getTransactions().get(0).getOccurredAt());
        assertEquals(TransactionType.TRANSFER, repo.getTransactions().get(1).getType());
    }

    @Test
    void corruptedExistingFileBlocksSaveAndKeepsBytes() throws Exception {
        Path file = folder.resolve("bank-data.json");
        String broken = "{\"users\":[{ invalid }], \"transactions\":[]}";
        Files.writeString(file, broken, StandardCharsets.UTF_8);
        JsonRepository repo = JsonRepository.getInstance();

        assertThrows(IllegalStateException.class, () -> repo.load(file));
        assertThrows(IllegalStateException.class, repo::save);
        assertEquals(broken, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void malformedKnownClassIsRejectedWithoutOverwriting() throws Exception {
        Path file = folder.resolve("bank-data.json");
        String broken = """
                {"users":[{"kind":"UNKNOWN","userId":"U1"}],"transactions":[]}
                """;
        Files.writeString(file, broken, StandardCharsets.UTF_8);
        JsonRepository repo = JsonRepository.getInstance();

        assertThrows(IllegalStateException.class, () -> repo.load(file));
        assertThrows(IllegalStateException.class, repo::save);
        assertEquals(broken, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void duplicateAccountAcrossCustomersFailsBeforeSave() {
        Path file = folder.resolve("bank-data.json");
        JsonRepository repo = JsonRepository.getInstance();
        repo.load(file);
        Customer first = new Customer("U01", "a", "hash", "KH01",
                "A", "0123456789", "a@example.com", "Nha");
        Customer second = new Customer("U02", "b", "hash", "KH02",
                "B", "0123456788", "b@example.com", "Nha");
        first.addAccount(new CheckingAccount("DUP", "KH01"));
        second.addAccount(new CheckingAccount("DUP", "KH02"));
        repo.getUsers().add(first);
        repo.getUsers().add(second);

        assertThrows(IllegalStateException.class, repo::save);
        assertFalse(Files.exists(file));
    }
}
