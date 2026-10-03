package vn.edu.pxu.bank.model.user;

import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireText;

public abstract class User {
    private String userId;
    private String username;
    private String passwordHash;
    private UserStatus status;
    private boolean mustChangePassword;

    protected User(String userId, String username, String passwordHash) {
        this(userId, username, passwordHash, UserStatus.ACTIVE, false);
    }

    protected User(String userId, String username, String passwordHash,
                   UserStatus status, boolean mustChangePassword) {
        this.userId = requireText(userId, "Mã người dùng");
        this.username = requireText(username, "Tên đăng nhập");
        this.passwordHash = requireText(passwordHash, "Mật khẩu đã băm");
        setStatus(status);
        this.mustChangePassword = mustChangePassword;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    protected void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "Trạng thái người dùng không được null");
    }

    // Tham số là giá trị hash; việc băm và xác minh mật khẩu nhập vào thuộc tầng service.
    public void changePassword(String currentPasswordHash, String newPasswordHash) {
        if (!passwordHash.equals(currentPasswordHash)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        String validated = requireText(newPasswordHash, "Mật khẩu mới đã băm");
        if (passwordHash.equals(validated)) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ");
        }
        passwordHash = validated;
        mustChangePassword = false;
    }

    public void resetPassword(String temporaryPasswordHash) {
        passwordHash = requireText(temporaryPasswordHash, "Mật khẩu tạm đã băm");
        mustChangePassword = true;
    }

    public void deactivate() {
        setStatus(UserStatus.INACTIVE);
    }

    public abstract UserRole getRole();

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return userId.equals(((User) other).userId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), userId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{userId='" + userId + "', username='" + username
                + "', status=" + status + "}";
    }
}
