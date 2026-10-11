package vn.edu.pxu.bank.service;

import java.time.LocalDate;

public interface ReportService {
    String generateAccountReport();
    String generateTransactionReport(LocalDate fromDate, LocalDate toDate);

    default String generateTodayTransactionReport() {
        LocalDate today = LocalDate.now();
        return generateTransactionReport(today, today);
    }
}
