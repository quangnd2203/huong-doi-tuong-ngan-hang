package vn.edu.pxu.bank;

import org.junit.jupiter.api.Test;
import vn.edu.pxu.bank.model.account.*;
import vn.edu.pxu.bank.model.transaction.*;
import vn.edu.pxu.bank.model.user.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    private static Customer customer() {
        return new Customer("U01", "customer", "hash1", "KH01", "Nguyễn A",
                "0912345678", "a@bank.vn", "Hà Nội");
    }

    @Test
    void checkingDepositWithdrawalAndClose() {
        CheckingAccount account = new CheckingAccount("CK01", "KH01");
        account.deposit(new BigDecimal("1000000"));
        account.withdraw(new BigDecimal("250000"));
        assertEquals(new BigDecimal("750000"), account.getBalance());

        assertThrows(IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("750001")));
        assertThrows(IllegalArgumentException.class,
                () -> account.deposit(new BigDecimal("1.5")));
        assertThrows(IllegalStateException.class, account::close);

        account.withdraw(new BigDecimal("750000"));
        account.close();
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
        assertThrows(IllegalStateException.class, () -> account.deposit(BigDecimal.ONE));
        account.reopen();
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void customerOwnsAccountsOnceAndEnforcesOwnership() {
        Customer customer = customer();
        customer.addAccount(new CheckingAccount("CK01", "KH01"));

        assertEquals(1, customer.getAccounts().size());
        assertThrows(UnsupportedOperationException.class,
                () -> customer.getAccounts().clear());
        assertThrows(IllegalStateException.class,
                () -> customer.addAccount(new CheckingAccount("CK02", "KH01")));
        assertThrows(IllegalArgumentException.class,
                () -> customer.addAccount(new SavingsAccount("ST01", "KH02",
                        new BigDecimal("1000000"), 3, LocalDate.of(2026, 1, 31))));
    }

    @Test
    void savingsInterestRespectsEarlyWithdrawalAndMaturity() {
        SavingsAccount savings = new SavingsAccount("ST01", "KH01",
                new BigDecimal("2000000"), 3, LocalDate.of(2026, 1, 31));

        assertEquals(LocalDate.of(2026, 4, 30), savings.getEndDate());
        assertEquals(BigDecimal.ZERO, savings.calculateInterest(LocalDate.of(2026, 1, 31)));
        assertEquals(new BigDecimal("307"),
                savings.calculateInterest(LocalDate.of(2026, 2, 28)));
        assertEquals(new BigDecimal("14630"),
                savings.calculateInterest(LocalDate.of(2026, 4, 30)));
        assertEquals(new BigDecimal("14630"),
                savings.calculateInterest(LocalDate.of(2026, 5, 30)));
        assertEquals(new BigDecimal("2014630"),
                savings.settle(LocalDate.of(2026, 4, 30)));
        assertEquals(AccountStatus.INACTIVE, savings.getStatus());
        assertEquals(BigDecimal.ZERO, savings.getBalance());
        assertThrows(IllegalStateException.class,
                () -> savings.settle(LocalDate.of(2026, 4, 30)));
    }

    @Test
    void loanHasFixedTermInterestAndSinglePayment() {
        LoanAccount loan = new LoanAccount("LN01", "KH01",
                new BigDecimal("1000000"), LocalDate.of(2026, 1, 31), 6);

        assertEquals(LocalDate.of(2026, 7, 31), loan.getEndDate());
        assertEquals(new BigDecimal("50000"), loan.calculateInterest());
        assertEquals(new BigDecimal("1050000"), loan.calculateTotalDue());
        assertEquals(new BigDecimal("1050000"), loan.repay());
        assertEquals(AccountStatus.INACTIVE, loan.getStatus());
        assertEquals(BigDecimal.ZERO, loan.getBalance());
        assertThrows(IllegalStateException.class, loan::repay);
    }

    @Test
    void customerCanDeactivateOnlyAfterAccountsAreInactive() {
        Customer customer = customer();
        CheckingAccount checking = new CheckingAccount("CK01", "KH01");
        customer.addAccount(checking);
        assertThrows(IllegalStateException.class, customer::deactivate);

        checking.close();
        customer.deactivate();
        assertEquals(UserStatus.INACTIVE, customer.getStatus());
        assertEquals(1, customer.getAccounts().size());
    }

    @Test
    void onlyOneActiveLoanPerCustomer() {
        Customer customer = customer();
        customer.addAccount(new LoanAccount("LN01", "KH01",
                new BigDecimal("1000000"), LocalDate.of(2026, 1, 31), 6));
        assertThrows(IllegalStateException.class,
                () -> customer.addAccount(new LoanAccount("LN02", "KH01",
                        new BigDecimal("2000000"), LocalDate.of(2026, 1, 31), 12)));
    }

    @Test
    void transactionHasOneRecordForTwoAccountsAndCannotBeEdited() {
        Transaction transaction = new Transaction("TX01", new BigDecimal("500000"),
                "CK01", "CK02", TransactionType.TRANSFER, "Chuyển tiền");
        assertEquals("CK01", transaction.getFromAccountNumber());
        assertEquals("CK02", transaction.getToAccountNumber());
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX02", BigDecimal.ONE, "CK01", "CK01",
                        TransactionType.TRANSFER, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX03", new BigDecimal("1.5"), null, "CK02",
                        TransactionType.DEPOSIT, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX04", BigDecimal.ONE, null, null,
                        TransactionType.WITHDRAWAL, ""));
    }

    @Test
    void passwordResetRequiresChangeAndNeverAppearsInToString() {
        Employee employee = new Employee("E01", "staff", "hash1");
        employee.resetPassword("tempHash");
        assertTrue(employee.isMustChangePassword());
        assertThrows(IllegalArgumentException.class,
                () -> employee.changePassword("wrongHash", "hash2"));
        employee.changePassword("tempHash", "hash2");
        assertFalse(employee.isMustChangePassword());
        assertFalse(employee.toString().contains("hash2"));
    }
}
