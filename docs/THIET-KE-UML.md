# Thiết kế UML — đối chiếu mã nguồn đã triển khai

**Trạng thái:** đồng bộ theo ứng dụng Java Swing, không còn là thiết kế “chưa triển khai”.

## Nguồn chuẩn

- [Class Diagram tổng hợp](../uml/class-diagram-current.md): Model, các Service, Factory, DI, Repository, UI.
- [Class Diagram Model](../uml/class-diagram-model.md): thực thể, kế thừa, đa hình, Composition.
- [Class Diagram Service/DI](../uml/class-diagram-service-factory.md): chữ ký Service, Factory, Singleton, Repository.

Các nguồn .mmd là bản chỉnh sửa chính. Đã xuất lại ba sơ đồ thành PNG/SVG cùng tên trong thư mục `uml/`, khớp bản Mermaid hiện tại.

## Thực thể và các quan hệ

**User (abstract)** gồm userId, username, passwordHash, status, mustChangePassword; getRole() là abstract. Customer và Employee kế thừa. Customer override deactivate() để chặn ngừng khi vẫn còn tài khoản ACTIVE và có toString() riêng.

**BankAccount (abstract)** chứa accountNumber, customerId, balance và status; getAccountType() là abstract. CheckingAccount, SavingsAccount và LoanAccount override getAccountType(). CheckingAccount nộp/rút/đóng/mở lại, SavingsAccount tính lãi và tất toán, LoanAccount tính lãi cố định và trả toàn bộ một lần.

**Composition:** Customer "1" *-- "0..*" BankAccount qua private List<BankAccount> accounts. Customer.addAccount() kiểm tra chủ sở hữu, giới hạn một CheckingAccount và một LoanAccount ACTIVE. getAccounts() trả List.copyOf() để không cho sửa collection bên ngoài. INACTIVE vẫn được giữ lịch sử.

**Aggregation:** JsonRepository "1" o-- "0..*" User và JsonRepository "1" o-- "0..*" Transaction. Repository quản lý dữ liệu trong hai collection, lưu và nạp lại JSON. Các đối tượng tồn tại như thực thể ứng dụng, không có quan hệ vòng đời sở hữu chặt như Composition. JsonRepository không giữ danh sách BankAccount cấp gốc.

**Transaction:** một lớp với TransactionType enum có bảy loại; fromAccountNumber/toAccountNumber là String. Quan hệ nét đứt đến BankAccount biểu thị mã tham chiếu, không phải association theo object. Giao dịch hoàn tất được lưu chung, không sửa/xóa.

## Service / Factory / DI / Repository

| Interface | Implementation | Nhiệm vụ |
| --- | --- | --- |
| AccountService | AccountServiceImpl | Xác thực, đổi mật khẩu, quản lý khách hàng và tài khoản thanh toán |
| TransactionService | TransactionServiceImpl | Nộp/rút/chuyển, tra cứu lịch sử giao dịch |
| SavingsService | SavingsServiceImpl | Mở/tất toán tiết kiệm, kiểm tra đáo hạn và tính lãi |
| LoanService | LoanServiceImpl | Giải ngân, trả hết vay, tính lãi và tổng trả |
| ReportService | ReportServiceImpl | Báo cáo tài khoản và giao dịch theo thời gian |

ReportService.generateTodayTransactionReport() là default method, tái sử dụng generateTransactionReport(today, today).

**JsonRepository Singleton eager:** static final INSTANCE, constructor private, getInstance() public, load/reload/save; Gson lưu một file JSON, ưu tiên ghi thay thế nguyên tử. ServiceSupport.save() cố gắng khôi phục trạng thái từ file đã lưu nếu gặp lỗi ghi.

**DI Singleton lazy:** private static instance; constructor private; public static synchronized getInstance(). Constructor nạp JsonRepository, tạo dữ liệu demo khi file chưa có, tạo và đăng ký 5 Service qua Factory rồi settleMaturedSavings(). register(Class<T>, T) là private, get(Class<T>) public. Không còn createDefault() hoặc new DI() từ Main.

**Năm Simple Factory** gọi static create() không tham số và tạo implementation tương ứng dùng JsonRepository.getInstance(). Không có lựa chọn nhiều implementation; không phải GoF Factory Method.

**UI:** Main chỉ gọi BankWindow.launch(DI.getInstance()); BankWindow dùng AccountService để đăng nhập/đổi mật khẩu, tạo EmployeePanel hoặc CustomerPanel theo User. Hai panel gọi các interface Service, không truy cập Repository hoặc tự lưu JSON.

## Đóng gói, đa hình và giới hạn

Các trường Model private, được validation khi cập nhật. Overload có SavingsAccount.calculateInterest()/calculateInterest(LocalDate), JsonRepository.load()/load(Path), ServiceSupport.requireActive(Customer)/requireActive(CheckingAccount). List<BankAccount> chứa nhiều loại tài khoản; ReportServiceImpl sử dụng account.getAccountType() đa hình.

User, BankAccount và Transaction có equals/toString thích hợp; Customer override deactivate() là hành vi nghiệp vụ. Không khẳng định có các phương thức override chưa tồn tại.

**Giới hạn:** UI điều hướng theo vai trò, nhưng API Service chưa nhận principal/phiên đăng nhập nên chưa cưỡng chế phân quyền độc lập tại tầng Service. Không thêm AuthService/Session vào UML khi code chưa có.

Xem [quy tắc nghiệp vụ](QUY-TAC-NGHIEP-VU.md) và [kế hoạch kiểm thử](KIEM-THU.md).
