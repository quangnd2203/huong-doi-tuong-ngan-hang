package vn.edu.pxu.bank.factory;

import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.ReportService;
import vn.edu.pxu.bank.service.ReportServiceImpl;

/** Tạo ReportService mà không để nơi gọi phụ thuộc trực tiếp vào lớp Impl. */
public final class ReportServiceFactory {
    private ReportServiceFactory() {
    }

    public static ReportService create() {
        return new ReportServiceImpl(JsonRepository.getInstance());
    }
}
