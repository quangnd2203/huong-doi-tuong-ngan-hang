package vn.edu.pxu.bank.model.account;

import java.math.BigDecimal;

import static vn.edu.pxu.bank.util.ValidationUtils.requireWholeVnd;

public class CheckingAccount extends BankAccount {
    public CheckingAccount(String accountNumber, String customerId) {
        this(accountNumber, customerId, BigDecimal.ZERO, AccountStatus.ACTIVE);
    }

    // Constructor đầy đủ phục vụ phục hồi trạng thái tài khoản đã lưu.
    public CheckingAccount(String accountNumber, String customerId, BigDecimal balance,
                           AccountStatus status) {
        super(accountNumber, customerId, balance, status);
        if (status == AccountStatus.INACTIVE && balance.signum() != 0) {
            throw new IllegalArgumentException("Tài khoản thanh toán đã đóng phải có số dư 0");
        }
    }

    public void deposit(BigDecimal amount) {
        requireActive();
        BigDecimal value = requireWholeVnd(amount, false);
        setBalance(getBalance().add(value));
    }

    public void withdraw(BigDecimal amount) {
        requireActive();
        BigDecimal value = requireWholeVnd(amount, false);
        if (getBalance().compareTo(value) < 0) {
            throw new IllegalStateException("Số dư không đủ");
        }
        setBalance(getBalance().subtract(value));
    }

    public void close() {
        requireActive();
        if (getBalance().signum() != 0) {
            throw new IllegalStateException("Chỉ được đóng tài khoản khi số dư bằng 0");
        }
        // Service còn phải kiểm tra sổ tiết kiệm và khoản vay ACTIVE của khách.
        setStatus(AccountStatus.INACTIVE);
    }

    public void reopen() {
        if (getStatus() != AccountStatus.INACTIVE) {
            throw new IllegalStateException("Tài khoản chưa đóng");
        }
        setStatus(AccountStatus.ACTIVE);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CHECKING;
    }
}
