package vn.edu.pxu.bank.factory;

import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.LoanService;
import vn.edu.pxu.bank.service.LoanServiceImpl;

/** Tạo LoanService mà không để nơi gọi phụ thuộc trực tiếp vào lớp Impl. */
public final class LoanServiceFactory {
    private LoanServiceFactory() {
    }

    public static LoanService create() {
        return new LoanServiceImpl(JsonRepository.getInstance());
    }
}
