package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.AccountStatus;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import vn.edu.pxu.bank.util.PasswordUtils;

import static vn.edu.pxu.bank.util.ValidationUtils.requireEmail;
import static vn.edu.pxu.bank.util.ValidationUtils.requireText;
import static vn.edu.pxu.bank.util.ValidationUtils.requireUsername;
import vn.edu.pxu.bank.repository.JsonRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountServiceImpl implements AccountService {
    private final JsonRepository repository;

    public AccountServiceImpl(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public User authenticate(String username, char[] password) {
        if (username == null || password == null) return null;
        for (User user : repository.getUsers()) {
            if (user.getUsername().equals(username.trim())
                    && user.getStatus() == vn.edu.pxu.bank.model.user.UserStatus.ACTIVE
                    && PasswordUtils.verify(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void changePassword(String userId, char[] oldPassword, char[] newPassword) {
        User user = null;
        String id = requireText(userId, "Mã người dùng");
        for (User candidate : repository.getUsers()) {
            if (candidate.getUserId().equals(id)) {
                user = candidate;
                break;
            }
        }
        if (user == null) throw new IllegalArgumentException("Không tìm thấy người dùng");
        if (user.getStatus() != vn.edu.pxu.bank.model.user.UserStatus.ACTIVE) {
            throw new IllegalStateException("Người dùng đã ngừng hoạt động");
        }
        if (oldPassword == null || !PasswordUtils.verify(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (newPassword == null || newPassword.length < 6) {
            throw new IllegalArgumentException("Mật khẩu mới cần ít nhất 6 ký tự");
        }
        if (PasswordUtils.verify(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ");
        }
        user.changePassword(user.getPasswordHash(), PasswordUtils.hash(newPassword));
        ServiceSupport.save(repository);
    }

    @Override
    public char[] generateTemporaryPassword() {
        return PasswordUtils.temporaryPassword();
    }

    @Override
    public List<Customer> findAllCustomers() {
        List<Customer> result = new ArrayList<>();
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) result.add(customer);
        }
        return List.copyOf(result);
    }

    @Override
    public Customer createCustomer(String username, String fullName, String phone, String email,
                                   String address, char[] temporaryPassword) {
        String login = requireUsername(username);
        for (User user : repository.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(login)) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
            }
        }
        requireTemporaryPassword(temporaryPassword);
        Customer customer = new Customer(nextUserId(), login,
                PasswordUtils.hash(temporaryPassword), nextCustomerId(),
                fullName, phone, email, address);
        customer.resetPassword(customer.getPasswordHash()); // Bắt buộc đổi mật khẩu khi đăng nhập lần đầu.
        // Kiểm tra hết dữ liệu đầu vào trước khi chạy nghiệp vụ tự xử lý sổ đến hạn.
        ServiceSupport.beforeOperation(repository);
        repository.getUsers().add(customer);
        ServiceSupport.save(repository);
        return customer;
    }

    @Override
    public Customer findCustomer(String customerId) {
        return ServiceSupport.customer(repository, customerId);
    }

    @Override
    public void updateCustomer(String customerId, String fullName, String phone,
                               String email, String address) {
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        // Kiểm tra đủ trường trước khi sửa để tránh cập nhật một nửa khi nhập sai.
        String name = requireText(fullName, "Họ tên");
        String validatedPhone = requireText(phone, "Số điện thoại");
        String validatedEmail = requireEmail(email);
        String validatedAddress = requireText(address, "Địa chỉ");
        customer.changeName(name);
        customer.updateContactInfo(validatedPhone, validatedEmail, validatedAddress);
        ServiceSupport.save(repository);
    }

    @Override
    public void resetCustomerPassword(String customerId, char[] temporaryPassword) {
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        requireTemporaryPassword(temporaryPassword);
        String hash = PasswordUtils.hash(temporaryPassword);
        customer.resetPassword(hash);
        ServiceSupport.save(repository);
    }

    @Override
    public void deactivateCustomer(String customerId) {
        ServiceSupport.beforeOperation(repository);
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        customer.deactivate(); // Model từ chối nếu còn tài khoản ACTIVE.
        ServiceSupport.save(repository);
    }

    private static void requireTemporaryPassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Mật khẩu tạm cần ít nhất 8 ký tự");
        }
    }

    private String nextUserId() {
        long max = 0;
        for (User user : repository.getUsers()) {
            max = Math.max(max, ServiceSupport.numericSuffix(user.getUserId(), "U"));
        }
        return "U" + String.format("%06d", max + 1);
    }

    private String nextCustomerId() {
        long max = 0;
        for (User user : repository.getUsers()) {
            if (user instanceof Customer customer) {
                max = Math.max(max, ServiceSupport.numericSuffix(customer.getCustomerId(), "KH"));
            }
        }
        return "KH" + String.format("%06d", max + 1);
    }

    @Override
    public CheckingAccount openCheckingAccount(String customerId) {
        ServiceSupport.beforeOperation(repository);
        Customer customer = ServiceSupport.customer(repository, customerId);
        ServiceSupport.requireActive(customer);
        for (BankAccount account : customer.getAccounts()) {
            if (account instanceof CheckingAccount) {
                throw new IllegalStateException("Khách đã có tài khoản thanh toán; hãy mở lại tài khoản cũ");
            }
        }
        CheckingAccount checking = new CheckingAccount(
                ServiceSupport.nextAccountId(repository, "CK"), customer.getCustomerId());
        customer.addAccount(checking);
        ServiceSupport.save(repository);
        return checking;
    }

    @Override
    public BankAccount findByAccountNumber(String accountNumber) {
        ServiceSupport.beforeOperation(repository);
        return ServiceSupport.account(repository, accountNumber);
    }

    @Override
    public List<BankAccount> findByCustomerId(String customerId) {
        ServiceSupport.beforeOperation(repository);
        return ServiceSupport.customer(repository, customerId).getAccounts();
    }

    @Override
    public void closeCheckingAccount(String accountNumber) {
        ServiceSupport.beforeOperation(repository);
        CheckingAccount checking = ServiceSupport.checking(repository, accountNumber);
        Customer customer = ServiceSupport.customer(repository, checking.getCustomerId());
        for (BankAccount account : customer.getAccounts()) {
            if (!(account instanceof CheckingAccount)
                    && account.getStatus() == AccountStatus.ACTIVE) {
                throw new IllegalStateException("Còn sổ tiết kiệm hoặc khoản vay đang hoạt động");
            }
        }
        checking.close();
        ServiceSupport.save(repository);
    }

    @Override
    public void reopenCheckingAccount(String accountNumber) {
        ServiceSupport.beforeOperation(repository);
        CheckingAccount checking = ServiceSupport.checking(repository, accountNumber);
        Customer customer = ServiceSupport.customer(repository, checking.getCustomerId());
        ServiceSupport.requireActive(customer);
        checking.reopen();
        ServiceSupport.save(repository);
    }
}
