# Đối chiếu 18 yêu cầu kỹ thuật

Đã đối chiếu trực tiếp file `.wiki/01-Do-an-nhom.pptx` (37 slide), đặc biệt slide 9–11. Chương 3 báo cáo cần chỉ rõ lớp/file thực tế cho từng mục. Cột dự kiến chỉ là hướng làm, chưa chứng minh hoàn thành.

| # | Yêu cầu | Vị trí/hướng làm dự kiến | Bằng chứng thực tế | Trạng thái |
| --- | --- | --- | --- | --- |
| 1 | Số lớp phù hợp bài toán, trách nhiệm rõ ràng | model, service, repository, ui | Chưa có | Chưa làm |
| 2 | 100% thuộc tính private, getter/setter phù hợp | Các lớp thực thể | Chưa có | Chưa làm |
| 3 | Setter có validation ở ít nhất 3 lớp | Thực thể và lớp con tài khoản | Chưa có | Chưa làm |
| 4 | Mỗi thực thể có ≥ 2 constructor, nối chuỗi bằng this() | Các lớp thực thể | Chưa có | Chưa làm |
| 5 | Ít nhất 2 nhóm phương thức nạp chồng | Chốt khi thiết kế API | Chưa có | Chưa làm |
| 6 | static và static final có mục đích | Hằng số và thành viên dùng chung phù hợp | Chưa có | Chưa làm |
| 7 | Ít nhất 2 quan hệ kết tập, gồm 1 Composition | Đã chốt Customer ◆── BankAccount, Customer.accounts (một tài khoản thuộc một Customer); cần chốt quan hệ kết tập thứ hai | uml/class-diagram-current.md | Đã có 1 Composition trên UML; thiếu quan hệ thứ hai và mã Java |
| 8 | Kế thừa ≥ 3 tầng hoặc 1 cha–3 con | BankAccount → CheckingAccount, SavingsAccount, LoanAccount | uml/class-diagram-current.md | Đã có cấu trúc trên UML; chưa cài đặt |
| 9 | Override toString(), equals(), ≥ 2 phương thức nghiệp vụ, có @Override | Cây thực thể và tài khoản | Chưa có | Chưa làm |
| 10 | ≥ 1 abstract class, có phương thức trừu tượng | Account | Chưa có | Chưa làm |
| 11 | ≥ 2 interface, ≥ 1 có default method | 5 interface service, ReportService có default generateTodayTransactionReport() | src/main/java/vn/edu/pxu/bank/service/ReportService.java; ServiceTest.java | Đã cài 5 interface và default method, có test |
| 12 | Collection kiểu cha chứa nhiều loại con, xử lý đa hình | Collection<Account> | Chưa có | Chưa làm |
| 13 | ≥ 2 lớp exception tự định nghĩa | NotFoundException và InsufficientBalanceException | src/main/java/vn/edu/pxu/bank/exception/; ServiceTest.java | Đã cài và dùng trong service, có test |
| 14 | Bắt lỗi đúng tầng, không catch rỗng/nuốt lỗi | ui, service, repository | UI hiện bắt RuntimeException và hiện dialog; ServiceSupport/JsonRepository xử lý lỗi lưu | Đã có phần hiển thị lỗi GUI và xử lý lưu; còn rà soát toàn bộ luồng và test giao diện |
| 15 | ≥ 2 trong Singleton / Factory / Strategy | Singleton JsonRepository và 5 Simple Factory `create()` đã có mã | JsonRepository.java; src/main/java/vn/edu/pxu/bank/factory/; FactoryTest.java | Đã code/test 2 pattern dạng Singleton + Simple Factory; Factory chỉ tạo một implementation nên cần xác minh cách chấm |
| 16 | Lưu file text/CSV/JSON, dữ liệu còn sau khi tắt | Gson một file data/bank-data.json, accounts lồng trong Customer, Transaction tách riêng | JsonRepository.java; JsonRepositoryTest.java; ServiceTest.java; UiStartupTest.java; data/README.md | Đã tích hợp lưu sau nghiệp vụ, khôi phục JSON và dữ liệu demo khởi động GUI |
| 17 | Repo GitHub, ≥ 25 commit trải đều | Lịch sử Git thực tế | Chưa có | Chưa làm |
| 18 | Chuẩn đặt tên Java, thụt lề nhất quán | Toàn bộ mã nguồn | Mới có Main.java | Cần kiểm tra khi hoàn thiện |

## Yêu cầu bổ sung

- [ ] Tối thiểu 4 nhóm chức năng nghiệp vụ, có thêm/sửa/xóa/tìm/thống kê ở mức phù hợp.
- [ ] Ít nhất 2 báo cáo thống kê có ý nghĩa.
- [ ] Nhập sai không làm chương trình crash.
- [ ] Java thuần JDK 17+, không framework.
- [ ] UML khớp mã nguồn cuối cùng.
- [ ] Báo cáo PDF 10–20 trang, đủ 5 chương.
- [ ] Bảng phân công XLSX, cá nhân đóng góp 100%.
- [ ] README, dữ liệu mẫu, ZIP và link GitHub đầy đủ.

Điểm thưởng tùy chọn theo slide 30: GUI hoàn chỉnh +0,5; ít nhất 10 test JUnit 5 có ý nghĩa +0,3; mẫu thiết kế ngoài ba mẫu đã học +0,2. Chỉ làm sau khi hoàn thiện phần bắt buộc.
