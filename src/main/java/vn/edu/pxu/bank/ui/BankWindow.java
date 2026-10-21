package vn.edu.pxu.bank.ui;

import vn.edu.pxu.bank.di.DI;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.service.AccountService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/** Cửa sổ Swing chỉ dùng AccountService để đăng nhập/đổi mật khẩu. */
public final class BankWindow extends JFrame {
    private final DI di;
    private final AccountService accounts;

    public BankWindow(DI di) {
        super("NBS Bank | Quản lý ngân hàng");
        this.di = di;
        this.accounts = di.get(AccountService.class);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1060, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new java.awt.Dimension(800, 560));
        showLogin();
    }

    /** Quản lý vòng đời cửa sổ ở tầng UI, tránh Maven đóng classloader trước Swing. */
    public static void launch(DI di) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Ứng dụng Swing cần desktop có giao diện đồ họa");
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            CountDownLatch closed = new CountDownLatch(1);
            SwingUtilities.invokeAndWait(() -> {
                BankWindow window = new BankWindow(di);
                window.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosed(WindowEvent event) {
                        closed.countDown();
                    }
                });
                window.setVisible(true);
            });
            closed.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ứng dụng bị ngắt khi đang chạy", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Không khởi động được cửa sổ ngân hàng", ex);
        }
    }

    private void showLogin() {
        JPanel root = new JPanel(new java.awt.GridBagLayout());
        JPanel login = new JPanel(new BorderLayout(0, 16));
        login.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(28, 36, 28, 36)));

        JLabel title = new JLabel("NBS BANK");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 27f));
        login.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 12, 12));
        JTextField username = new JTextField(20);
        JPasswordField password = new JPasswordField(20);
        form.add(new JLabel("Tên đăng nhập"));
        form.add(username);
        form.add(new JLabel("Mật khẩu"));
        form.add(password);
        login.add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Đăng nhập");
        loginButton.addActionListener(event -> signIn(username.getText().trim(), password.getPassword()));
        password.addActionListener(event -> loginButton.doClick());

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.add(loginButton, BorderLayout.NORTH);
        JLabel demo = new JLabel(DemoData.LOGIN_HINTS);
        footer.add(demo, BorderLayout.SOUTH);
        login.add(footer, BorderLayout.SOUTH);
        root.add(login);

        setContentPane(root);
        revalidate();
        repaint();
    }

    private void signIn(String username, char[] password) {
        try {
            User user = accounts.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Sai tên đăng nhập, mật khẩu hoặc tài khoản không hoạt động.",
                        "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (user.isMustChangePassword() && !changeTemporaryPassword(user, password)) {
                return;
            }
            if (user instanceof Employee) {
                setContentPane(new EmployeePanel(di, this::showLogin));
            } else if (user instanceof Customer customer) {
                setContentPane(new CustomerPanel(di, customer.getCustomerId(), this::showLogin));
            }
            revalidate();
            repaint();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private boolean changeTemporaryPassword(User user, char[] currentPassword) {
        JPasswordField newPassword = new JPasswordField(20);
        if (JOptionPane.showConfirmDialog(this, newPassword, "Đổi mật khẩu tạm để tiếp tục",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }
        char[] raw = newPassword.getPassword();
        try {
            accounts.changePassword(user.getUserId(), currentPassword, raw);
            return true;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đổi mật khẩu",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            Arrays.fill(raw, '\0');
        }
    }
}
