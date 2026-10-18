package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SavingsServiceImpl implements SavingsService {
    private final JsonRepository repository;

    public SavingsServiceImpl(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public SavingsAccount openSavingsAccount(String customerId, BigDecimal amount, int termMonths) {
        ServiceSupport.beforeOperation(repository);
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        CheckingAccount checking = ServiceSupport.checking(customer);
        ServiceSupport.requireActive(checking);

        BigDecimal principal = ServiceSupport.amount(amount);
        ServiceSupport.requireEnough(checking, principal);
        SavingsAccount savings = new SavingsAccount(
                ServiceSupport.nextAccountId(repository, "ST"),
                customer.getCustomerId(), principal, termMonths, LocalDate.now());
        Transaction transaction = new Transaction(
                ServiceSupport.nextTransactionId(repository), principal,
                checking.getAccountNumber(), savings.getAccountNumber(),
                TransactionType.SAVINGS_OPEN, "Mở sổ tiết kiệm " + termMonths + " tháng");

        checking.withdraw(principal);
        customer.addAccount(savings);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
        return savings;
    }

    @Override
    public void settleSavingsAccount(String accountNumber) {
        ServiceSupport.beforeOperation(repository);
        BankAccount account = ServiceSupport.account(repository, accountNumber);
        if (!(account instanceof SavingsAccount savings)) {
            throw new IllegalArgumentException("Đây không phải sổ tiết kiệm");
        }
        if (savings.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sổ tiết kiệm đã tất toán");
        }
        Customer customer = ServiceSupport.customer(repository, savings.getCustomerId());
        ServiceSupport.requireActive(customer);
        CheckingAccount checking = ServiceSupport.checking(customer);
        ServiceSupport.requireActive(checking);

        LocalDate today = LocalDate.now();
        BigDecimal principal = savings.getBalance();
        BigDecimal interest = savings.calculateInterest(today);
        BigDecimal payout = principal.add(interest);
        Transaction transaction = settlementTransaction(savings, checking, principal, interest);

        savings.settle(today);
        checking.deposit(payout);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
    }

    @Override
    public BigDecimal calculateInterest(String accountNumber, LocalDate date) {
        BankAccount account = ServiceSupport.account(repository, accountNumber);
        if (!(account instanceof SavingsAccount saving)) {
            throw new IllegalArgumentException("Đây không phải sổ tiết kiệm");
        }
        if (saving.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sổ tiết kiệm đã tất toán");
        }
        return saving.calculateInterest(date);
    }

    @Override
    public void settleMaturedSavings() {
        LocalDate today = LocalDate.now();
        List<DueSavings> due = new ArrayList<>();
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) {
                for (BankAccount account : customer.getAccounts()) {
                    if (account instanceof SavingsAccount savings
                            && savings.getStatus() == AccountStatus.ACTIVE
                            && !today.isBefore(savings.getEndDate())) {
                        ServiceSupport.requireActive(customer);
                        CheckingAccount checking = ServiceSupport.checking(customer);
                        ServiceSupport.requireActive(checking);
                        due.add(new DueSavings(savings, checking,
                                savings.getBalance(), savings.calculateInterest(today)));
                    }
                }
            }
        }
        // Tính và kiểm tra toàn bộ trước khi thay đổi bất kỳ số dư nào.
        if (due.isEmpty()) return;

        for (DueSavings item : due) {
            Transaction transaction = settlementTransaction(item.savings, item.checking,
                    item.principal, item.interest);
            BigDecimal payout = item.savings.settle(today);
            item.checking.deposit(payout);
            repository.getTransactions().add(transaction);
        }
        ServiceSupport.save(repository);
    }

    private Transaction settlementTransaction(SavingsAccount savings, CheckingAccount checking,
                                               BigDecimal principal, BigDecimal interest) {
        return new Transaction(ServiceSupport.nextTransactionId(repository),
                principal.add(interest), savings.getAccountNumber(), checking.getAccountNumber(),
                TransactionType.SAVINGS_SETTLEMENT,
                "Gốc: " + principal.toPlainString() + " VND; lãi: "
                        + interest.toPlainString() + " VND");
    }

    private record DueSavings(SavingsAccount savings, CheckingAccount checking,
                              BigDecimal principal, BigDecimal interest) {
    }
}
