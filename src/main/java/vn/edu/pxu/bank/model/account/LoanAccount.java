package vn.edu.pxu.bank.model.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class LoanAccount extends BankAccount {
    private static final BigDecimal FIXED_ANNUAL_RATE = new BigDecimal("0.10");
    private BigDecimal annualRate;
    private LocalDate startDate;
    private LocalDate endDate;

    public LoanAccount(String accountNumber, String customerId, BigDecimal principal,
                       LocalDate startDate, int termMonths) {
        this(accountNumber, customerId, principal, FIXED_ANNUAL_RATE,
                startDate, endDateFor(startDate, termMonths), AccountStatus.ACTIVE);
    }

    // Constructor đầy đủ dùng cả cho khoản vay đã trả xong (balance = 0, INACTIVE).
    public LoanAccount(String accountNumber, String customerId, BigDecimal balance,
                       BigDecimal annualRate, LocalDate startDate, LocalDate endDate,
                       AccountStatus status) {
        super(accountNumber, customerId, balance, status);
        this.annualRate = Objects.requireNonNull(annualRate, "Lãi suất vay không được null");
        if (this.annualRate.compareTo(FIXED_ANNUAL_RATE) != 0) {
            throw new IllegalArgumentException("Lãi vay cố định phải là 10%/năm");
        }
        this.startDate = Objects.requireNonNull(startDate, "Ngày giải ngân không được null");
        this.endDate = Objects.requireNonNull(endDate, "Ngày kết thúc kỳ hạn không được null");
        getTermMonths(); // Kiểm tra kỳ hạn được phép.
        if (status == AccountStatus.ACTIVE && getBalance().signum() == 0) {
            throw new IllegalArgumentException("Khoản vay ACTIVE phải còn dư nợ gốc");
        }
        if (status == AccountStatus.INACTIVE && getBalance().signum() != 0) {
            throw new IllegalArgumentException("Khoản vay đã trả hết phải có dư nợ 0");
        }
    }

    private static LocalDate endDateFor(LocalDate startDate, int termMonths) {
        Objects.requireNonNull(startDate, "Ngày giải ngân không được null");
        if (termMonths != 6 && termMonths != 12) {
            throw new IllegalArgumentException("Kỳ hạn vay chỉ nhận 6 hoặc 12 tháng");
        }
        return startDate.plusMonths(termMonths);
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getTermMonths() {
        if (endDate.equals(startDate.plusMonths(6))) return 6;
        if (endDate.equals(startDate.plusMonths(12))) return 12;
        throw new IllegalArgumentException("Khoản vay phải có kỳ hạn 6 hoặc 12 tháng");
    }

    public BigDecimal calculateInterest() {
        return getBalance().multiply(annualRate)
                .multiply(BigDecimal.valueOf(getTermMonths()))
                .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalDue() {
        return getBalance().add(calculateInterest());
    }

    public BigDecimal repay() {
        requireActive();
        BigDecimal totalDue = calculateTotalDue();
        setBalance(BigDecimal.ZERO);
        setStatus(AccountStatus.INACTIVE);
        return totalDue;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.LOAN;
    }
}
