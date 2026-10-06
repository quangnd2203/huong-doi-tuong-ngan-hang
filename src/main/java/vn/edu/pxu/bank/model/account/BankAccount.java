package vn.edu.pxu.bank.model.account;

import java.math.BigDecimal;
import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireText;
import static vn.edu.pxu.bank.util.ValidationUtils.requireWholeVnd;

public abstract class BankAccount {
    private String accountNumber;
    private String customerId;
    private BigDecimal balance;
    private AccountStatus status;

    protected BankAccount(String accountNumber, String customerId, BigDecimal balance) {
        this(accountNumber, customerId, balance, AccountStatus.ACTIVE);
    }

    protected BankAccount(String accountNumber, String customerId, BigDecimal balance,
                          AccountStatus status) {
        this.accountNumber = requireText(accountNumber, "Số tài khoản");
        this.customerId = requireText(customerId, "Mã khách hàng");
        setBalance(balance);
        setStatus(status);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    protected void setBalance(BigDecimal balance) {
        this.balance = requireWholeVnd(balance, true);
    }

    protected void setStatus(AccountStatus status) {
        this.status = Objects.requireNonNull(status, "Trạng thái tài khoản không được null");
    }

    protected void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản không ở trạng thái ACTIVE");
        }
    }

    public abstract AccountType getAccountType();

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BankAccount account)) return false;
        return accountNumber.equals(account.accountNumber);
    }

    @Override
    public final int hashCode() {
        return accountNumber.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{accountNumber='" + accountNumber
                + "', customerId='" + customerId + "', balance=" + balance
                + ", status=" + status + "}";
    }
}
