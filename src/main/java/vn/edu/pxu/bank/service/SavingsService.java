package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.SavingsAccount;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface SavingsService {
    SavingsAccount openSavingsAccount(String customerId, BigDecimal amount, int termMonths);
    void settleSavingsAccount(String accountNumber);
    void settleMaturedSavings();
    BigDecimal calculateInterest(String accountNumber, LocalDate date);
}
