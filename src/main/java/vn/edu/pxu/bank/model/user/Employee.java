package vn.edu.pxu.bank.model.user;

public class Employee extends User {
    public Employee(String userId, String username, String passwordHash) {
        this(userId, username, passwordHash, UserStatus.ACTIVE, false);
    }

    public Employee(String userId, String username, String passwordHash,
                    UserStatus status, boolean mustChangePassword) {
        super(userId, username, passwordHash, status, mustChangePassword);
    }

    @Override
    public UserRole getRole() {
        return UserRole.EMPLOYEE;
    }
}
