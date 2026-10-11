package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.LoanAccount;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanService {
    LoanAccount openLoanAccount(String customerId, BigDecimal amount, BigDecimal annualRate, LocalDate endDate);
    void repayLoanAccount(String accountNumber);
    BigDecimal calculateInterest(String accountNumber);
    BigDecimal calculateTotalDue(String accountNumber);
}
