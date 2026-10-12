package vn.edu.pxu.bank.factory;

import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.AccountServiceImpl;

/** Tạo AccountService mà không để nơi gọi phụ thuộc trực tiếp vào lớp Impl. */
public final class AccountServiceFactory {
    private AccountServiceFactory() {
    }

    public static AccountService create() {
        return new AccountServiceImpl(JsonRepository.getInstance());
    }
}
