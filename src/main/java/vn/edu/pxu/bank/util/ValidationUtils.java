package vn.edu.pxu.bank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Kiểm tra dữ liệu dùng chung; các quy tắc nghiệp vụ riêng vẫn nằm trong Model. */
public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được trống");
        }
        return value.trim();
    }

    /** Tên đăng nhập mới: 4-20 ký tự, bắt đầu bằng a-z, tiếp theo a-z/0-9/_. */
    public static String requireUsername(String username) {
        String value = requireText(username, "Tên đăng nhập");
        if (!value.matches("[a-z][a-z0-9_]{3,19}")) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập phải dài 4-20 ký tự, bắt đầu bằng chữ thường a-z; "
                            + "chỉ chứa a-z, 0-9 và _");
        }
        return value;
    }

    public static String requireEmail(String email) {
        String value = requireText(email, "Email");
        if (!value.contains("@") || value.startsWith("@") || value.endsWith("@")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        return value;
    }

    /** Chỉ nhận số nguyên đồng VND; không tự làm tròn tiền nhập. */
    public static BigDecimal requireWholeVnd(BigDecimal amount, boolean allowZero) {
        Objects.requireNonNull(amount, "Số tiền không được null");
        if (amount.signum() < 0 || (!allowZero && amount.signum() == 0)) {
            throw new IllegalArgumentException("Số tiền phải " + (allowZero ? "không âm" : "dương"));
        }
        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Số tiền phải là số nguyên VND", ex);
        }
    }

    public static BigDecimal requireRate(BigDecimal rate) {
        Objects.requireNonNull(rate, "Lãi suất không được null");
        if (rate.signum() <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Lãi suất phải lớn hơn 0 và không vượt quá 1");
        }
        return rate;
    }
}
