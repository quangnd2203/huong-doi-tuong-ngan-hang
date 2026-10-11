package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.exception.InsufficientBalanceException;
import vn.edu.pxu.bank.exception.NotFoundException;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.model.user.UserStatus;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.math.BigDecimal;
import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireText;
import static vn.edu.pxu.bank.util.ValidationUtils.requireWholeVnd;

/** Các thao tác tìm dữ liệu/kiểm tra dùng chung cho đúng 5 service. */
final class ServiceSupport {
    private ServiceSupport() {
    }

    static Customer customer(JsonRepository repository, String customerId) {
        String id = requireText(customerId, "Mã khách hàng");
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer && customer.getCustomerId().equals(id)) {
                return customer;
            }
        }
        throw new NotFoundException("Không tìm thấy khách hàng: " + id);
    }

    static BankAccount account(JsonRepository repository, String number) {
        String id = requireText(number, "Số tài khoản");
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) {
                for (BankAccount account : customer.getAccounts()) {
                    if (account.getAccountNumber().equals(id)) {
                        return account;
                    }
                }
            }
        }
        throw new NotFoundException("Không tìm thấy tài khoản: " + id);
    }

    static CheckingAccount checking(Customer customer) {
        for (BankAccount account : customer.getAccounts()) {
            if (account instanceof CheckingAccount checking) {
                return checking;
            }
        }
        throw new NotFoundException("Khách hàng chưa có tài khoản thanh toán");
    }

    static CheckingAccount checking(JsonRepository repository, String number) {
        BankAccount account = account(repository, number);
        if (!(account instanceof CheckingAccount checking)) {
            throw new IllegalArgumentException("Đây không phải tài khoản thanh toán");
        }
        return checking;
    }

    static void requireActive(Customer customer) {
        if (customer.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Khách hàng đã ngừng hoạt động");
        }
    }

    static void requireActive(CheckingAccount checking) {
        if (checking.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản thanh toán đã đóng");
        }
    }

    static void requireEnough(CheckingAccount checking, BigDecimal amount) {
        if (checking.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư không đủ");
        }
    }

    static BigDecimal amount(BigDecimal amount) {
        return requireWholeVnd(amount, false);
    }

    static String nextAccountId(JsonRepository repository, String prefix) {
        long max = 0;
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) {
                for (BankAccount account : customer.getAccounts()) {
                    max = Math.max(max, numericSuffix(account.getAccountNumber(), prefix));
                }
            }
        }
        return prefix + String.format("%06d", max + 1);
    }

    static String nextTransactionId(JsonRepository repository) {
        long max = 0;
        for (Transaction transaction : repository.getTransactions()) {
            max = Math.max(max, numericSuffix(transaction.getTransactionId(), "TX"));
        }
        return "TX" + String.format("%06d", max + 1);
    }

    static long numericSuffix(String id, String prefix) {
        if (!id.startsWith(prefix)) return 0;
        String suffix = id.substring(prefix.length());
        if (!suffix.matches("[0-9]{1,15}")) return 0;
        return Long.parseLong(suffix);
    }

    /** Lưu một lần; nếu lưu thất bại, nạp lại trạng thái đã có trên ổ đĩa. */
    static void save(JsonRepository repository) {
        Objects.requireNonNull(repository);
        try {
            repository.save();
        } catch (RuntimeException ex) {
            try {
                repository.reload();
            } catch (RuntimeException reloadError) {
                ex.addSuppressed(reloadError);
            }
            throw ex;
        }
    }

    /** Tất toán sổ đã đến hạn trước khi bắt đầu một thao tác nghiệp vụ. */
    static void beforeOperation(JsonRepository repository) {
        new SavingsServiceImpl(repository).settleMaturedSavings();
    }
}
