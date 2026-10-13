package vn.edu.pxu.bank.di;

import vn.edu.pxu.bank.factory.AccountServiceFactory;
import vn.edu.pxu.bank.factory.LoanServiceFactory;
import vn.edu.pxu.bank.factory.ReportServiceFactory;
import vn.edu.pxu.bank.factory.SavingsServiceFactory;
import vn.edu.pxu.bank.factory.TransactionServiceFactory;
import vn.edu.pxu.bank.repository.JsonRepository;
import vn.edu.pxu.bank.service.AccountService;
import vn.edu.pxu.bank.service.LoanService;
import vn.edu.pxu.bank.service.ReportService;
import vn.edu.pxu.bank.service.SavingsService;
import vn.edu.pxu.bank.service.TransactionService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Singleton DI: khởi tạo dữ liệu và Service một lần trước khi mở giao diện. */
public final class DI {
    private static DI instance;
    private final Map<Class<?>, Object> dependencies = new HashMap<>();

    private DI() {
        JsonRepository repository = JsonRepository.getInstance();
        boolean firstLaunch = Files.notExists(Path.of("data", "bank-data.json"));
        repository.load();
        if (firstLaunch) DemoData.initialize(repository);

        register(AccountService.class, AccountServiceFactory.create());
        register(TransactionService.class, TransactionServiceFactory.create());
        register(SavingsService.class, SavingsServiceFactory.create());
        register(LoanService.class, LoanServiceFactory.create());
        register(ReportService.class, ReportServiceFactory.create());

        get(SavingsService.class).settleMaturedSavings();
    }

    public static synchronized DI getInstance() {
        if (instance == null) instance = new DI();
        return instance;
    }

    private <T> void register(Class<T> type, T instance) {
        Objects.requireNonNull(type, "Kiểu dependency không được null");
        dependencies.put(type, type.cast(Objects.requireNonNull(instance,
                "Dependency không được null")));
    }

    public <T> T get(Class<T> type) {
        Objects.requireNonNull(type, "Kiểu dependency không được null");
        Object instance = dependencies.get(type);
        if (instance == null) {
            throw new IllegalStateException("Chưa đăng ký dependency: " + type.getSimpleName());
        }
        return type.cast(instance);
    }

}
