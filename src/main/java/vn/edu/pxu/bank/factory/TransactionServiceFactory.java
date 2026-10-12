package vn.edu.pxu.bank.factory;

import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.TransactionService;
import vn.edu.pxu.bank.service.TransactionServiceImpl;

/** Tạo TransactionService mà không để nơi gọi phụ thuộc trực tiếp vào lớp Impl. */
public final class TransactionServiceFactory {
    private TransactionServiceFactory() {
    }

    public static TransactionService create() {
        return new TransactionServiceImpl(JsonRepository.getInstance());
    }
}
