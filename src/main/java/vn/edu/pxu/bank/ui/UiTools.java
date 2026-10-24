package vn.edu.pxu.bank.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/** Các thành phần nhập liệu và thông báo dùng chung cho Swing. */
final class UiTools {
    private UiTools() {
    }

    static String[] prompt(JComponent parent, String title, String... labels) {
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTextField[] inputs = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            form.add(new javax.swing.JLabel(labels[i]));
            inputs[i] = new JTextField(18);
            form.add(inputs[i]);
        }
        if (JOptionPane.showConfirmDialog(parent, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] values = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            values[i] = inputs[i].getText().trim();
        }
        return values;
    }

    static String[] promptWithDefaults(JComponent parent, String title,
                                       String[] labels, String[] defaults) {
        if (labels.length != defaults.length) {
            throw new IllegalArgumentException("Số nhãn và giá trị mặc định phải khớp");
        }
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTextField[] inputs = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            form.add(new javax.swing.JLabel(labels[i]));
            inputs[i] = new JTextField(defaults[i], 18);
            form.add(inputs[i]);
        }
        if (JOptionPane.showConfirmDialog(parent, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] values = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) values[i] = inputs[i].getText().trim();
        return values;
    }

    static BigDecimal money(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số tiền không hợp lệ (nhập số nguyên VND)", e);
        }
    }

    static String formatVnd(BigDecimal amount) {
        Objects.requireNonNull(amount, "Số tiền không được null");
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
                .format(amount) + " VND";
    }

    static LocalDate date(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày phải có định dạng yyyy-MM-dd", ex);
        }
    }

    static int term(String input, int... permitted) {
        try {
            int value = Integer.parseInt(input);
            for (int choice : permitted) {
                if (value == choice) return value;
            }
        } catch (NumberFormatException ignored) {
            // Thông báo thống nhất cho kỳ hạn không hợp lệ.
        }
        throw new IllegalArgumentException("Kỳ hạn không hợp lệ");
    }

    static JButton button(String text, JComponent parent, Runnable operation, Runnable refresh) {
        JButton button = new JButton(text);
        button.addActionListener(event -> {
            try {
                operation.run();
                if (refresh != null) refresh.run();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(),
                        "Không thực hiện được", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }

    static JPanel buttons(JButton... buttons) {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 8));
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    static void info(JComponent parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Hiển thị mật khẩu tạm trong ô có thể chọn và sao chép, thay vì JLabel. */
    static void showTemporaryPassword(JComponent parent, String details, char[] password) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        javax.swing.JLabel description = new javax.swing.JLabel(details);
        panel.add(description, BorderLayout.NORTH);

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line.add(new javax.swing.JLabel("Mật khẩu tạm:"));
        JTextField field = new JTextField(new String(password), 17);
        field.setEditable(false);
        line.add(field);
        JButton copy = new JButton("Sao chép");
        copy.addActionListener(event -> {
            field.selectAll();
            field.copy();
            copy.setText("Đã sao chép");
        });
        line.add(copy);
        panel.add(line, BorderLayout.CENTER);
        panel.add(new javax.swing.JLabel("Khách phải đổi mật khẩu khi đăng nhập."),
                BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, "Thông tin đăng nhập",
                JOptionPane.INFORMATION_MESSAGE);
        field.setText("");
    }

    static void report(JComponent parent, String title, String content) {
        JTextArea area = new JTextArea(content, 20, 54);
        area.setEditable(false);
        area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        JOptionPane.showMessageDialog(parent, new JScrollPane(area), title,
                JOptionPane.PLAIN_MESSAGE);
    }

    static JTable table(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(27);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    static void fill(JTable table, Consumer<DefaultTableModel> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        rows.accept(model);
    }
}
