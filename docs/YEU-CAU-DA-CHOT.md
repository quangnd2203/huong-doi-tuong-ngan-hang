# Yêu cầu đã chốt qua phỏng vấn

## Phạm vi và môi trường

- Thực hiện cá nhân, chạy local trước; chưa tạo Git hoặc GitHub.
- Java console với hai menu nhân viên và khách hàng.
- Đăng nhập bằng tên tài khoản và mật khẩu, phân quyền theo vai trò.
- Có sẵn tài khoản nhân viên demo để dùng thử; lưu toàn bộ người dùng, tài khoản và giao dịch trong một file `data/bank-data.json`, dùng **Gson** chuyển đổi object ↔ JSON; không tự viết parser.
- Các nhóm nghiệp vụ: khách hàng, tài khoản, giao dịch, tiết kiệm và khoản vay.
- Đã bổ sung LoanAccount: dư nợ gốc dương, giải ngân vào tài khoản thanh toán, trả hết một lần từ tài khoản thanh toán. Đã chốt kỳ hạn 6/12 tháng, lãi suất 10%/năm, tính lãi cố định theo kỳ hạn và không xử lý riêng trả sớm/trễ; chưa triển khai mã nguồn.

## Các quyền đã được lựa chọn

| Thao tác | Vai trò được chọn |
| --- | --- |
| Tạo hồ sơ khách hàng | Nhân viên |
| Tạo tài khoản đăng nhập cho khách | Nhân viên |
| Tạo tài khoản ngân hàng | Nhân viên |
| Đổi mật khẩu đăng nhập của khách | Khách hàng |
| Đặt lại mật khẩu tạm cho khách quên mật khẩu | Nhân viên |
| Xem số dư và lịch sử giao dịch | Khách hàng |
| Chuyển khoản | Khách hàng |
| Xử lý nộp/rút tiền tại quầy | Nhân viên |
| Mở sổ tiết kiệm, tất toán tại quầy | Nhân viên |
| Xem sổ tiết kiệm và lãi dự kiến | Khách hàng |
| Trả toàn bộ khoản vay của chính mình | Khách hàng |
| Đóng tài khoản thanh toán đủ điều kiện | Nhân viên |
| Ngừng hoạt động hồ sơ khách hàng đủ điều kiện | Nhân viên |
| Xem hai báo cáo và tra cứu toàn hệ thống | Nhân viên |
| Sửa điện thoại, email, địa chỉ của chính mình | Khách hàng |
| Sửa điện thoại, email, địa chỉ và họ tên khách hàng | Nhân viên |

Khách hàng chỉ được xem dữ liệu của chính mình. Tài khoản đăng nhập và tài khoản ngân hàng là hai khái niệm riêng.

Sau khi nhân viên đặt lại mật khẩu tạm, khách bắt buộc đổi mật khẩu khi đăng nhập lại.

## Báo cáo đã chọn

1. Tổng số tài khoản theo loại và trạng thái; tổng số dư tiền gửi, đồng thời thống kê riêng số khoản vay và tổng dư nợ gốc (không gộp với tiền gửi).
2. Tổng tiền giao dịch theo khoảng ngày, tách **bảy nhóm**: nộp tiền mặt, rút tiền mặt, chuyển khoản, mở sổ và tất toán tiết kiệm, giải ngân khoản vay, trả nợ vay.

Báo cáo tài khoản thống kê tất cả, tách rõ tài khoản thanh toán ACTIVE/INACTIVE, sổ đang gửi/đã tất toán và khoản vay ACTIVE/INACTIVE. Dư nợ gốc lấy từ LoanAccount.balance, báo cáo riêng, không cộng chung vào số dư tiền gửi.

Mỗi chuyển khoản chỉ tính một lần. Giao dịch sửa sai bằng thao tác thông thường vẫn tính theo loại tương ứng; mở/tất toán tiết kiệm là nhóm riêng, không gộp vào ba nhóm nộp/rút/chuyển.

Giao dịch tất toán ghi amount bằng tổng gốc + lãi thực trả về tài khoản thanh toán; note ghi chi tiết gốc/lãi. Báo cáo tổng tiền tất toán cộng amount, không yêu cầu trường lãi riêng. Giao dịch LOAN_DISBURSEMENT thống kê tiền gốc giải ngân, LOAN_REPAYMENT thống kê tổng gốc + lãi khách đã trả; tách riêng, không tính trùng vào nộp/rút/chuyển.

Chưa chọn báo cáo top khách hàng hoặc báo cáo tiết kiệm theo kỳ hạn cho bản đầu tiên.

## Tiết kiệm đã chốt ở M2

Kỳ hạn 3 và 12 tháng, lãi suất lần lượt 3% và 6%/năm. Tất toán trước hạn toàn bộ hưởng 0,2%/năm. Tính lãi theo ngày thực gửi/365; mở sổ trừ tài khoản thanh toán, tất toán trả gốc và lãi vào tài khoản đó. Khi chương trình kiểm tra sổ đến hạn, tự động trả gốc và lãi.

Lưu cả lãi suất kỳ hạn và lãi trước hạn tại thời điểm mở sổ. Thay đổi cấu hình về sau chỉ áp dụng với sổ mới.

Ngày đến hạn cộng 3 hoặc 12 tháng theo lịch; thiếu ngày tương ứng thì lấy cuối tháng đích. Số ngày tính lãi là chênh lệch ngày, mở và tất toán cùng ngày được 0 ngày.

Chi tiết và các quy ước còn mở được ghi tại [QUY-TAC-NGHIEP-VU.md](QUY-TAC-NGHIEP-VU.md).

## Giao dịch và số tiền đã chốt ở M2

- Miễn phí nộp/rút/chuyển khoản.
- Chưa đặt hạn mức riêng, kiểm tra số tiền dương và số dư đủ khi trừ tiền.
- Mở sổ tiết kiệm tối thiểu 1.000.000 đồng.
- Tiền nhập theo đồng nguyên; tiền lãi làm tròn đến đồng, từ 0,5 đồng làm tròn lên.
- Mỗi khách một tài khoản thanh toán, có thể có nhiều sổ tiết kiệm.
- Kiểm tra sổ đến hạn khi khởi động và trước mỗi thao tác nghiệp vụ. Nếu phát hiện trễ, lãi vẫn dừng tại ngày đến hạn.
- Giao dịch đã hoàn tất không được sửa/xóa; nhân viên xử lý sai sót bằng giao dịch thông thường, ghi chú mã giao dịch sai và giữ lịch sử cả hai. Chưa làm chức năng bù chuyên biệt.

## Điểm cần đặc tả khi thiết kế M2

Đã chốt tách service: AccountService quản lý tài khoản thanh toán và tra cứu với 5 hàm `openCheckingAccount`, `findByAccountNumber`, `findByCustomerId`, `closeCheckingAccount`, `reopenCheckingAccount` (giữ chức năng kích hoạt lại tài khoản cũ); **TransactionService xử lý nộp/rút/chuyển khoản thông thường**; SavingsService xử lý tiết kiệm; LoanService xử lý vay; **ReportService (interface) / ReportServiceImpl** thực hiện hai báo cáo thống kê đã chốt. Mỗi service dùng interface và lớp Impl tương ứng. Không đưa nộp/rút/chuyển vào AccountService. TransactionService đã chốt 3 hàm `deposit(String accountNumber, BigDecimal amount)`, `withdraw(String accountNumber, BigDecimal amount)`, `transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount)`; cả ba trả về `Transaction` vừa ghi. Nghiệp vụ sổ tiết kiệm và khoản vay vẫn do service tương ứng đảm nhận. ReportService đã chốt cách tách interface/Impl và hai phương thức `generateAccountReport(): String`, `generateTransactionReport(LocalDate fromDate, LocalDate toDate): String`, cùng `default generateTodayTransactionReport(): String` gọi báo cáo giao dịch từ hôm nay đến hôm nay (không thêm loại báo cáo thứ ba). Service tạo chuỗi báo cáo, menu console hiển thị; không tạo lớp kết quả riêng.

Đã chốt API case study tối giản:
- `SavingsService`: `openSavingsAccount(customerId, amount, termMonths): SavingsAccount`, `settleSavingsAccount(accountNumber): void`, `settleMaturedSavings(): void`.
- `LoanService`: `openLoanAccount(customerId, amount, annualRate, endDate): LoanAccount`, `repayLoanAccount(accountNumber): void`.
- Lãi được tính trong `SavingsAccount` / `LoanAccount`, không thêm hàm service trùng chức năng; không tạo lớp kết quả riêng. Lãi vay đã chốt: gốc × 10% × số tháng kỳ hạn / 12, làm tròn đến đồng, tổng tiền trả không đổi theo ngày trả thực tế.

Khoản vay đã chọn: nhân viên giải ngân vào tài khoản thanh toán, dư nợ gốc lưu dương trong LoanAccount.balance; khách đăng nhập tự trả toàn bộ gốc+lãi khoản vay của chính mình từ tài khoản thanh toán. Trả thành công thì dư nợ về 0 và trạng thái tự chuyển INACTIVE, không cần đóng thủ công. Mỗi khách chỉ có tối đa một khoản vay ACTIVE tại cùng thời điểm; sau khi trả xong, được vay tiếp bằng một khoản mới và giữ nguyên lịch sử khoản cũ. Thêm LOAN_DISBURSEMENT và LOAN_REPAYMENT vào hai nhóm riêng của báo cáo giao dịch hiện có; không tạo báo cáo thứ ba.

Quyết định UML được cập nhật từng phần tại [THIET-KE-UML.md](THIET-KE-UML.md): đã chốt Customer và chọn Transaction dùng enum. Customer giữ thông tin liên hệ trực tiếp, chưa tách ContactInfo. **Đã chốt Composition Customer–BankAccount:** Customer chứa `List<BankAccount> accounts`; mỗi tài khoản thuộc duy nhất một khách, `customerId` dùng kiểm tra chủ sở hữu. Tài khoản và hồ sơ INACTIVE vẫn lưu để giữ lịch sử.

- Cây BankAccount đã có CheckingAccount, SavingsAccount và LoanAccount trên UML, đủ cấu trúc một cha–ba con. Đã chốt khách tự trả khoản vay của mình, mỗi khách tối đa một khoản ACTIVE, được vay tiếp sau tất toán và giữ lịch sử; không cho đóng tài khoản thanh toán hay ngừng hoạt động khách khi còn khoản vay ACTIVE. Khoản vay được thống kê riêng trong cả hai báo cáo hiện có, không cộng dư nợ vào tiền gửi.
- Đã chốt một file `data/bank-data.json` chứa toàn bộ người dùng, tài khoản và giao dịch, không chia thành nhiều file. Mỗi BankAccount chỉ lưu **một lần bên trong `Customer.accounts`** của chủ sở hữu, không có mảng accounts toàn cục trùng lặp; Transaction vẫn là danh sách chung, không sao chép thành giao dịch riêng cho hai khách. **Tự lưu ngay sau mỗi nghiệp vụ thành công làm thay đổi dữ liệu**, không chỉ khi thoát; thao tác chỉ tra cứu không cần ghi. Tất toán tiết kiệm tự động cũng phải lưu khi có thay đổi. Đã chọn Gson; còn thiết kế schema JSON, xử lý lớp kế thừa và kiểu ngày, lưu mật khẩu, cách xử lý ghi thất bại và file chưa tồn tại. File đã có nhưng hỏng/không đọc được phải báo lỗi, dừng sử dụng dữ liệu, giữ nguyên file và không ghi đè bằng dữ liệu mới.
- Cách trình bày tiền gốc lịch sử của sổ đã tất toán so với số dư hiện tại; báo cáo tất toán dùng tổng gốc+lãi, chi tiết được ghi trong note.

Phỏng vấn phạm vi và quy tắc nghiệp vụ chính đã hoàn tất, đủ đầu vào để dựng UML v1. Các điểm trên sẽ được xử lý trong tài liệu thiết kế, phân biệt đề xuất kỹ thuật với quyết định đã được sinh viên lựa chọn. Chưa triển khai mã nghiệp vụ hoặc hoàn thành mốc UML.

## Hai Design Pattern đã chọn trên bản thiết kế

- **Singleton:** `JsonRepository` có một instance qua `getInstance()`, chịu trách nhiệm đọc/ghi `data/bank-data.json` bằng Gson.
- **Simple Factory:** mỗi interface service có một Factory riêng với `create(...)`, trả về interface và tạo implementation mặc định tương ứng. Các dependency được truyền qua constructor; chưa dùng DI container hay thêm implementation mới. Khi triển khai cần làm rõ logic lựa chọn implementation để đủ bằng chứng áp dụng Factory theo rubric; chưa coi là đã hoàn thành tiêu chí.
- Mới cập nhật UML/tài liệu, **chưa viết Java**.

## Đóng tài khoản và ngừng hoạt động khách hàng

- Chỉ đóng tài khoản thanh toán khi số dư bằng 0, không còn sổ tiết kiệm ACTIVE và không còn khoản vay ACTIVE; giữ lịch sử giao dịch.
- Khi khách quay lại, kích hoạt lại tài khoản cũ, giữ nguyên số tài khoản.
- Luôn giữ hồ sơ khách hàng; chỉ ngừng hoạt động sau khi đã xử lý hết tài khoản, sổ tiết kiệm và không còn khoản vay ACTIVE.
