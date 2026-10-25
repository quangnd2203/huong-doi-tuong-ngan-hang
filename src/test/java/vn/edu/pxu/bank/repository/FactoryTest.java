package vn.edu.pxu.bank.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.pxu.bank.factory.AccountServiceFactory;
import vn.edu.pxu.bank.factory.LoanServiceFactory;
import vn.edu.pxu.bank.factory.ReportServiceFactory;
import vn.edu.pxu.bank.factory.SavingsServiceFactory;
import vn.edu.pxu.bank.factory.TransactionServiceFactory;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.AccountServiceImpl;
import vn.edu.pxu.bank.service.LoanServiceImpl;
import vn.edu.pxu.bank.service.ReportService;
import vn.edu.pxu.bank.service.ReportServiceImpl;
import vn.edu.pxu.bank.service.SavingsServiceImpl;
import vn.edu.pxu.bank.service.TransactionService;
import vn.edu.pxu.bank.service.TransactionServiceImpl;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {
    @TempDir
    Path directory;

    @Test
    void factoriesCreateCorrectInterfacesWithoutMakingServicesSingleton() {
        AccountService first = AccountServiceFactory.create();
        AccountService second = AccountServiceFactory.create();

        assertInstanceOf(AccountServiceImpl.class, first);
        assertNotSame(first, second);
        assertInstanceOf(TransactionServiceImpl.class, TransactionServiceFactory.create());
        assertInstanceOf(SavingsServiceImpl.class, SavingsServiceFactory.create());
        assertInstanceOf(LoanServiceImpl.class, LoanServiceFactory.create());
        assertInstanceOf(ReportServiceImpl.class, ReportServiceFactory.create());
    }

    @Test
    void factoryCreatedServicesShareSingletonRepository() {
        JsonRepository repository = JsonRepository.getInstance();
        repository.load(directory.resolve("bank-data.json"));
        repository.getUsers().add(new Customer("U01", "alice", "hash", "KH01",
                "Alice", "0912345678", "alice@example.com", "Hà Nội"));
        repository.save();

        AccountService accounts = AccountServiceFactory.create();
        TransactionService transactions = TransactionServiceFactory.create();
        ReportService reports = ReportServiceFactory.create();

        CheckingAccount checking = accounts.openCheckingAccount("KH01");
        transactions.deposit(checking.getAccountNumber(), new BigDecimal("200000"));

        assertEquals(new BigDecimal("200000"), checking.getBalance());
        assertTrue(reports.generateAccountReport().contains("Tổng tiền gửi: 200000 VND"));
        assertTrue(reports.generateTodayTransactionReport().contains("DEPOSIT: 1 giao dịch"));
        repository.reload();
        assertEquals(new BigDecimal("200000"), ((CheckingAccount)
                accounts.findByAccountNumber(checking.getAccountNumber())).getBalance());
    }
}
