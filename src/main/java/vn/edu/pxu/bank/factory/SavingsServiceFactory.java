package vn.edu.pxu.bank.factory;

import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.SavingsService;
import vn.edu.pxu.bank.service.SavingsServiceImpl;

/** Tạo SavingsService mà không để nơi gọi phụ thuộc trực tiếp vào lớp Impl. */
public final class SavingsServiceFactory {
    private SavingsServiceFactory() {
    }

    public static SavingsService create() {
        return new SavingsServiceImpl(JsonRepository.getInstance());
    }
}
