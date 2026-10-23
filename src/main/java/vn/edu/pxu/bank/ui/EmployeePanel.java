package vn.edu.pxu.bank.ui;

import vn.edu.pxu.bank.di.DI;
import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.UserStatus;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.LoanService;
import vn.edu.pxu.bank.service.ReportService;
import vn.edu.pxu.bank.service.SavingsService;
import vn.edu.pxu.bank.service.TransactionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

/** Màn nhân viên: chọn đúng dòng khách/tài khoản rồi thao tác qua chuột phải. */
final class EmployeePanel extends JPanel {
    private final AccountService accounts;
    private final TransactionService transactions;
    private final SavingsService savings;
    private final LoanService loans;
    private final ReportService reports;
    private final JTable customerTable = UiTools.table("Mã KH", "Họ tên", "Tên đăng nhập", "Trạng thái");
    private final JTable accountTable = UiTools.table("Số tài khoản", "Mã KH", "Loại", "Số dư / Dư nợ", "Trạng thái");

    EmployeePanel(DI di, Runnable logout) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        accounts = di.get(AccountService.class);
        transactions = di.get(TransactionService.class);
        savings = di.get(SavingsService.class);
        loans = di.get(LoanService.class);
        reports = di.get(ReportService.class);

        JLabel heading = new JLabel("NBS Bank · Giao diện nhân viên");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 21f));
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(event -> logout.run());
        JPanel header = new JPanel(new BorderLayout());
        header.add(heading, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Khách hàng & tài khoản", accountsTab());
        tabs.addTab("Báo cáo", reportsTab());
        add(tabs, BorderLayout.CENTER);
        refresh();
    }

    private JPanel accountsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JSplitPane views = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titled("Khách hàng", new JScrollPane(customerTable)),
                titled("Tài khoản", new JScrollPane(accountTable)));
        views.setResizeWeight(0.35);
        panel.add(views, BorderLayout.CENTER);
        panel.add(UiTools.buttons(
                UiTools.button("Thêm khách hàng", this, this::addCustomer, this::refresh),
                UiTools.button("Làm mới", this, this::refresh, null)
        ), BorderLayout.NORTH);

        customerTable.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showCustomerMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { showCustomerMenu(e); }
        });
        accountTable.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showAccountMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { showAccountMenu(e); }
        });
        return panel;
    }

    private void showCustomerMenu(MouseEvent event) {
        if (!event.isPopupTrigger()) return;
        int row = customerTable.rowAtPoint(event.getPoint());
        if (row < 0) return;
        customerTable.setRowSelectionInterval(row, row);
        String customerId = customerTable.getValueAt(row, 0).toString();
        Customer customer = accounts.findCustomer(customerId);
        boolean active = customer.getStatus() == UserStatus.ACTIVE;
        boolean hasChecking = false;
        boolean activeChecking = false;
        boolean hasActiveLoan = false;
        boolean hasActiveAccounts = false;
        for (BankAccount account : customer.getAccounts()) {
            if (account.getStatus() == AccountStatus.ACTIVE) hasActiveAccounts = true;
            if (account instanceof CheckingAccount) {
                hasChecking = true;
                if (account.getStatus() == AccountStatus.ACTIVE) activeChecking = true;
            }
            if (account instanceof LoanAccount && account.getStatus() == AccountStatus.ACTIVE) {
                hasActiveLoan = true;
            }
        }

        JPopupMenu menu = new JPopupMenu();
        addAction(menu, "Xem chi tiết khách hàng", true, () -> showCustomerDetails(customerId));
        addAction(menu, "Sửa thông tin khách hàng", active, () -> editCustomer(customerId));
        menu.addSeparator();
        addAction(menu, "Mở tài khoản thanh toán", active && !hasChecking, () ->
                UiTools.info(this, "Đã mở " + accounts.openCheckingAccount(customerId).getAccountNumber()));
        addAction(menu, "Mở sổ tiết kiệm", active && activeChecking, () -> openSavings(customerId));
        addAction(menu, "Giải ngân khoản vay", active && activeChecking && !hasActiveLoan,
                () -> openLoan(customerId));
        menu.addSeparator();
        addAction(menu, "Đặt lại mật khẩu", active, () -> resetPassword(customerId));
        addAction(menu, "Ngừng hoạt động khách hàng", active && !hasActiveAccounts,
                () -> {
                    accounts.deactivateCustomer(customerId);
                    UiTools.info(this, "Đã ngừng hoạt động khách hàng " + customerId);
                });
        menu.show(customerTable, event.getX(), event.getY());
    }

    private void showAccountMenu(MouseEvent event) {
        if (!event.isPopupTrigger()) return;
        int row = accountTable.rowAtPoint(event.getPoint());
        if (row < 0) return;
        accountTable.setRowSelectionInterval(row, row);
        String number = accountTable.getValueAt(row, 0).toString();
        BankAccount account = accounts.findByAccountNumber(number);
        JPopupMenu menu = new JPopupMenu();

        if (account instanceof CheckingAccount checking) {
            Customer owner = accounts.findCustomer(checking.getCustomerId());
            boolean active = checking.getStatus() == AccountStatus.ACTIVE
                    && owner.getStatus() == UserStatus.ACTIVE;
            addAction(menu, "Xem chi tiết tài khoản", true, () -> showCheckingDetails(number));
            addAction(menu, "Xem lịch sử giao dịch", true, () -> showHistory(number));
            menu.addSeparator();
            addAction(menu, "Nộp tiền mặt", active, () -> cashTransaction(number, true));
            addAction(menu, "Rút tiền mặt", active, () -> cashTransaction(number, false));
            menu.addSeparator();
            addAction(menu, "Đóng tài khoản", active
                    && checking.getBalance().signum() == 0 && noOtherActiveAccounts(checking), () -> {
                accounts.closeCheckingAccount(number);
                UiTools.info(this, "Đã đóng tài khoản " + number);
            });
            addAction(menu, "Kích hoạt lại",
                    checking.getStatus() == AccountStatus.INACTIVE
                            && owner.getStatus() == UserStatus.ACTIVE, () -> {
                accounts.reopenCheckingAccount(number);
                UiTools.info(this, "Đã kích hoạt lại " + number);
            });
        } else if (account instanceof SavingsAccount saving) {
            addAction(menu, "Xem chi tiết sổ tiết kiệm", true, () -> showSavingsDetails(number));
            addAction(menu, "Dự tính lãi theo ngày",
                    saving.getStatus() == AccountStatus.ACTIVE, () -> showSavingsForecast(number));
            addAction(menu, "Tất toán tiết kiệm",
                    saving.getStatus() == AccountStatus.ACTIVE, () -> {
                savings.settleSavingsAccount(number);
                UiTools.info(this, "Đã tất toán sổ " + number);
            });
        } else if (account instanceof LoanAccount loan) {
            addAction(menu, "Xem chi tiết khoản vay", true, () -> showLoanDetails(number));
            addAction(menu, "Xem dư nợ và tổng phải trả", true, () -> showLoanDebt(number));
        }
        if (menu.getComponentCount() > 0) menu.show(accountTable, event.getX(), event.getY());
    }

    private void addAction(JPopupMenu menu, String label, boolean enabled, Runnable action) {
        JMenuItem item = new JMenuItem(label);
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
        menu.add(item);
    }

    private boolean noOtherActiveAccounts(CheckingAccount checking) {
        Customer owner = accounts.findCustomer(checking.getCustomerId());
        for (BankAccount account : owner.getAccounts()) {
            if (!(account instanceof CheckingAccount)
                    && account.getStatus() == AccountStatus.ACTIVE) return false;
        }
        return true;
    }

    private void addCustomer() {
        String[] values = UiTools.prompt(this, "Thêm khách hàng",
                "Tên đăng nhập (4-20, a-z/0-9/_)", "Họ tên", "Điện thoại", "Email", "Địa chỉ");
        if (values == null) return;
        char[] password = accounts.generateTemporaryPassword();
        try {
            Customer customer = accounts.createCustomer(
                    values[0], values[1], values[2], values[3], values[4], password);
            UiTools.showTemporaryPassword(this,
                    "Đã tạo khách hàng " + customer.getCustomerId()
                            + " | Tên đăng nhập: " + customer.getUsername(), password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void editCustomer(String customerId) {
        Customer customer = accounts.findCustomer(customerId);
        String[] values = UiTools.promptWithDefaults(this, "Sửa thông tin " + customerId,
                new String[]{"Họ tên", "Điện thoại", "Email", "Địa chỉ"},
                new String[]{customer.getFullName(), customer.getPhone(),
                        customer.getEmail(), customer.getAddress()});
        if (values == null) return;
        accounts.updateCustomer(customerId, values[0], values[1], values[2], values[3]);
        UiTools.info(this, "Đã cập nhật " + customerId);
    }

    private void resetPassword(String customerId) {
        char[] password = accounts.generateTemporaryPassword();
        try {
            accounts.resetCustomerPassword(customerId, password);
            UiTools.showTemporaryPassword(this,
                    "Đã đặt lại mật khẩu cho " + customerId, password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void showCustomerDetails(String customerId) {
        Customer c = accounts.findCustomer(customerId);
        UiTools.info(this, "Mã khách hàng: " + c.getCustomerId()
                + "\nHọ tên: " + c.getFullName()
                + "\nTên đăng nhập: " + c.getUsername()
                + "\nĐiện thoại: " + c.getPhone()
                + "\nEmail: " + c.getEmail()
                + "\nĐịa chỉ: " + c.getAddress()
                + "\nTrạng thái: " + c.getStatus()
                + "\nSố tài khoản: " + c.getAccounts().size());
    }

    private void openSavings(String customerId) {
        String[] values = UiTools.prompt(this, "Mở sổ cho " + customerId,
                "Số tiền (VND)", "Kỳ hạn (3 / 12 tháng)");
        if (values == null) return;
        SavingsAccount saving = savings.openSavingsAccount(customerId,
                UiTools.money(values[0]), UiTools.term(values[1], 3, 12));
        UiTools.info(this, "Đã mở sổ " + saving.getAccountNumber());
    }

    private void openLoan(String customerId) {
        String[] values = UiTools.prompt(this, "Giải ngân cho " + customerId,
                "Số tiền (VND)", "Kỳ hạn (6 / 12 tháng)");
        if (values == null) return;
        int months = UiTools.term(values[1], 6, 12);
        LoanAccount loan = loans.openLoanAccount(customerId, UiTools.money(values[0]),
                new BigDecimal("0.10"), LocalDate.now().plusMonths(months));
        UiTools.info(this, "Đã giải ngân khoản vay " + loan.getAccountNumber());
    }

    private void showCheckingDetails(String accountNumber) {
        CheckingAccount account = (CheckingAccount) accounts.findByAccountNumber(accountNumber);
        UiTools.info(this, "Tài khoản: " + account.getAccountNumber()
                + "\nMã khách hàng: " + account.getCustomerId()
                + "\nTrạng thái: " + account.getStatus()
                + "\nSố dư: " + UiTools.formatVnd(account.getBalance()));
    }

    private void showHistory(String accountNumber) {
        StringBuilder content = new StringBuilder("LỊCH SỬ TÀI KHOẢN " + accountNumber + "\n\n");
        int count = 0;
        for (Transaction transaction : transactions.findByAccountNumber(accountNumber)) {
            if (accountNumber.equals(transaction.getFromAccountNumber())
                    || accountNumber.equals(transaction.getToAccountNumber())) {
                content.append(transaction.getOccurredAt()).append(" | ")
                        .append(transaction.getType()).append(" | ")
                        .append(UiTools.formatVnd(transaction.getAmount()))
                        .append("\n").append(transaction.getFromAccountNumber())
                        .append(" → ").append(transaction.getToAccountNumber())
                        .append(" | ").append(transaction.getNote()).append("\n\n");
                count++;
            }
        }
        if (count == 0) content.append("Chưa có giao dịch.");
        UiTools.report(this, "Lịch sử giao dịch", content.toString());
    }

    private void showSavingsDetails(String number) {
        SavingsAccount saving = (SavingsAccount) accounts.findByAccountNumber(number);
        UiTools.info(this, "Sổ tiết kiệm: " + saving.getAccountNumber()
                + "\nKhách hàng: " + saving.getCustomerId()
                + "\nTrạng thái: " + saving.getStatus()
                + "\nKỳ hạn: " + saving.getTermMonths() + " tháng"
                + "\nNgày gửi: " + saving.getStartDate()
                + "\nNgày đáo hạn: " + saving.getEndDate()
                + "\nLãi suất kỳ hạn: " + rateText(saving.getAnnualRate())
                + "\nLãi trước hạn: " + rateText(saving.getEarlyWithdrawalRate())
                + "\nSố dư gốc còn lại: " + UiTools.formatVnd(saving.getBalance())
                + (saving.getStatus() == AccountStatus.INACTIVE
                        ? "\nĐã tất toán. Xem giao dịch để biết số tiền thực nhận." : ""));
    }

    private void showSavingsForecast(String number) {
        SavingsAccount saving = (SavingsAccount) accounts.findByAccountNumber(number);
        if (saving.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sổ đã tất toán");
        }
        String[] values = UiTools.prompt(this, "Dự tính lãi sổ " + number,
                "Ngày dự kiến (yyyy-MM-dd; để trống = " + saving.getEndDate() + ")");
        if (values == null) return;
        LocalDate date = values[0].isBlank() ? saving.getEndDate() : UiTools.date(values[0]);
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Chỉ chọn ngày từ hôm nay trở đi");
        }
        BigDecimal rate = date.isBefore(saving.getEndDate())
                ? saving.getEarlyWithdrawalRate() : saving.getAnnualRate();
        BigDecimal interest = savings.calculateInterest(number, date);
        UiTools.info(this, "Sổ: " + number + "\nNgày dự tính: " + date
                + "\nTiền gốc: " + UiTools.formatVnd(saving.getBalance())
                + "\nLãi suất áp dụng: " + rateText(rate)
                + "\nLãi dự tính: " + UiTools.formatVnd(interest)
                + "\nTổng dự tính nhận: "
                + UiTools.formatVnd(saving.getBalance().add(interest))
                + (date.isAfter(saving.getEndDate())
                        ? "\nChỉ tính lãi đến ngày đáo hạn." : ""));
    }

    private void showLoanDetails(String number) {
        LoanAccount loan = (LoanAccount) accounts.findByAccountNumber(number);
        UiTools.info(this, "Khoản vay: " + number
                + "\nKhách hàng: " + loan.getCustomerId()
                + "\nTrạng thái: " + loan.getStatus()
                + "\nNgày giải ngân: " + loan.getStartDate()
                + "\nNgày đến hạn: " + loan.getEndDate()
                + "\nKỳ hạn: " + loan.getTermMonths() + " tháng"
                + "\nLãi suất cố định: " + rateText(loan.getAnnualRate())
                + "\nDư nợ gốc: " + UiTools.formatVnd(loan.getBalance()));
    }

    private void showLoanDebt(String number) {
        LoanAccount loan = (LoanAccount) accounts.findByAccountNumber(number);
        if (loan.getStatus() != AccountStatus.ACTIVE) {
            UiTools.info(this, "Khoản vay " + number + " đã trả hết. Tổng dư nợ hiện tại: 0 VND.");
            return;
        }
        UiTools.info(this, "Khoản vay: " + number
                + "\nDư nợ gốc: " + UiTools.formatVnd(loan.getBalance())
                + "\nLãi toàn kỳ: " + UiTools.formatVnd(loans.calculateInterest(number))
                + "\nTổng phải trả: " + UiTools.formatVnd(loans.calculateTotalDue(number)));
    }

    private static String rateText(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros()
                .toPlainString().replace('.', ',') + "%/năm";
    }

    private void cashTransaction(String accountNumber, boolean deposit) {
        String title = (deposit ? "Nộp tiền vào " : "Rút tiền từ ") + accountNumber;
        String[] values = UiTools.prompt(this, title, "Số tiền (VND)");
        if (values == null) return;

        BigDecimal amount = UiTools.money(values[0]);
        Transaction transaction = deposit
                ? transactions.deposit(accountNumber, amount)
                : transactions.withdraw(accountNumber, amount);
        UiTools.info(this, "Giao dịch thành công: " + transaction.getTransactionId()
                + "\nTài khoản: " + accountNumber
                + "\nSố tiền: " + UiTools.formatVnd(amount));
    }

    private JPanel reportsTab() {
        JPanel panel = formPage("Báo cáo thống kê · dành cho nhân viên");
        panel.add(UiTools.buttons(
                UiTools.button("Báo cáo tài khoản", this, () ->
                        UiTools.report(this, "Báo cáo tài khoản", reports.generateAccountReport()), this::refresh),
                UiTools.button("Giao dịch hôm nay", this, () ->
                        UiTools.report(this, "Giao dịch hôm nay",
                                reports.generateTodayTransactionReport()), this::refresh),
                UiTools.button("Giao dịch theo ngày", this, () -> {
                    String[] values = UiTools.prompt(this, "Báo cáo giao dịch",
                            "Từ ngày (yyyy-MM-dd)", "Đến ngày (yyyy-MM-dd)");
                    if (values != null) {
                        UiTools.report(this, "Báo cáo giao dịch",
                                reports.generateTransactionReport(
                                        UiTools.date(values[0]), UiTools.date(values[1])));
                    }
                }, this::refresh)));
        return panel;
    }

    private JPanel formPage(String description) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 8));
        JLabel label = new JLabel(description);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(label);
        return panel;
    }

    private JPanel titled(String title, JScrollPane table) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder(title));
        wrapper.add(table);
        return wrapper;
    }

    private void refresh() {
        UiTools.fill(customerTable, model -> {
            for (Customer customer : accounts.findAllCustomers()) {
                model.addRow(new Object[]{customer.getCustomerId(), customer.getFullName(),
                        customer.getUsername(), customer.getStatus()});
            }
        });
        UiTools.fill(accountTable, model -> {
            for (Customer customer : accounts.findAllCustomers()) {
                for (BankAccount account : accounts.findByCustomerId(customer.getCustomerId())) {
                    model.addRow(new Object[]{account.getAccountNumber(),
                            customer.getCustomerId(), account.getAccountType(),
                            UiTools.formatVnd(account.getBalance()), account.getStatus()});
                }
            }
        });
    }
}
