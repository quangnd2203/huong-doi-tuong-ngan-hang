package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TransactionServiceImpl implements TransactionService {
    private final JsonRepository repository;

    public TransactionServiceImpl(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        String number = ServiceSupport.account(repository, accountNumber).getAccountNumber();
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : repository.getTransactions()) {
            if (number.equals(transaction.getFromAccountNumber())
                    || number.equals(transaction.getToAccountNumber())) {
                result.add(transaction);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public List<Transaction> findByCustomerId(String customerId) {
        Customer customer = ServiceSupport.customer(repository, customerId);
        Set<String> accountNumbers = new HashSet<>();
        for (BankAccount account : customer.getAccounts()) {
            accountNumbers.add(account.getAccountNumber());
        }
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : repository.getTransactions()) {
            if (accountNumbers.contains(transaction.getFromAccountNumber())
                    || accountNumbers.contains(transaction.getToAccountNumber())) {
                result.add(transaction);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        ServiceSupport.beforeOperation(repository);
        CheckingAccount checking = activeChecking(accountNumber);
        BigDecimal value = ServiceSupport.amount(amount);
        Transaction transaction = new Transaction(ServiceSupport.nextTransactionId(repository),
                value, null, checking.getAccountNumber(), TransactionType.DEPOSIT, "Nộp tiền mặt");
        checking.deposit(value);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
        return transaction;
    }

    @Override
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        ServiceSupport.beforeOperation(repository);
        CheckingAccount checking = activeChecking(accountNumber);
        BigDecimal value = ServiceSupport.amount(amount);
        ServiceSupport.requireEnough(checking, value);
        Transaction transaction = new Transaction(ServiceSupport.nextTransactionId(repository),
                value, checking.getAccountNumber(), null, TransactionType.WITHDRAWAL, "Rút tiền mặt");
        checking.withdraw(value);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
        return transaction;
    }

    @Override
    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        ServiceSupport.beforeOperation(repository);
        CheckingAccount source = activeChecking(fromAccountNumber);
        CheckingAccount target = activeChecking(toAccountNumber);
        if (source.getAccountNumber().equals(target.getAccountNumber())) {
            throw new IllegalArgumentException("Không được chuyển tiền vào chính tài khoản nguồn");
        }
        BigDecimal value = ServiceSupport.amount(amount);
        ServiceSupport.requireEnough(source, value);
        Transaction transaction = new Transaction(ServiceSupport.nextTransactionId(repository),
                value, source.getAccountNumber(), target.getAccountNumber(),
                TransactionType.TRANSFER, "Chuyển khoản");
        source.withdraw(value);
        target.deposit(value);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
        return transaction;
    }

    private CheckingAccount activeChecking(String accountNumber) {
        CheckingAccount checking = ServiceSupport.checking(repository, accountNumber);
        ServiceSupport.requireActive(ServiceSupport.customer(repository, checking.getCustomerId()));
        ServiceSupport.requireActive(checking);
        return checking;
    }
}
