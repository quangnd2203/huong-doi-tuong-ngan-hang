package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.transaction.Transaction;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    List<Transaction> findByAccountNumber(String accountNumber);
    List<Transaction> findByCustomerId(String customerId);
    Transaction deposit(String accountNumber, BigDecimal amount);
    Transaction withdraw(String accountNumber, BigDecimal amount);
    Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount);
}
