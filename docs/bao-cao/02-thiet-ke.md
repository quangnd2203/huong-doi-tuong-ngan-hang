# Chương 2. Thiết kế hệ thống

## 2.1. Kiến trúc phân tầng

Ứng dụng Java 17 + Swing chia thành tầng UI, Service nghiệp vụ, Model và Repository. Main.main() khởi động DI Singleton, sau đó gọi BankWindow.launch(di). DI nạp JSON, tạo dữ liệu giả lập nếu lần đầu và đăng ký năm Service. UI chỉ sử dụng Service, không đọc/ghi Repository hoặc băm mật khẩu trực tiếp.

Luồng phụ thuộc: **Main → DI → năm Factory → năm ServiceImpl → JsonRepository Singleton → data/bank-data.json**. BankWindow/EmployeePanel/CustomerPanel gọi interface Service được cung cấp bởi DI.

## 2.2. Class Diagram chuẩn

- [Class Diagram tổng hợp](../../uml/class-diagram-current.md).
- [Class Diagram Model](../../uml/class-diagram-model.md).
- [Class Diagram Service, Factory, Repository và DI](../../uml/class-diagram-service-factory.md).

Nguồn chính là Mermaid trong thư mục `uml/`; đã xuất PNG và SVG từ bản .mmd hiện tại. Trong báo cáo ưu tiên các bản SVG Model và Service/DI riêng để dễ đọc.

## 2.3. Kế thừa và quan hệ

User là lớp trừu tượng với Customer/Employee; BankAccount là lớp trừu tượng với CheckingAccount/SavingsAccount/LoanAccount. Mỗi lớp con BankAccount override getAccountType().

**Composition:** Customer sở hữu List<BankAccount> accounts; Customer.addAccount() kiểm tra customerId, không chấp nhận tài khoản không thuộc chủ sở hữu.

**Aggregation:** JsonRepository quản lý List<User> và List<Transaction>; tài khoản không được sao chép thành danh sách toàn cục. Transaction lưu mã tài khoản nguồn/đích dạng String.

## 2.4. Interface, đa hình, đóng gói

Năm interface AccountService, TransactionService, SavingsService, LoanService, ReportService có năm implementation. Thuộc tính thực thể private, các phương thức cập nhật thực hiện validation. Collection List<BankAccount> giữ ba loại tài khoản, ReportServiceImpl gọi đa hình getAccountType(). ReportService định nghĩa default method generateTodayTransactionReport().

## 2.5. Design Pattern

JsonRepository sử dụng eager Singleton; DI sử dụng lazy Singleton, constructor private và getInstance(). Năm Simple Factory tạo service implementations qua create(), dùng cùng Repository Singleton. Simple Factory không phải GoF Factory Method.

## 2.6. Lưu trữ và giới hạn

Gson lưu User, Customer.accounts và Transaction vào một file JSON. Các nghiệp vụ thay đổi dữ liệu thành công được lưu ngay; ServiceSupport phục hồi từ file cũ khi xảy ra lỗi ghi. UI giới hạn thao tác theo vai trò, tuy nhiên Service chưa được truyền principal để cưỡng chế quyền độc lập.

Chương 4 sẽ đưa bằng chứng JUnit và ảnh giao diện Swing chạy thật.
