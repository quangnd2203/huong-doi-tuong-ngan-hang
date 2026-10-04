package vn.edu.pxu.bank.model.user;

import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static vn.edu.pxu.bank.util.ValidationUtils.requireEmail;
import static vn.edu.pxu.bank.util.ValidationUtils.requireText;

public class Customer extends User {
    private String customerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private List<BankAccount> accounts;

    public Customer(String userId, String username, String passwordHash,
                    String customerId, String fullName, String phone, String email, String address) {
        this(userId, username, passwordHash, UserStatus.ACTIVE, false,
                customerId, fullName, phone, email, address);
    }

    public Customer(String userId, String username, String passwordHash,
                    UserStatus status, boolean mustChangePassword,
                    String customerId, String fullName, String phone, String email, String address) {
        super(userId, username, passwordHash, status, mustChangePassword);
        this.customerId = requireText(customerId, "Mã khách hàng");
        changeName(fullName);
        updateContactInfo(phone, email, address);
        this.accounts = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public List<BankAccount> getAccounts() {
        return List.copyOf(accounts);
    }

    public void changeName(String fullName) {
        this.fullName = requireText(fullName, "Họ tên");
    }

    public void setPhone(String phone) {
        this.phone = requireText(phone, "Số điện thoại");
    }

    public void setEmail(String email) {
        this.email = requireEmail(email);
    }

    public void setAddress(String address) {
        this.address = requireText(address, "Địa chỉ");
    }

    public void updateContactInfo(String phone, String email, String address) {
        // Kiểm tra toàn bộ trước để dữ liệu không cập nhật dở nếu có trường sai.
        String newPhone = requireText(phone, "Số điện thoại");
        String newEmail = requireEmail(email);
        String newAddress = requireText(address, "Địa chỉ");
        this.phone = newPhone;
        this.email = newEmail;
        this.address = newAddress;
    }

    public void addAccount(BankAccount account) {
        Objects.requireNonNull(account, "Tài khoản không được null");
        if (!customerId.equals(account.getCustomerId())) {
            throw new IllegalArgumentException("Tài khoản không thuộc khách hàng này");
        }
        if (accounts.stream().anyMatch(a -> a.getAccountNumber().equals(account.getAccountNumber()))) {
            throw new IllegalArgumentException("Số tài khoản đã tồn tại trong hồ sơ");
        }
        if (account instanceof CheckingAccount
                && accounts.stream().anyMatch(a -> a instanceof CheckingAccount)) {
            throw new IllegalStateException("Khách hàng đã có tài khoản thanh toán");
        }
        if (account instanceof LoanAccount && account.getStatus() == AccountStatus.ACTIVE
                && accounts.stream().anyMatch(a -> a instanceof LoanAccount
                        && a.getStatus() == AccountStatus.ACTIVE)) {
            throw new IllegalStateException("Khách hàng đang có khoản vay hoạt động");
        }
        accounts.add(account);
    }

    @Override
    public void deactivate() {
        if (accounts.stream().anyMatch(a -> a.getStatus() == AccountStatus.ACTIVE)) {
            throw new IllegalStateException("Phải xử lý hết tài khoản hoạt động trước khi ngừng khách hàng");
        }
        super.deactivate();
    }

    @Override
    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }

    @Override
    public String toString() {
        return "Customer{customerId='" + customerId + "', fullName='" + fullName
                + "', status=" + getStatus() + ", accounts=" + accounts.size() + "}";
    }
}
