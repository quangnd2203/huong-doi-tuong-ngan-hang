package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class LoanServiceImpl implements LoanService {
    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.10");
    private final JsonRepository repository;

    public LoanServiceImpl(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LoanAccount openLoanAccount(String customerId, BigDecimal amount,
                                       BigDecimal annualRate, LocalDate endDate) {
        ServiceSupport.beforeOperation(repository);
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        CheckingAccount checking = ServiceSupport.checking(customer);
        ServiceSupport.requireActive(checking);
        BigDecimal principal = ServiceSupport.amount(amount);
        if (annualRate == null || annualRate.compareTo(ANNUAL_RATE) != 0) {
            throw new IllegalArgumentException("Lãi vay cố định phải là 10%/năm");
        }

        LocalDate startDate = LocalDate.now();
        Objects.requireNonNull(endDate, "Ngày đến hạn không được null");
        int months;
        if (endDate.equals(startDate.plusMonths(6))) {
            months = 6;
        } else if (endDate.equals(startDate.plusMonths(12))) {
            months = 12;
        } else {
            throw new IllegalArgumentException("Khoản vay chỉ có kỳ hạn 6 hoặc 12 tháng");
        }

        LoanAccount loan = new LoanAccount(ServiceSupport.nextAccountId(repository, "LN"),
                customer.getCustomerId(), principal, startDate, months);
        Transaction transaction = new Transaction(ServiceSupport.nextTransactionId(repository),
                principal, loan.getAccountNumber(), checking.getAccountNumber(),
                TransactionType.LOAN_DISBURSEMENT, "Giải ngân khoản vay " + months + " tháng");

        customer.addAccount(loan); // Chặn nếu đang có khoản vay ACTIVE.
        checking.deposit(principal);
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
        return loan;
    }

    @Override
    public BigDecimal calculateInterest(String accountNumber) {
        return findLoan(accountNumber).calculateInterest();
    }

    @Override
    public BigDecimal calculateTotalDue(String accountNumber) {
        return findLoan(accountNumber).calculateTotalDue();
    }

    private LoanAccount findLoan(String accountNumber) {
        BankAccount account = ServiceSupport.account(repository, accountNumber);
        if (!(account instanceof LoanAccount loan)) {
            throw new IllegalArgumentException("Đây không phải tài khoản vay");
        }
        return loan;
    }

    @Override
    public void repayLoanAccount(String accountNumber) {
        ServiceSupport.beforeOperation(repository);
        BankAccount account = ServiceSupport.account(repository, accountNumber);
        if (!(account instanceof LoanAccount loan)) {
            throw new IllegalArgumentException("Đây không phải tài khoản vay");
        }
        if (loan.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Khoản vay đã trả hết");
        }
        Customer customer = ServiceSupport.customer(repository, loan.getCustomerId());
        ServiceSupport.requireActive(customer);
        CheckingAccount checking = ServiceSupport.checking(customer);
        ServiceSupport.requireActive(checking);

        BigDecimal principal = loan.getBalance();
        BigDecimal interest = loan.calculateInterest();
        BigDecimal total = loan.calculateTotalDue();
        ServiceSupport.requireEnough(checking, total);
        Transaction transaction = new Transaction(ServiceSupport.nextTransactionId(repository),
                total, checking.getAccountNumber(), loan.getAccountNumber(),
                TransactionType.LOAN_REPAYMENT,
                "Gốc: " + principal.toPlainString() + " VND; lãi: "
                        + interest.toPlainString() + " VND");

        checking.withdraw(total);
        loan.repay();
        repository.getTransactions().add(transaction);
        ServiceSupport.save(repository);
    }
}
