package vn.edu.pxu.bank.ui;

import vn.edu.pxu.bank.di.DI;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.LoanService;
import vn.edu.pxu.bank.service.SavingsService;
import vn.edu.pxu.bank.service.TransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Khách hàng chỉ thao tác trên tài khoản của chính mình. */
final class CustomerPanel extends JPanel {
    private final String customerId;
    private final AccountService accounts;
    private final TransactionService transactions;
    private final SavingsService savings;
    private final LoanService loans;
    private final JTable accountTable = UiTools.table("Số tài khoản", "Loại", "Số dư / Dư nợ", "Trạng thái");
    private final JTable transactionTable = UiTools.table("Mã GD", "Thời điểm", "Loại", "Từ", "Đến", "Số tiền");
    private final JLabel info = new JLabel();

    CustomerPanel(DI di, String customerId, Runnable logout) {
        super(new BorderLayout(8, 8));
        this.customerId = customerId;
        this.accounts = di.get(AccountService.class);
        this.transactions = di.get(TransactionService.class);
        this.savings = di.get(SavingsService.class);
        this.loans = di.get(LoanService.class);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel heading = new JLabel("NBS Bank · Tài khoản khách hàng");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 21f));
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(event -> logout.run());
        JPanel top = new JPanel(new BorderLayout());
        top.add(heading, BorderLayout.WEST);
        top.add(logoutButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tài khoản của tôi", accountTab());
        tabs.addTab("Giao dịch", transactionTab());
        tabs.addTab("Thông tin cá nhân", profileTab());
        add(tabs, BorderLayout.CENTER);
        refresh();
    }

    private JPanel accountTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JScrollPane(accountTable), BorderLayout.CENTER);
        panel.add(UiTools.buttons(
                UiTools.button("Chuyển khoản", this, () -> {
                    String[] values = UiTools.prompt(this, "Chuyển khoản",
                            "Tài khoản nhận", "Số tiền (VND)");
                    if (values != null) {
                        CheckingAccount source = checking();
                        Transaction transaction = transactions.transfer(source.getAccountNumber(),
                                values[0], UiTools.money(values[1]));
                        UiTools.info(this, "Chuyển khoản thành công: " + transaction.getTransactionId());
                    }
                }, this::refresh),
                UiTools.button("Làm mới", this, this::refresh, null)
        ), BorderLayout.NORTH);

        accountTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                showAccountMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showAccountMenu(event);
            }
        });
        return panel;
    }

    private void showAccountMenu(MouseEvent event) {
        if (!event.isPopupTrigger()) return;
        int row = accountTable.rowAtPoint(event.getPoint());
        if (row < 0) return;

        // Chọn đúng dòng được bấm chuột phải, kể cả khi bảng đã sắp xếp lại.
        accountTable.setRowSelectionInterval(row, row);
        String number = accountTable.getValueAt(row, 0).toString();
        BankAccount account = owned(number, BankAccount.class);
        JPopupMenu menu = new JPopupMenu();

        if (account instanceof SavingsAccount saving) {
            menu.add(menuItem("Dự tính lãi tiết kiệm", true,
                    () -> showSavingsForecast(saving)));
            menu.add(menuItem("Tất toán tiết kiệm", saving.getStatus() == AccountStatus.ACTIVE, () -> {
                owned(number, SavingsAccount.class);
                savings.settleSavingsAccount(number);
                UiTools.info(this, "Tất toán sổ " + number + " thành công");
            }));
        } else if (account instanceof LoanAccount loan) {
            menu.add(menuItem("Chi tiết khoản vay", true,
                    () -> showLoanDetails(loan)));
            menu.add(menuItem("Trả nợ vay", loan.getStatus() == AccountStatus.ACTIVE, () -> {
                owned(number, LoanAccount.class);
                loans.repayLoanAccount(number);
                UiTools.info(this, "Đã trả hết khoản vay " + number);
            }));
        }

        if (menu.getComponentCount() > 0) {
            menu.show(accountTable, event.getX(), event.getY());
        }
    }

    private JMenuItem menuItem(String title, boolean enabled, Runnable action) {
        JMenuItem item = new JMenuItem(title);
        item.setEnabled(enabled);
        item.addActionListener(event -> {
            try {
                action.run();
                refresh();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thực hiện được", JOptionPane.ERROR_MESSAGE);
            }
        });
        return item;
    }

    private void showSavingsForecast(SavingsAccount saving) {
        if (saving.getStatus() != AccountStatus.ACTIVE) {
            UiTools.info(this, "Sổ " + saving.getAccountNumber()
                    + " đã tất toán. Hãy xem giao dịch SAVINGS_SETTLEMENT để biết lãi thực nhận.");
            return;
        }

        String[] values = UiTools.prompt(this, "Dự tính lãi tiết kiệm",
                "Ngày dự kiến (yyyy-MM-dd, trống = " + saving.getEndDate() + ")");
        if (values == null) return;
        LocalDate date = values[0].isBlank() ? saving.getEndDate() : UiTools.date(values[0]);
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Chỉ chọn ngày từ hôm nay trở đi");
        }

        BigDecimal interest = savings.calculateInterest(saving.getAccountNumber(), date);
        BigDecimal rate = date.isBefore(saving.getEndDate())
                ? saving.getEarlyWithdrawalRate() : saving.getAnnualRate();
        String ratePercent = rate.multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros().toPlainString().replace('.', ',');
        String note = date.isBefore(saving.getEndDate())
                ? "Tất toán trước hạn: áp dụng lãi suất không kỳ hạn."
                : "Đúng/sau hạn: chỉ tính lãi đến ngày đáo hạn.";

        UiTools.info(this, "Sổ: " + saving.getAccountNumber()
                + "\nNgày mở: " + saving.getStartDate()
                + "\nNgày đáo hạn: " + saving.getEndDate()
                + "\nNgày dự tính: " + date
                + "\nTiền gốc: " + UiTools.formatVnd(saving.getBalance())
                + "\nLãi suất áp dụng: " + ratePercent + "%/năm"
                + "\nLãi dự kiến: " + UiTools.formatVnd(interest)
                + "\nTổng dự kiến nhận: "
                + UiTools.formatVnd(saving.getBalance().add(interest))
                + "\n" + note);
    }

    private void showLoanDetails(LoanAccount loan) {
        if (loan.getStatus() != AccountStatus.ACTIVE) {
            UiTools.info(this, "Khoản vay " + loan.getAccountNumber()
                    + " đã trả hết. Hãy xem giao dịch LOAN_REPAYMENT để biết số tiền thực trả.");
            return;
        }

        UiTools.info(this, "Khoản vay: " + loan.getAccountNumber()
                + "\nNgày giải ngân: " + loan.getStartDate()
                + "\nNgày đáo hạn: " + loan.getEndDate()
                + "\nKỳ hạn: " + loan.getTermMonths() + " tháng"
                + "\nLãi suất cố định: "
                + loan.getAnnualRate().multiply(BigDecimal.valueOf(100))
                        .stripTrailingZeros().toPlainString() + "%/năm"
                + "\nDư nợ gốc: " + UiTools.formatVnd(loan.getBalance())
                + "\nLãi toàn kỳ: " + UiTools.formatVnd(loans.calculateInterest(loan.getAccountNumber()))
                + "\nTổng phải trả: " + UiTools.formatVnd(loans.calculateTotalDue(loan.getAccountNumber()))
                + "\nTổng nợ không thay đổi theo ngày trả.");
    }

    private JPanel transactionTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        panel.add(UiTools.buttons(UiTools.button("Làm mới giao dịch",
                this, this::refresh, null)), BorderLayout.NORTH);
        return panel;
    }

    private JPanel profileTab() {
        JPanel panel = new JPanel(new BorderLayout());
        info.setFont(info.getFont().deriveFont(16f));
        info.setVerticalAlignment(JLabel.TOP);
        info.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.add(info, BorderLayout.NORTH);
        return panel;
    }

    private Customer currentCustomer() {
        return accounts.findCustomer(customerId);
    }

    private CheckingAccount checking() {
        for (BankAccount account : accounts.findByCustomerId(customerId)) {
            if (account instanceof CheckingAccount checking) return checking;
        }
        throw new IllegalStateException("Chưa có tài khoản thanh toán");
    }

    private BankAccount owned(String number, Class<?> type) {
        BankAccount account = accounts.findByAccountNumber(number);
        if (!customerId.equals(account.getCustomerId()) || !type.isInstance(account)) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản thuộc sở hữu của bạn");
        }
        return account;
    }

    private void refresh() {
        Customer customer = currentCustomer();
        info.setText("<html><b>Khách hàng:</b> " + escape(customer.getFullName())
                + "<br><b>Mã khách:</b> " + escape(customer.getCustomerId())
                + "<br><b>Điện thoại:</b> " + escape(customer.getPhone())
                + "<br><b>Email:</b> " + escape(customer.getEmail())
                + "<br><b>Địa chỉ:</b> " + escape(customer.getAddress()) + "</html>");
        UiTools.fill(accountTable, model -> {
            for (BankAccount account : accounts.findByCustomerId(customerId)) {
                model.addRow(new Object[]{account.getAccountNumber(),
                        account.getAccountType(), UiTools.formatVnd(account.getBalance()),
                        account.getStatus()});
            }
        });
        UiTools.fill(transactionTable, model -> {
            for (Transaction transaction : transactions.findByCustomerId(customerId)) {
                model.addRow(new Object[]{
                        transaction.getTransactionId(), transaction.getOccurredAt(),
                        transaction.getType(), transaction.getFromAccountNumber(),
                        transaction.getToAccountNumber(), UiTools.formatVnd(transaction.getAmount())});
            }
        });
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
