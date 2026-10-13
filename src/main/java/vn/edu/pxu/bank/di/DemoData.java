package vn.edu.pxu.bank.di;

import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.util.PasswordUtils;
import java.util.Arrays;

/** Dữ liệu giả lập: chỉ tạo khi lần chạy đầu chưa có JSON. */
public final class DemoData {
    private DemoData() {
    }

    public static void initialize(JsonRepository repository) {
        if (!repository.getUsers().isEmpty() || !repository.getTransactions().isEmpty()) {
            return;
        }

        char[] adminPassword = "Admin@123".toCharArray();
        char[] customerPassword = "Khach@123".toCharArray();
        try {
            Employee employee = new Employee("U001", "admin", PasswordUtils.hash(adminPassword));
            Customer customer = new Customer("U002", "khach", PasswordUtils.hash(customerPassword),
                    "KH001", "Nguyễn Văn A", "0912345678", "khach@example.com", "Hà Nội");
            customer.addAccount(new CheckingAccount("CK000001", customer.getCustomerId()));
            repository.getUsers().add(employee);
            repository.getUsers().add(customer);
            repository.save();
        } finally {
            Arrays.fill(adminPassword, '\0');
            Arrays.fill(customerPassword, '\0');
        }
    }
}
