package vn.edu.pxu.bank.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.pxu.bank.exception.InsufficientBalanceException;
import vn.edu.pxu.bank.exception.NotFoundException;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.service.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {
    @TempDir
    Path directory;

    private JsonRepository repository() {
        JsonRepository repository = JsonRepository.getInstance();
        repository.load(directory.resolve("bank-data.json"));
        repository.getUsers().add(customer("U1", "alice", "KH1"));
        repository.getUsers().add(customer("U2", "bob", "KH2"));
        repository.save();
        return repository;
    }

    private Customer customer(String userId, String username, String customerId) {
        return new Customer(userId, username, "hash", customerId, username,
                "0912345678", username + "@example.com", "Hà Nội");
    }

    private Customer owner(JsonRepository repository, String customerId) {
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer && customer.getCustomerId().equals(customerId)) {
                return customer;
            }
        }
        throw new AssertionError("Không tìm thấy khách");
    }

    @Test
    void transactionLookupIsFilteredByAccountAndCustomer() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        CheckingAccount first = accounts.openCheckingAccount("KH1");
        CheckingAccount second = accounts.openCheckingAccount("KH2");
        transactions.deposit(first.getAccountNumber(), new BigDecimal("100000"));
        transactions.transfer(first.getAccountNumber(), second.getAccountNumber(),
                new BigDecimal("10000"));

        assertEquals(2, transactions.findByAccountNumber(first.getAccountNumber()).size());
        assertEquals(1, transactions.findByAccountNumber(second.getAccountNumber()).size());
        assertEquals(2, transactions.findByCustomerId("KH1").size());
        assertEquals(1, transactions.findByCustomerId("KH2").size());
        assertThrows(NotFoundException.class, () -> transactions.findByAccountNumber("NONE"));
        assertThrows(NotFoundException.class, () -> transactions.findByCustomerId("NONE"));
        assertThrows(UnsupportedOperationException.class,
                () -> transactions.findByCustomerId("KH1").clear());
    }

    @Test
    void interestQueriesThroughServicesMatchModelAndRejectSettledSavings() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        SavingsService savings = new SavingsServiceImpl(repository);
        LoanService loans = new LoanServiceImpl(repository);
        CheckingAccount checking = accounts.openCheckingAccount("KH1");
        transactions.deposit(checking.getAccountNumber(), new BigDecimal("5000000"));
        SavingsAccount book = savings.openSavingsAccount("KH1", new BigDecimal("1000000"), 3);
        assertEquals(book.calculateInterest(book.getEndDate()),
                savings.calculateInterest(book.getAccountNumber(), book.getEndDate()));

        LoanAccount loan = loans.openLoanAccount("KH1", new BigDecimal("1000000"),
                new BigDecimal("0.10"), LocalDate.now().plusMonths(6));
        assertEquals(loan.calculateInterest(), loans.calculateInterest(loan.getAccountNumber()));
        assertEquals(loan.calculateTotalDue(), loans.calculateTotalDue(loan.getAccountNumber()));
        savings.settleSavingsAccount(book.getAccountNumber());
        assertThrows(IllegalStateException.class,
                () -> savings.calculateInterest(book.getAccountNumber(), book.getEndDate()));
    }

    @Test
    void accountCreationCloseReopenAndLookupPersist() {
        JsonRepository repository = repository();
        AccountService account = new AccountServiceImpl(repository);
        CheckingAccount checking = account.openCheckingAccount("KH1");
        assertEquals("CK000001", checking.getAccountNumber());
        assertEquals(1, account.findByCustomerId("KH1").size());
        assertSame(checking, account.findByAccountNumber(checking.getAccountNumber()));
        assertThrows(IllegalStateException.class, () -> account.openCheckingAccount("KH1"));
        assertThrows(NotFoundException.class, () -> account.findByAccountNumber("NONE"));

        account.closeCheckingAccount(checking.getAccountNumber());
        assertEquals(AccountStatus.INACTIVE, checking.getStatus());
        assertThrows(IllegalStateException.class, () -> account.openCheckingAccount("KH1"));
        account.reopenCheckingAccount(checking.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, checking.getStatus());

        repository.reload();
        CheckingAccount restored = (CheckingAccount) new AccountServiceImpl(repository)
                .findByAccountNumber(checking.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, restored.getStatus());
        assertEquals(checking.getAccountNumber(), restored.getAccountNumber());
    }

    @Test
    void depositWithdrawTransferAreRecordedOnceAndSurviveReload() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        CheckingAccount first = accounts.openCheckingAccount("KH1");
        CheckingAccount second = accounts.openCheckingAccount("KH2");

        Transaction deposited = transactions.deposit(first.getAccountNumber(), new BigDecimal("1000000"));
        Transaction withdrawn = transactions.withdraw(first.getAccountNumber(), new BigDecimal("100000"));
        Transaction transferred = transactions.transfer(first.getAccountNumber(),
                second.getAccountNumber(), new BigDecimal("300000"));

        assertEquals(TransactionType.DEPOSIT, deposited.getType());
        assertEquals(TransactionType.WITHDRAWAL, withdrawn.getType());
        assertEquals(TransactionType.TRANSFER, transferred.getType());
        assertEquals(new BigDecimal("600000"), first.getBalance());
        assertEquals(new BigDecimal("300000"), second.getBalance());
        assertEquals(3, repository.getTransactions().size());
        assertNotEquals(deposited.getTransactionId(), withdrawn.getTransactionId());
        assertNotEquals(withdrawn.getTransactionId(), transferred.getTransactionId());
        assertThrows(InsufficientBalanceException.class,
                () -> transactions.transfer(second.getAccountNumber(), first.getAccountNumber(),
                        new BigDecimal("300001")));
        assertThrows(IllegalArgumentException.class,
                () -> transactions.transfer(first.getAccountNumber(), first.getAccountNumber(), BigDecimal.ONE));
        assertEquals(3, repository.getTransactions().size());

        repository.reload();
        assertEquals(3, repository.getTransactions().size());
        assertEquals(new BigDecimal("600000"),
                ((CheckingAccount) accounts.findByAccountNumber(first.getAccountNumber())).getBalance());
        assertEquals(new BigDecimal("300000"),
                ((CheckingAccount) accounts.findByAccountNumber(second.getAccountNumber())).getBalance());
    }

    @Test
    void savingsOpenAndEarlySettlementWriteTwoTransactions() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        SavingsService savings = new SavingsServiceImpl(repository);
        CheckingAccount checking = accounts.openCheckingAccount("KH1");
        transactions.deposit(checking.getAccountNumber(), new BigDecimal("3000000"));

        SavingsAccount account = savings.openSavingsAccount("KH1", new BigDecimal("1000000"), 3);
        assertEquals(new BigDecimal("2000000"), checking.getBalance());
        assertEquals(2, repository.getTransactions().size()); // Nộp và mở sổ
        assertEquals(TransactionType.SAVINGS_OPEN, repository.getTransactions().get(1).getType());
        savings.settleSavingsAccount(account.getAccountNumber());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
        assertEquals(new BigDecimal("3000000"), checking.getBalance());
        assertEquals(3, repository.getTransactions().size());
        Transaction settled = repository.getTransactions().get(2);
        assertEquals(TransactionType.SAVINGS_SETTLEMENT, settled.getType());
        assertTrue(settled.getNote().contains("Gốc:"));
        assertTrue(settled.getNote().contains("lãi:"));
        assertThrows(IllegalStateException.class, () -> savings.settleSavingsAccount(account.getAccountNumber()));
        assertThrows(IllegalStateException.class, () -> accounts.closeCheckingAccount(checking.getAccountNumber()));
    }

    @Test
    void maturedSavingsAreSettledExactlyOnceBeforeAReadOperation() {
        JsonRepository repository = repository();
        Customer customer = owner(repository, "KH1");
        CheckingAccount checking = new CheckingAccount("CK01", customer.getCustomerId());
        customer.addAccount(checking);
        SavingsAccount matured = new SavingsAccount("ST01", customer.getCustomerId(),
                new BigDecimal("2000000"), 3, LocalDate.now().minusMonths(4));
        customer.addAccount(matured);
        repository.save();

        AccountService service = new AccountServiceImpl(repository);
        service.findByCustomerId("KH1"); // Tự kiểm tra sổ đến hạn.
        assertEquals(AccountStatus.INACTIVE, matured.getStatus());
        assertEquals(1, repository.getTransactions().size());
        assertEquals(TransactionType.SAVINGS_SETTLEMENT, repository.getTransactions().get(0).getType());
        assertEquals(new BigDecimal("2000000").add(
                new BigDecimal("2000000").multiply(new BigDecimal("0.03"))
                        .multiply(BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(
                                matured.getStartDate(), matured.getEndDate())))
                        .divide(BigDecimal.valueOf(365), 0, java.math.RoundingMode.HALF_UP)),
                checking.getBalance());
        service.findByCustomerId("KH1");
        assertEquals(1, repository.getTransactions().size());
        repository.reload();
        assertEquals(AccountStatus.INACTIVE, ((SavingsAccount)
                owner(repository, "KH1").getAccounts().get(1)).getStatus());
    }

    @Test
    void cannotCloseCheckingWhenSavingsActiveEvenIfBalanceZero() {
        JsonRepository repository = repository();
        Customer customer = owner(repository, "KH1");
        CheckingAccount checking = new CheckingAccount("CK01", "KH1");
        customer.addAccount(checking);
        customer.addAccount(new SavingsAccount("ST01", "KH1",
                new BigDecimal("1000000"), 12, LocalDate.now()));
        repository.save();

        AccountService accounts = new AccountServiceImpl(repository);
        assertThrows(IllegalStateException.class, () -> accounts.closeCheckingAccount("CK01"));
        assertEquals(AccountStatus.ACTIVE, checking.getStatus());
    }

    @Test
    void loanDisbursementRepaymentAndOneActiveLoanRestriction() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        LoanService loans = new LoanServiceImpl(repository);
        CheckingAccount checking = accounts.openCheckingAccount("KH1");
        LocalDate endDate = LocalDate.now().plusMonths(6);

        LoanAccount loan = loans.openLoanAccount("KH1", new BigDecimal("1000000"),
                new BigDecimal("0.10"), endDate);
        assertEquals(new BigDecimal("1000000"), checking.getBalance());
        assertEquals(new BigDecimal("50000"), loan.calculateInterest());
        assertEquals(TransactionType.LOAN_DISBURSEMENT, repository.getTransactions().get(0).getType());
        assertThrows(IllegalStateException.class, () -> loans.openLoanAccount(
                "KH1", BigDecimal.ONE, new BigDecimal("0.10"), endDate));
        assertThrows(IllegalArgumentException.class, () -> loans.openLoanAccount(
                "KH2", BigDecimal.ONE, new BigDecimal("0.11"), endDate));
        assertThrows(IllegalStateException.class, () -> accounts.closeCheckingAccount(checking.getAccountNumber()));
        assertThrows(InsufficientBalanceException.class, () -> loans.repayLoanAccount(loan.getAccountNumber()));
        transactions.deposit(checking.getAccountNumber(), new BigDecimal("50000"));
        loans.repayLoanAccount(loan.getAccountNumber());

        assertEquals(AccountStatus.INACTIVE, loan.getStatus());
        assertEquals(BigDecimal.ZERO, checking.getBalance());
        assertEquals(TransactionType.LOAN_REPAYMENT, repository.getTransactions().get(2).getType());
        assertEquals(new BigDecimal("1050000"), repository.getTransactions().get(2).getAmount());
        assertThrows(IllegalStateException.class, () -> loans.repayLoanAccount(loan.getAccountNumber()));

        loans.openLoanAccount("KH1", new BigDecimal("2000000"),
                new BigDecimal("0.10"), LocalDate.now().plusMonths(12));
        assertEquals(2, owner(repository, "KH1").getAccounts().stream()
                .filter(a -> a instanceof LoanAccount).count());
        repository.reload();
        assertEquals(4, repository.getTransactions().size());
    }

    @Test
    void reportsSeparateDepositsFromLoansAndCountSevenTypes() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        LoanService loans = new LoanServiceImpl(repository);
        ReportService reports = new ReportServiceImpl(repository);
        CheckingAccount checking = accounts.openCheckingAccount("KH1");
        transactions.deposit(checking.getAccountNumber(), new BigDecimal("200000"));
        loans.openLoanAccount("KH1", new BigDecimal("1000000"),
                new BigDecimal("0.10"), LocalDate.now().plusMonths(6));

        String accountReport = reports.generateAccountReport();
        assertTrue(accountReport.contains("Tổng tiền gửi: 1200000 VND"));
        assertTrue(accountReport.contains("Tổng dư nợ gốc: 1000000 VND"));
        String report = reports.generateTodayTransactionReport();
        assertTrue(report.contains("DEPOSIT: 1 giao dịch, 200000 VND"));
        assertTrue(report.contains("LOAN_DISBURSEMENT: 1 giao dịch, 1000000 VND"));
        assertTrue(report.contains("SAVINGS_OPEN: 0 giao dịch, 0 VND"));
        assertTrue(report.contains("LOAN_REPAYMENT: 0 giao dịch, 0 VND"));
        assertEquals(7, report.lines().filter(s -> s.contains("giao dịch,")).count());
        assertThrows(IllegalArgumentException.class, () -> reports.generateTransactionReport(
                LocalDate.now(), LocalDate.now().minusDays(1)));
        String yesterday = reports.generateTransactionReport(LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(1));
        assertTrue(yesterday.contains("DEPOSIT: 0 giao dịch, 0 VND"));
    }


    @Test
    void reportingIncludesAllSevenTransactionCategoriesExactlyOnce() {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        SavingsService savings = new SavingsServiceImpl(repository);
        LoanService loans = new LoanServiceImpl(repository);
        ReportService reports = new ReportServiceImpl(repository);
        CheckingAccount first = accounts.openCheckingAccount("KH1");
        CheckingAccount second = accounts.openCheckingAccount("KH2");

        transactions.deposit(first.getAccountNumber(), new BigDecimal("5000000"));
        transactions.withdraw(first.getAccountNumber(), new BigDecimal("100000"));
        transactions.transfer(first.getAccountNumber(), second.getAccountNumber(), new BigDecimal("100000"));
        SavingsAccount book = savings.openSavingsAccount("KH1", new BigDecimal("1000000"), 3);
        savings.settleSavingsAccount(book.getAccountNumber());
        LoanAccount loan = loans.openLoanAccount("KH1", new BigDecimal("1000000"),
                new BigDecimal("0.10"), LocalDate.now().plusMonths(6));
        loans.repayLoanAccount(loan.getAccountNumber());

        assertEquals(7, repository.getTransactions().size());
        String report = reports.generateTodayTransactionReport();
        for (TransactionType type : TransactionType.values()) {
            assertTrue(report.contains(type + ": 1 giao dịch,"), type.toString());
        }
        assertTrue(report.contains("SAVINGS_SETTLEMENT: 1 giao dịch, 1000000 VND"));
        assertTrue(report.contains("LOAN_REPAYMENT: 1 giao dịch, 1050000 VND"));
        assertEquals(new BigDecimal("4750000"), first.getBalance());
        assertEquals(new BigDecimal("100000"), second.getBalance());
    }

    @Test
    void severalMaturedSavingsProduceDifferentTransactionIds() {
        JsonRepository repository = repository();
        Customer customer = owner(repository, "KH1");
        CheckingAccount checking = new CheckingAccount("CK01", "KH1");
        customer.addAccount(checking);
        SavingsAccount first = new SavingsAccount("ST01", "KH1",
                new BigDecimal("1000000"), 3, LocalDate.now().minusMonths(5));
        SavingsAccount second = new SavingsAccount("ST02", "KH1",
                new BigDecimal("2000000"), 3, LocalDate.now().minusMonths(4));
        customer.addAccount(first);
        customer.addAccount(second);
        repository.save();

        SavingsService service = new SavingsServiceImpl(repository);
        service.settleMaturedSavings();
        assertEquals(AccountStatus.INACTIVE, first.getStatus());
        assertEquals(AccountStatus.INACTIVE, second.getStatus());
        assertEquals(2, repository.getTransactions().size());
        assertNotEquals(repository.getTransactions().get(0).getTransactionId(),
                repository.getTransactions().get(1).getTransactionId());
        assertTrue(checking.getBalance().compareTo(new BigDecimal("3000000")) > 0);
        service.settleMaturedSavings();
        assertEquals(2, repository.getTransactions().size());
    }

    @Test
    void failedSaveRestoresLastGoodDiskState() throws Exception {
        JsonRepository repository = repository();
        AccountService accounts = new AccountServiceImpl(repository);
        TransactionService transactions = new TransactionServiceImpl(repository);
        CheckingAccount checking = accounts.openCheckingAccount("KH1");
        String saved = Files.readString(directory.resolve("bank-data.json"));

        // Trùng username khiến repository.save() từ chối ghi.
        repository.getUsers().add(customer("U3", "alice", "KH3"));
        assertThrows(IllegalStateException.class,
                () -> transactions.deposit(checking.getAccountNumber(), BigDecimal.ONE));
        assertEquals(saved, Files.readString(directory.resolve("bank-data.json")));
        assertEquals(2, repository.getUsers().size());
        assertEquals(0, repository.getTransactions().size());
        assertEquals(BigDecimal.ZERO,
                ((CheckingAccount) accounts.findByAccountNumber(checking.getAccountNumber())).getBalance());
    }
}
