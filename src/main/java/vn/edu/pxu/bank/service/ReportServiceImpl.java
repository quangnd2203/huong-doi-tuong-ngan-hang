package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.AccountType;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.transaction.TransactionType;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ReportServiceImpl implements ReportService {
    private final JsonRepository repository;

    public ReportServiceImpl(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public String generateAccountReport() {
        ServiceSupport.beforeOperation(repository);
        Map<AccountType, Map<AccountStatus, Integer>> counts = new EnumMap<>(AccountType.class);
        Map<AccountType, BigDecimal> totals = new EnumMap<>(AccountType.class);
        for (AccountType type : AccountType.values()) {
            counts.put(type, new EnumMap<>(AccountStatus.class));
            totals.put(type, BigDecimal.ZERO);
            for (AccountStatus status : AccountStatus.values()) {
                counts.get(type).put(status, 0);
            }
        }

        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) {
                for (BankAccount account : customer.getAccounts()) {
                    AccountType type = account.getAccountType();
                    AccountStatus status = account.getStatus();
                    counts.get(type).put(status, counts.get(type).get(status) + 1);
                    totals.put(type, totals.get(type).add(account.getBalance()));
                }
            }
        }

        StringBuilder report = new StringBuilder("BÁO CÁO TÀI KHOẢN\n");
        for (AccountType type : AccountType.values()) {
            report.append(type).append(": ACTIVE=")
                    .append(counts.get(type).get(AccountStatus.ACTIVE))
                    .append(", INACTIVE=")
                    .append(counts.get(type).get(AccountStatus.INACTIVE))
                    .append(", tổng số dư=")
                    .append(totals.get(type).toPlainString()).append(" VND\n");
        }
        BigDecimal deposits = totals.get(AccountType.CHECKING).add(totals.get(AccountType.SAVINGS));
        report.append("Tổng tiền gửi: ").append(deposits.toPlainString()).append(" VND\n")
                .append("Tổng dư nợ gốc: ")
                .append(totals.get(AccountType.LOAN).toPlainString()).append(" VND");
        return report.toString();
    }

    @Override
    public String generateTransactionReport(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "Ngày bắt đầu không được null");
        Objects.requireNonNull(toDate, "Ngày kết thúc không được null");
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        ServiceSupport.beforeOperation(repository);

        Map<TransactionType, Integer> counts = new EnumMap<>(TransactionType.class);
        Map<TransactionType, BigDecimal> totals = new EnumMap<>(TransactionType.class);
        for (TransactionType type : TransactionType.values()) {
            counts.put(type, 0);
            totals.put(type, BigDecimal.ZERO);
        }

        for (Transaction transaction : repository.getTransactions()) {
            LocalDate date = transaction.getOccurredAt().toLocalDate();
            if (!date.isBefore(fromDate) && !date.isAfter(toDate)) {
                TransactionType type = transaction.getType();
                counts.put(type, counts.get(type) + 1);
                totals.put(type, totals.get(type).add(transaction.getAmount()));
            }
        }

        StringBuilder report = new StringBuilder("BÁO CÁO GIAO DỊCH ")
                .append(fromDate).append(" - ").append(toDate).append("\n");
        for (TransactionType type : TransactionType.values()) {
            report.append(type).append(": ")
                    .append(counts.get(type)).append(" giao dịch, ")
                    .append(totals.get(type).toPlainString()).append(" VND\n");
        }
        return report.toString();
    }
}
