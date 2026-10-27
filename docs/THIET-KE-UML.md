# Quyết định thiết kế UML

Tài liệu ghi từng phần được sinh viên chốt trong quá trình phỏng vấn UML. Chưa có sơ đồ lớp hoàn chỉnh hoặc mã nghiệp vụ.

Xem [sơ đồ tổng hợp hiện tại](../uml/class-diagram-current.md), hoặc hai bản gọn đã lưu: [UML Model](../uml/class-diagram-model.md) và [UML Service – Factory – Repository](../uml/class-diagram-service-factory.md). Các sơ đồ là bản thiết kế đang thảo luận, chưa phải UML v1 hoàn chỉnh.

## Customer — đã chốt

Đại diện hồ sơ khách hàng. Sau khi xem bản phác, hướng thiết kế hiện tại là Customer kế thừa User và nhận phần đăng nhập từ lớp cha. Thuộc tính riêng private:

| Thuộc tính | Kiểu |
| --- | --- |
| customerId | String |
| fullName | String |
| phone | String |
| email | String |
| address | String |
| accounts | List<BankAccount> | Danh sách tài khoản thuộc khách hàng, sở hữu bằng Composition |

Phương thức nghiệp vụ đã được trình bày và chốt:

- `updateContactInfo(phone, email, address)`.
- `changeName(fullName)`.
- `deactivate()` được kế thừa từ User trong bản phác hiện tại.

Giữ điện thoại/email/địa chỉ trực tiếp trong Customer, chưa tách ContactInfo; chưa thêm CCCD hoặc ngày sinh. Dùng chung User.status, không có Customer.status riêng. Chỉ được ngừng hoạt động khách khi đã xử lý hết tài khoản và sổ tiết kiệm, đồng thời không còn khoản vay ACTIVE; kiểm tra điều kiện ở tầng service.

**Đã chốt Composition `Customer ◆── BankAccount`**: mỗi BankAccount thuộc đúng một Customer suốt vòng đời và nằm trong `Customer.accounts`; không tồn tại tài khoản mồ côi hoặc bị hai khách cùng sở hữu. Đóng tài khoản hay chuyển Customer sang INACTIVE là đổi trạng thái, không xóa hồ sơ/tài khoản/giao dịch nên không mâu thuẫn với yêu cầu lưu lịch sử. Constructor, getter/setter, validation và chữ ký chi tiết sẽ hoàn thiện khi viết Java; danh sách không được cho bên ngoài tùy tiện sửa gây lệch chủ sở hữu.

## Transaction — hướng đã chọn

Một lớp Transaction và enum TransactionType để phân biệt nộp tiền, rút tiền, chuyển khoản, mở sổ, tất toán. Không tách cây Transaction/CashTransaction/DepositTransaction/WithdrawalTransaction như đề xuất ban đầu.

Các thuộc tính private đã được chọn:

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| transactionId | String | Mã giao dịch |
| amount | BigDecimal | Số tiền; tất toán ghi tổng gốc + lãi thực trả |
| fromAccountNumber | String | Số tài khoản nguồn, để trống khi nộp tiền mặt |
| toAccountNumber | String | Số tài khoản đích, để trống khi rút tiền mặt |
| type | TransactionType | DEPOSIT, WITHDRAWAL, TRANSFER, SAVINGS_OPEN, SAVINGS_SETTLEMENT, LOAN_DISBURSEMENT, LOAN_REPAYMENT |
| occurredAt | LocalDateTime | Ngày và giờ giao dịch |
| note | String | Ghi chú, gồm mã giao dịch sai khi thực hiện giao dịch điều chỉnh |

Đã chọn bỏ performedByUserId, không lưu người thực hiện và không cần quy ước tác nhân hệ thống. Nguồn/đích liên kết BankAccount bằng số tài khoản, không phải mã khách hàng hoặc tham chiếu đối tượng trực tiếp.

Đã chốt amount của tất toán là tổng gốc + lãi thực trả; note ghi chi tiết tiền gốc và tiền lãi, không thêm thuộc tính lãi riêng. Ví dụ gốc 2.000.000 đồng, lãi 15.123 đồng thì amount là 2.015.123 đồng. Constructor, phương thức và validation sẽ thiết kế tiếp; giao dịch hoàn tất không sửa/xóa theo quy tắc nghiệp vụ.

## User — hướng kế thừa và trạng thái chung

Xem [bản phác User → Customer, Employee](../uml/user-inheritance-proposal.md). User là lớp cha trừu tượng chứa thông tin đăng nhập chung; Customer bổ sung hồ sơ, Employee đại diện nhân viên.

Đã chốt một thuộc tính User.status kiểu UserStatus (ACTIVE/INACTIVE) cho trạng thái hoạt động và quyền đăng nhập. Bỏ loginEnabled và trạng thái hồ sơ Customer riêng. Tài khoản ngân hàng có trạng thái đóng/mở riêng. Các thuộc tính và phương thức khác trong bản phác vẫn được hoàn thiện dần, chưa có mã nguồn.

## BankAccount — bản phác đang thảo luận

Đã chọn BankAccount trừu tượng với ba lớp con CheckingAccount, SavingsAccount và LoanAccount. Cấu trúc một cha–ba con đáp ứng yêu cầu kế thừa trên UML; chữ ký phương thức và phần nghiệp vụ vẫn đang hoàn thiện.

Các thuộc tính lớp cha trong bản phác:

| Thuộc tính | Kiểu | Ghi nhận |
| --- | --- | --- |
| accountNumber | String | Số tài khoản, đang đề xuất dùng làm định danh |
| customerId | String | Đã chọn thay tham chiếu đối tượng Customer owner bằng mã khách hàng |
| balance | BigDecimal | Số dư hiện tại |
| status | AccountStatus | Đã chốt ACTIVE/INACTIVE cho tài khoản ngân hàng, tách khỏi User.status |

`BankAccount.customerId` vẫn lưu để kiểm tra chủ sở hữu và tương thích các nghiệp vụ đã chốt; đây chỉ là **mã tham chiếu, không phải danh sách tài khoản thứ hai hoặc quan hệ kết tập thứ hai**. Khi tạo tài khoản, service phải tìm Customer hợp lệ, gắn tài khoản vào đúng `Customer.accounts` và bảo đảm `account.customerId == customer.customerId`. `findByCustomerId` đọc danh sách của Customer, `findByAccountNumber` tra cứu qua các Customer (chấp nhận duyệt danh sách với case study console), báo cáo duyệt các danh sách này. Không tạo thêm danh sách toàn cục độc lập lưu cùng các tài khoản.

Đóng tài khoản hoặc tất toán sổ chuyển AccountStatus sang INACTIVE. Mở lại tài khoản thanh toán cũ chuyển về ACTIVE. Không cho đóng CheckingAccount nếu còn LoanAccount ACTIVE của cùng khách, dù CheckingAccount.balance = 0. Điều kiện không còn sổ tiết kiệm ACTIVE và số dư thanh toán bằng 0 vẫn giữ nguyên.

Trong SavingsAccount, bỏ principal theo phạm vi chỉ mở sổ và tất toán toàn bộ: balance là tiền gốc khi đang gửi. Tính lãi và ghi nhận giao dịch trước khi đưa balance về 0; tiền gốc lịch sử tra từ giao dịch mở sổ được giữ lại. Không tính lại lãi lịch sử từ balance bằng 0 sau tất toán.

Các trường tiết kiệm đang xem gồm termMonths, annualRate, earlyWithdrawalRate, startDate và endDate. startDate là ngày bắt đầu gửi, endDate là ngày kết thúc kỳ hạn gửi. Đã chốt tính endDate từ startDate/kỳ hạn ngay khi mở sổ và lưu lại trong đối tượng/JSON theo quy tắc cuối tháng đã chọn. Không sửa ngày kết thúc độc lập làm lệch kỳ hạn.

## LoanAccount — đã bổ sung vào phạm vi

Sinh viên đã chọn thêm nghiệp vụ khoản vay. LoanAccount kế thừa các trường chung và trả về AccountType.LOAN. Đã chốt balance là dư nợ gốc dương, nhân viên giải ngân vào tài khoản thanh toán và trả hết một lần từ tài khoản thanh toán. Khi trả đủ gốc+lãi, dư nợ về 0 và trạng thái thành INACTIVE.

Thiết kế LoanAccount đang được trình bày:

| Thuộc tính riêng | Kiểu | Ý nghĩa |
| --- | --- | --- |
| annualRate | BigDecimal | Lãi suất năm cố định khi giải ngân: 0,10 (10%/năm) |
| startDate | LocalDate | Ngày bắt đầu khoản vay/giải ngân |
| endDate | LocalDate | Ngày kết thúc kỳ hạn vay |

Phương thức đã chốt theo công thức lãi cố định: `calculateInterest()`, `calculateTotalDue()` và ghi chồng `getAccountType()`. `calculateInterest()` = dư nợ gốc × annualRate × số tháng kỳ hạn / 12 (làm tròn đồng, HALF_UP); `calculateTotalDue()` = dư nợ gốc + tiền lãi. Không cần tham số onDate do lãi không phụ thuộc ngày trả thực tế. Không thêm trường gốc ban đầu; tra lịch sử từ giao dịch giải ngân.

Service phối hợp giải ngân và trả nợ giữa hai tài khoản, ghi Transaction. Nhân viên giải ngân; khách đăng nhập tự trả nợ cho khoản vay của chính mình. LOAN_DISBURSEMENT: từ tài khoản vay sang tài khoản thanh toán, amount là tiền gốc. LOAN_REPAYMENT: chiều ngược lại, amount là tổng gốc+lãi, note ghi chi tiết. Thanh toán thành công thì dư nợ bằng 0 và khoản vay tự chuyển INACTIVE, không có thao tác đóng riêng. Phép biến đổi dư nợ khác với tiền gửi, không áp dụng trừ/cộng theo chiều giao dịch một cách máy móc.

Đã chốt vay **6 hoặc 12 tháng, lãi suất cố định 10%/năm**, tính lãi theo toàn kỳ, trả toàn bộ một lần; không phân biệt trả sớm/trễ hoặc tính thêm phí phạt. Giữ `startDate`, `endDate` và không thêm `termMonths`. Ngày kết thúc tính bằng `startDate.plusMonths(6 hoặc 12)`; xác định số tháng bằng cách đối chiếu lại hai mốc này (tránh lỗi chênh lệch tháng khi rơi vào cuối tháng). Mỗi khách chỉ được có **tối đa một LoanAccount ACTIVE** tại cùng thời điểm. Khi trả hết, LoanAccount cũ chuyển thành INACTIVE và vẫn lưu để tra cứu; khách được mở một LoanAccount mới. Quan hệ Customer–LoanAccount vì vậy là một-nhiều theo lịch sử, còn ràng buộc một khoản đang hoạt động được kiểm tra khi giải ngân. Đã chốt không cho đóng tài khoản thanh toán hoặc ngừng hoạt động khách nếu còn LoanAccount ACTIVE. Đã chốt bổ sung khoản vay vào hai báo cáo hiện tại: báo cáo tài khoản tách LoanAccount ACTIVE/INACTIVE và tổng dư nợ gốc không cộng tiền gửi; báo cáo giao dịch tách LOAN_DISBURSEMENT và LOAN_REPAYMENT, mỗi giao dịch tính một lần.

## Service — đã chốt tách năm trách nhiệm

- AccountService / AccountServiceImpl: mở tài khoản thanh toán, tra cứu theo số tài khoản hoặc khách hàng, đóng/kích hoạt lại tài khoản thanh toán.
- TransactionService / TransactionServiceImpl: thực hiện **nộp tiền, rút tiền, chuyển khoản thông thường**, cập nhật số dư và ghi Transaction; không đưa ba nghiệp vụ này vào AccountService.
- SavingsService / SavingsServiceImpl: mở sổ, tất toán theo yêu cầu, xử lý sổ đến hạn khi khởi động/trước thao tác nghiệp vụ.
- LoanService / LoanServiceImpl: tạo khoản vay và giải ngân, thu toàn bộ gốc+lãi để kết thúc khoản vay.
- ReportService / ReportServiceImpl: lập hai báo cáo đã chốt gồm thống kê tài khoản (tách dư nợ vay) và thống kê giao dịch theo khoảng ngày (7 nhóm).

Đã chốt tách năm service, dùng interface và lớp Impl tương ứng; AccountService không gom nghiệp vụ giao dịch, tiết kiệm hoặc vay. Theo định hướng **case study Java console**, đã chốt API tối giản cho AccountService (5 phương thức), TransactionService (3), SavingsService (3) và LoanService (2); không thêm lớp kết quả (DTO) hoặc phương thức tính lãi trùng với model.

### AccountService — 5 phương thức đã chốt

| Phương thức | Trách nhiệm |
| --- | --- |
| `openCheckingAccount(String customerId)` | Tạo tài khoản thanh toán cho khách đủ điều kiện; trả về `CheckingAccount` |
| `findByAccountNumber(String accountNumber)` | Tra cứu một tài khoản; trả về `BankAccount` |
| `findByCustomerId(String customerId)` | Tra cứu các tài khoản thuộc khách; trả về `List<BankAccount>` |
| `closeCheckingAccount(String accountNumber)` | Đóng tài khoản thanh toán khi số dư bằng 0, không có sổ tiết kiệm ACTIVE và khoản vay ACTIVE |
| `reopenCheckingAccount(String accountNumber)` | Kích hoạt lại tài khoản thanh toán cũ, giữ nguyên số tài khoản |

Đã chốt giữ đủ năm phương thức, kể cả `reopenCheckingAccount()`. Hai hàm đóng/mở lại đang thể hiện trên UML bằng tên và tham số; kiểu trả về có thể hoàn thiện khi viết chữ ký Java. Nộp/rút/chuyển khoản không thuộc AccountService.

### SavingsService — 3 phương thức đã chốt

| Phương thức | Kiểu trả về | Trách nhiệm |
| --- | --- | --- |
| `openSavingsAccount(String customerId, BigDecimal amount, int termMonths)` | `SavingsAccount` | Kiểm tra điều kiện, trừ tiền tài khoản thanh toán, tạo sổ và ghi `SAVINGS_OPEN` |
| `settleSavingsAccount(String accountNumber)` | `void` | Tính lãi, hoàn gốc + lãi về tài khoản thanh toán, đưa sổ về `INACTIVE` và ghi `SAVINGS_SETTLEMENT` |
| `settleMaturedSavings()` | `void` | Tìm sổ đến hạn và tự tất toán khi khởi động/trước các thao tác nghiệp vụ; không tất toán lại sổ đã kết thúc |

`SavingsAccount.calculateInterest(onDate)` thực hiện tính lãi theo quy tắc đã chốt; service điều phối nghiệp vụ, không viết lại công thức. Khách xem lãi dự kiến thông qua dữ liệu sổ và phương thức tính lãi của model, không cần thêm hàm riêng vào SavingsService ở phạm vi hiện tại.

### LoanService — 2 phương thức đã chốt

| Phương thức | Kiểu trả về | Trách nhiệm |
| --- | --- | --- |
| `openLoanAccount(String customerId, BigDecimal amount, BigDecimal annualRate, LocalDate endDate)` | `LoanAccount` | Tạo khoản vay, giải ngân vào tài khoản thanh toán và ghi `LOAN_DISBURSEMENT` |
| `repayLoanAccount(String accountNumber)` | `void` | Tính số tiền phải trả, kiểm tra số dư, thu toàn bộ gốc + lãi, đưa dư nợ về 0, chuyển khoản vay sang `INACTIVE` và ghi `LOAN_REPAYMENT` |

`LoanAccount.calculateInterest()` và `calculateTotalDue()` đảm nhiệm tính lãi cố định/tổng nợ; không thêm hàm tính tổng nợ vào service. Giữ chữ ký `openLoanAccount(customerId, amount, annualRate, endDate)` đã chốt: với bản case study, UI/service chỉ cho phép lãi suất 10% và `endDate` tương ứng kỳ hạn 6 hoặc 12 tháng kể từ ngày giải ngân. Quyền trả nợ đã chốt thuộc khách hàng đối với khoản vay của chính mình; mỗi khách tối đa một khoản ACTIVE, được mở khoản mới sau khi khoản cũ INACTIVE, giữ lịch sử khoản cũ. Hai báo cáo được xử lý bởi ReportService.

### TransactionService — đã chốt 3 phương thức

Đã chọn interface `TransactionService` và lớp `TransactionServiceImpl` riêng để xử lý ba nghiệp vụ giao dịch thường: nộp/rút tiền mặt do nhân viên thực hiện; chuyển khoản do khách tự thực hiện. Service kiểm tra điều kiện, cập nhật CheckingAccount và ghi Transaction tương ứng (DEPOSIT, WITHDRAWAL, TRANSFER). Nghiệp vụ mở/tất toán tiết kiệm và giải ngân/trả nợ vẫn do SavingsService/LoanService điều phối, không chuyển sang TransactionService.

| Phương thức đã chốt | Kiểu trả về | Trách nhiệm |
| --- | --- | --- |
| `deposit(String accountNumber, BigDecimal amount)` | `Transaction` | Nộp tiền mặt vào tài khoản thanh toán, ghi DEPOSIT |
| `withdraw(String accountNumber, BigDecimal amount)` | `Transaction` | Rút tiền mặt từ tài khoản thanh toán khi đủ số dư, ghi WITHDRAWAL |
| `transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount)` | `Transaction` | Chuyển tiền giữa hai tài khoản thanh toán hợp lệ, ghi một giao dịch TRANSFER |

Ba hàm trả về chính đối tượng Transaction đã ghi khi thành công để UI có thể hiển thị thông tin giao dịch; khi dữ liệu không hợp lệ hoặc nghiệp vụ thất bại thì báo lỗi, không trả về giao dịch thành công. Không tạo lớp kết quả riêng.

### ReportService — đã chốt interface + Impl và 2 phương thức

Đã chọn **ReportService là interface, ReportServiceImpl là lớp triển khai**, tương tự bốn service còn lại. Hai phương thức đã chốt:

| Phương thức | Kiểu trả về | Trách nhiệm |
| --- | --- | --- |
| `generateAccountReport()` | `String` | Tạo nội dung thống kê tài khoản theo loại/trạng thái; tách tổng dư nợ gốc vay khỏi tiền gửi |
| `generateTransactionReport(LocalDate fromDate, LocalDate toDate)` | `String` | Tạo nội dung thống kê tiền giao dịch trong khoảng ngày, chia bảy nhóm |

**Đã chốt default method** trong `ReportService`: `default String generateTodayTransactionReport()` lấy `LocalDate today = LocalDate.now()` rồi trả `generateTransactionReport(today, today)`. Đây chỉ là phương thức tiện ích cho báo cáo giao dịch hôm nay, **không thêm loại báo cáo thứ ba**; `ReportServiceImpl` không bắt buộc override. ReportService chỉ xây dựng **chuỗi báo cáo**, menu console chịu trách nhiệm hiển thị chuỗi đó. Không in trực tiếp trong service, không tạo DTO/lớp kết quả mới. Kiểu tham số ngày dùng `LocalDate` vì thống kê theo ngày, không yêu cầu thời điểm cụ thể; cách kiểm tra khoảng ngày sẽ được hoàn thiện khi triển khai.

**Đã triển khai đủ 5 interface và 5 lớp Impl** ở `src/main/java/vn/edu/pxu/bank/service/`, hai hàm đóng/mở lại tài khoản trả `void`, `ReportService` có default method và trả chuỗi báo cáo. Mỗi Impl nhận `JsonRepository` qua constructor, không tạo service Singleton. `ServiceSupport` (package-private) hỗ trợ tra cứu, sinh mã, kiểm tra số dư, rollback bộ nhớ khi lưu thất bại và tất toán sổ đến hạn trước các thao tác. Kiểm thử luồng chính ở `src/test/java/vn/edu/pxu/bank/repository/ServiceTest.java`. API hiện không có tham số người thao tác nên **chưa thực hiện xác thực/phân quyền nhân viên-khách hàng**; phần này sẽ được chốt khi kết nối UI hoặc điều chỉnh API. UML trong `uml/` còn cần đồng bộ chữ ký kiểu dữ liệu với mã Java.

## Design Pattern — đã chốt hướng Singleton + Simple Factory

- **Singleton:** `JsonRepository` đã cài constructor private, `static final INSTANCE`, `getInstance()`, `load()/save()` đọc/ghi một file `data/bank-data.json` bằng Gson và có test. Không tạo `BankData` chỉ để làm pattern.
- **Simple Factory (đã triển khai):** 5 lớp `AccountServiceFactory`, `TransactionServiceFactory`, `SavingsServiceFactory`, `LoanServiceFactory`, `ReportServiceFactory`. Mỗi lớp có constructor private và `static create()` không tham số, trả về **interface** tương ứng; lấy `JsonRepository.getInstance()` ngay trong factory. Mỗi factory hiện tạo đúng một `*ServiceImpl`; không tự thêm implementation giả.
- **Phụ thuộc:** Factory gọi `JsonRepository.getInstance()` và truyền vào constructor `*ServiceImpl`; API gọi factory không cần mang tham số singleton. Constructor của các service vẫn nhận repository để kiểm thử và gọi trực tiếp khi cần.
- **Registry DI (đã triển khai theo yêu cầu mới):** `vn.edu.pxu.bank.di.DI` giữ `Map<Class<?>, Object>`, hỗ trợ `register()`, `get()` và `createDefault()` chỉ đăng ký 5 interface service qua Factory; `JsonRepository` đã là Singleton nên không đăng ký trùng vào DI. Tạo một `DI` ở lúc khởi động và dùng chung registry đó. `DI` không phải Singleton và không dùng framework; các service được giữ cùng instance trong từng registry, không tự biến thành Singleton.
- **Ranh giới rubric:** `create()` chỉ bọc `new` là Simple Factory tối thiểu, **chưa chứng minh được logic lựa chọn implementation**. Trước khi đánh dấu hoàn thành tiêu chí Factory cần bổ sung một trường hợp lựa chọn có ý nghĩa và kiểm tra thầy có chấp nhận Simple Factory hay yêu cầu đúng GoF Factory Method. Không tự bịa implementation thứ hai ngay lúc này.

Factory, Singleton và DI registry đã có mã Java, kiểm thử tại `FactoryTest.java` và `DITest.java`; sơ đồ UML service/factory được cập nhật. Factory hiện chỉ bọc việc tạo một implementation nên vẫn là **Simple Factory tối thiểu**, không phải GoF Factory Method. `DI.createDefault()` không gọi `load()`; cần tải file dữ liệu trước khi thực hiện nghiệp vụ.

## Lưu dữ liệu JSON — đã chốt phạm vi file

Đã chọn **một file `data/bank-data.json`** lưu tập trung người dùng, mọi loại BankAccount và Transaction. **Tài khoản được lưu lồng đúng một lần trong `Customer.accounts` của người dùng Customer**, không lưu thêm mảng `accounts` độc lập ở gốc JSON; các giao dịch lưu trong danh sách chung duy nhất, không nhúng lặp vào tài khoản/khách hàng. Không sử dụng ba file `users.json`, `accounts.json`, `transactions.json`. Cách này giữ dữ liệu của một nghiệp vụ (ví dụ chuyển tiền, mở sổ, trả nợ) trong cùng một lần ghi dữ liệu tổng hợp. File đã tồn tại nhưng lỗi/hỏng phải dừng và giữ nguyên, không ghi đè.

Đã chốt **lưu tự động ngay sau mỗi nghiệp vụ thành công có thay đổi dữ liệu** (gồm tạo/sửa khách hàng, cập nhật trạng thái tài khoản, giao dịch, tiết kiệm, vay và tất toán sổ đến hạn nếu có); nghiệp vụ chỉ đọc không cần ghi. Một lần lưu phải bao gồm đồng bộ dữ liệu của nghiệp vụ trong file chung, thay vì lưu riêng từng tài khoản/giao dịch. Đã chọn **Gson** làm thư viện tuần tự hóa/giải tuần tự hóa JSON thay vì tự viết bộ đọc/ghi JSON. **Đã triển khai** `JsonRepository` Singleton tại `src/main/java/vn/edu/pxu/bank/repository/JsonRepository.java`: `getInstance()`, `load()`, `save()`, `getUsers()`, `getTransactions()`. JSON chỉ có hai mảng gốc `users` và `transactions`; từng User/BankAccount có `kind` để khôi phục lớp con; LocalDate/LocalDateTime dạng ISO. `load()` xây lại object qua constructor và kiểm tra dữ liệu; file hỏng hoặc không đọc được thì báo lỗi, chặn `save()` và không ghi đè. `save()` kiểm tra tính duy nhất, ghi file tạm rồi thay thế file chính. Khi file chưa tồn tại, load tạo danh sách rỗng trong RAM nhưng không tự ghi. Chi tiết schema xem `data/README.md`. **Đã tích hợp save sau nghiệp vụ thành công** và `ServiceSupport` khôi phục từ file hiện tại khi lưu lỗi. Cả năm service được truyền `JsonRepository` qua constructor. Không thêm tầng kiến trúc mới.

## Các phần chưa chốt

Các enum trong sơ đồ hiện tại đã có ô riêng với ký hiệu enumeration và danh sách giá trị: UserStatus, UserRole, AccountStatus, AccountType và TransactionType. UserRole/AccountType là kiểu trả về của getRole()/getAccountType(), không thêm trường dữ liệu trùng loại lớp con.

- Chi tiết còn lại của User, Customer và Employee, gồm username có được thay đổi hay không.
- Chi tiết tài khoản thanh toán, sổ tiết kiệm và khoản vay; cây kế thừa BankAccount đã có một cha–ba con.
- Đã chốt một Composition `Customer ◆── BankAccount` với `Customer.accounts` là nguồn dữ liệu tài khoản duy nhất; còn chọn **quan hệ kết tập thứ hai** theo slide 10, không tính `BankAccount.customerId` thành quan hệ riêng. Singleton + Simple Factory đã chốt hướng, vẫn cần viết code và chứng minh Factory có lựa chọn implementation có nghĩa.
- Đã chốt AccountService (5 hàm, có mở lại tài khoản cũ), TransactionService (3 hàm trả về Transaction), SavingsService (3 hàm) và LoanService (2 hàm). ReportService/ReportServiceImpl đã chốt hai hàm `generateAccountReport()` và `generateTransactionReport(fromDate, toDate)`, đều trả về `String`. Còn hoàn thiện kiểu trả về đóng/mở lại tài khoản, chi tiết lưu trữ một file JSON qua Gson, lớp hỗ trợ; default method đã chốt trên UML, chờ triển khai Java.

Tiếp tục theo cách: mô tả một phần, phỏng vấn sinh viên, ghi quyết định rồi chuyển sang phần tiếp theo.
