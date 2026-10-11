package vn.edu.pxu.bank.service;

import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.User;
import java.util.List;

public interface AccountService {
    User authenticate(String username, char[] password);
    void changePassword(String userId, char[] oldPassword, char[] newPassword);
    char[] generateTemporaryPassword();
    List<Customer> findAllCustomers();
    Customer createCustomer(String username, String fullName, String phone, String email,
                            String address, char[] temporaryPassword);
    Customer findCustomer(String customerId);
    void updateCustomer(String customerId, String fullName, String phone, String email, String address);
    void resetCustomerPassword(String customerId, char[] temporaryPassword);
    void deactivateCustomer(String customerId);

    CheckingAccount openCheckingAccount(String customerId);
    BankAccount findByAccountNumber(String accountNumber);
    List<BankAccount> findByCustomerId(String customerId);
    void closeCheckingAccount(String accountNumber);
    void reopenCheckingAccount(String accountNumber);
}
