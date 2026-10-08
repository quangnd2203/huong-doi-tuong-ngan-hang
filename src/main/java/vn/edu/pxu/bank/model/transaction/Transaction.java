package vn.edu.pxu.bank.model.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireText;
import static vn.edu.pxu.bank.util.ValidationUtils.requireWholeVnd;

public final class Transaction {
    private final String transactionId;
    private final BigDecimal amount;
    private final String fromAccountNumber;
    private final String toAccountNumber;
    private final TransactionType type;
    private final LocalDateTime occurredAt;
    private final String note;

    public Transaction(String transactionId, BigDecimal amount,
                       String fromAccountNumber, String toAccountNumber,
                       TransactionType type, String note) {
        this(transactionId, amount, fromAccountNumber, toAccountNumber,
                type, LocalDateTime.now(), note);
    }

    public Transaction(String transactionId, BigDecimal amount,
                       String fromAccountNumber, String toAccountNumber,
                       TransactionType type, LocalDateTime occurredAt, String note) {
        this.transactionId = requireText(transactionId, "Mã giao dịch");
        this.amount = requireWholeVnd(amount, false);
        this.fromAccountNumber = optionalAccount(fromAccountNumber);
        this.toAccountNumber = optionalAccount(toAccountNumber);
        this.type = Objects.requireNonNull(type, "Loại giao dịch không được null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Thời điểm giao dịch không được null");
        this.note = note == null ? "" : note.trim();
        validateEndpoints();
    }

    private static String optionalAccount(String accountNumber) {
        return accountNumber == null || accountNumber.isBlank()
                ? null : accountNumber.trim();
    }

    private void validateEndpoints() {
        if (type == TransactionType.DEPOSIT) {
            if (fromAccountNumber != null || toAccountNumber == null) {
                throw new IllegalArgumentException("Nộp tiền mặt chỉ có tài khoản nhận");
            }
        } else if (type == TransactionType.WITHDRAWAL) {
            if (fromAccountNumber == null || toAccountNumber != null) {
                throw new IllegalArgumentException("Rút tiền mặt chỉ có tài khoản nguồn");
            }
        } else if (fromAccountNumber == null || toAccountNumber == null) {
            throw new IllegalArgumentException("Giao dịch này phải có tài khoản nguồn và đích");
        }
        if (type == TransactionType.TRANSFER && fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Không thể chuyển khoản tới cùng tài khoản");
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Transaction transaction)) return false;
        return transactionId.equals(transaction.transactionId);
    }

    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{id='" + transactionId + "', type=" + type
                + ", amount=" + amount + ", from='" + fromAccountNumber
                + "', to='" + toAccountNumber + "', occurredAt=" + occurredAt + "}";
    }
}
