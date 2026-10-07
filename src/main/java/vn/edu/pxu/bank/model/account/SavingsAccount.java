package vn.edu.pxu.bank.model.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireRate;

public class SavingsAccount extends BankAccount {
    private static final BigDecimal MIN_OPENING_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal RATE_3_MONTHS = new BigDecimal("0.03");
    private static final BigDecimal RATE_12_MONTHS = new BigDecimal("0.06");
    private static final BigDecimal DEFAULT_EARLY_RATE = new BigDecimal("0.002");

    private int termMonths;
    private BigDecimal annualRate;
    private BigDecimal earlyWithdrawalRate;
    private LocalDate startDate;
    private LocalDate endDate;

    public SavingsAccount(String accountNumber, String customerId, BigDecimal amount,
                          int termMonths, LocalDate startDate) {
        this(accountNumber, customerId, amount, termMonths, rateFor(termMonths),
                DEFAULT_EARLY_RATE, startDate,
                Objects.requireNonNull(startDate, "Ngày bắt đầu không được null").plusMonths(termMonths),
                AccountStatus.ACTIVE);
    }

    // Lãi suất và ngày được lưu tại thời điểm mở; INACTIVE giữ lịch sử với balance = 0.
    public SavingsAccount(String accountNumber, String customerId, BigDecimal balance,
                          int termMonths, BigDecimal annualRate, BigDecimal earlyWithdrawalRate,
                          LocalDate startDate, LocalDate endDate, AccountStatus status) {
        super(accountNumber, customerId, balance, status);
        rateFor(termMonths); // Kiểm tra chỉ cho kỳ hạn 3 hoặc 12 tháng.
        this.termMonths = termMonths;
        this.annualRate = requireRate(annualRate);
        this.earlyWithdrawalRate = requireRate(earlyWithdrawalRate);
        this.startDate = Objects.requireNonNull(startDate, "Ngày bắt đầu không được null");
        this.endDate = Objects.requireNonNull(endDate, "Ngày đến hạn không được null");
        if (!this.endDate.equals(this.startDate.plusMonths(termMonths))) {
            throw new IllegalArgumentException("Ngày đến hạn không khớp kỳ hạn");
        }
        if (status == AccountStatus.ACTIVE && getBalance().compareTo(MIN_OPENING_AMOUNT) < 0) {
            throw new IllegalArgumentException("Sổ tiết kiệm ACTIVE phải có ít nhất 1.000.000 VND");
        }
        if (status == AccountStatus.INACTIVE && getBalance().signum() != 0) {
            throw new IllegalArgumentException("Sổ đã tất toán phải có số dư 0");
        }
    }

    private static BigDecimal rateFor(int months) {
        return switch (months) {
            case 3 -> RATE_3_MONTHS;
            case 12 -> RATE_12_MONTHS;
            default -> throw new IllegalArgumentException("Kỳ hạn tiết kiệm chỉ nhận 3 hoặc 12 tháng");
        };
    }

    public int getTermMonths() {
        return termMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public BigDecimal getEarlyWithdrawalRate() {
        return earlyWithdrawalRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal calculateInterest(LocalDate onDate) {
        Objects.requireNonNull(onDate, "Ngày tính lãi không được null");
        if (onDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày tính lãi không thể trước ngày mở sổ");
        }
        LocalDate until = onDate.isAfter(endDate) ? endDate : onDate;
        long days = ChronoUnit.DAYS.between(startDate, until);
        BigDecimal rate = onDate.isBefore(endDate) ? earlyWithdrawalRate : annualRate;
        return getBalance().multiply(rate).multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(365), 0, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateInterest() {
        return calculateInterest(LocalDate.now());
    }

    // Trả về toàn bộ tiền phải hoàn. Service chịu trách nhiệm ghi giao dịch và lưu dữ liệu.
    public BigDecimal settle(LocalDate onDate) {
        requireActive();
        BigDecimal payout = getBalance().add(calculateInterest(onDate));
        setBalance(BigDecimal.ZERO);
        setStatus(AccountStatus.INACTIVE);
        return payout;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }
}
