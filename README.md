# Quản lý ngân hàng

Đồ án học phần **Lập trình hướng đối tượng**, PXU University.

| Thông tin | Nội dung |
| --- | --- |
| Sinh viên | Nguyễn Đăng Quang |
| Mã sinh viên | NBS2503ITA0062 |
| Nhóm đăng ký | 5 |
| Hình thức | Cá nhân, đảm nhiệm toàn bộ công việc |
| Giảng viên | Nguyễn Dũng |
| Công nghệ | Java thuần, JDK 17 trở lên, Java Swing desktop |

## Trạng thái

Đã khởi tạo cấu trúc, chương trình chào và phỏng vấn phạm vi: hai vai trò nhân viên/khách hàng, các nhóm khách hàng/tài khoản/giao dịch/tiết kiệm/khoản vay và hai báo cáo thống kê. Đã có bản phác UML; nghiệp vụ vay đã chốt cách tính lãi, quyền trả nợ, giới hạn một khoản vay ACTIVE mỗi khách và bổ sung vào hai báo cáo. Xem [yêu cầu đã chốt](docs/YEU-CAU-DA-CHOT.md) và [UML hiện tại](uml/class-diagram-current.md). **Model, `JsonRepository` và cả 5 service (interface + Impl) đã cài đặt/kiểm thử**, gồm hai báo cáo trả về `String`. Đã có 5 Simple Factory với `create()` không tham số; `DI` là Singleton (`getInstance()`), tự nạp JSON và đăng ký 5 Service trước khi mở Swing. Repository là Singleton và chỉ được tầng DI/Factory/Service truy cập. Đã có **giao diện desktop Swing** với đăng nhập theo vai trò, danh sách tài khoản, giao dịch, tiết kiệm, khoản vay và báo cáo; `Main` chỉ gọi `BankWindow.launch(DI.getInstance())`; việc nạp JSON thuộc DI, vòng đời cửa sổ thuộc UI. Lần chạy đầu tự tạo dữ liệu demo. Đã triển khai thêm/sửa/tra cứu/ngừng hoạt động khách hàng, đặt lại mật khẩu tạm qua AccountService. Phân quyền trực tiếp theo vai trò tại service vẫn chưa hoàn thiện vì API không nhận thông tin người đang đăng nhập.

## Cấu trúc

```text
.
├── .wiki/                         # Tài liệu yêu cầu gốc
├── README.md                      # Thông tin và cách build/chạy
├── pom.xml                        # Maven: Java 17, Gson, chạy Main
├── PHAN-CONG.md                   # Phân công cá nhân, dự kiến đóng góp 100%
├── docs/
│   ├── MO-TA-DE-TAI.md             # Mô tả M1 theo phạm vi đã chọn
│   ├── YEU-CAU-DA-CHOT.md          # Kết quả phỏng vấn và quy tắc còn mở
│   ├── QUY-TAC-NGHIEP-VU.md        # Quy tắc chi tiết đã chọn qua phỏng vấn
│   ├── KE-HOACH.md                 # Các mốc M0–M5
│   ├── CHECKLIST-OOP.md            # Theo dõi đủ 18 yêu cầu
│   ├── KIEM-THU.md                # Các tình huống cần kiểm tra
│   ├── DONG-GOI.md                # Danh mục sản phẩm nộp
│   ├── bao-cao/                   # Bìa, mục lục và 5 chương
│   └── screenshots/               # Ảnh chạy chương trình cho chương 4
├── uml/                           # Ba sơ đồ Mermaid tổng hợp/Model/Service và bản ảnh cần tái xuất
├── src/main/java/vn/edu/pxu/bank/
│   ├── Main.java                  # Điểm khởi chạy
│   ├── model/
│   │   ├── user/                  # User, Customer, Employee, UserStatus, UserRole
│   │   ├── account/               # BankAccount, 3 loại tài khoản, enum
│   │   └── transaction/           # Transaction, TransactionType
│   ├── service/                   # 5 interface + 5 Impl + ServiceSupport
│   ├── di/                        # DI Singleton getInstance/get; nạp JSON và dữ liệu demo
│   ├── repository/                # JsonRepository Singleton, Gson, load/save một JSON
│   ├── exception/                 # NotFoundException, InsufficientBalanceException
│   ├── factory/                   # 5 Factory create() lấy JsonRepository Singleton
│   ├── strategy/                  # Thư mục dự phòng, chưa triển khai Strategy Pattern
│   ├── util/                      # ValidationUtils, PasswordUtils (PBKDF2)
│   └── ui/                        # Swing: BankWindow, EmployeePanel, CustomerPanel, demo
├── src/test/java/vn/edu/pxu/bank/  # Dành cho kiểm thử khi có mã nghiệp vụ
└── data/                          # Dữ liệu mẫu và dữ liệu lưu trữ
```

## Build và chạy

Yêu cầu **JDK 17 trở lên** và **Apache Maven**. Kiểm tra môi trường:

```bash
java -version
javac -version
mvn -version
```

Môi trường đã kiểm tra trong DevSpace: **JDK 21, Maven 3.10.0**. Trên macOS, nếu máy khác chưa có Maven thì cài bằng:

```bash
brew install maven
```

Sau đó, tại **thư mục gốc dự án** (để đường dẫn `data/` hoạt động đúng), build và chạy:

```bash
mvn clean package
mvn exec:java
```

Maven dùng `pom.xml` để biên dịch tương thích **Java 17** (`maven.compiler.release=17`), mã hóa UTF-8 và tự quản lý dependency **Gson 2.15.0**. Không sử dụng framework. Mã nguồn vẫn theo cấu trúc `src/main/java` và `src/test/java`; thư mục kết quả build `target/` đã được `.gitignore` bỏ qua. Lệnh `mvn exec:java` mở **cửa sổ Swing** trên máy có desktop (macOS/Windows/Linux); không cần HTTP server hay Bootstrap. Khi **chưa có `data/bank-data.json`**, chương trình khởi tạo dữ liệu demo một lần: nhân viên `admin` / `Admin@123`, khách hàng `khach` / `Khach@123`, mã KH `KH001`, tài khoản `CK000001` ban đầu số dư 0. Nhân viên có thể nộp tiền để thử giao dịch; dữ liệu thay đổi được lưu vào JSON. Mật khẩu trong JSON được băm PBKDF2, không lưu plaintext. Đây chỉ là tài khoản **demo**, phải thay mật khẩu nếu dùng ngoài môi trường học tập. Để xem màn hình đăng nhập mặc định khi đã có file cũ, dùng file dữ liệu phù hợp; không tự xóa/ghi đè file có dữ liệu. Dùng `mvn clean verify` để build và chạy JUnit (hiện **50 test**), các test dùng file tạm, không ghi vào file demo. Phân quyền hiện giới hạn qua giao diện theo vai trò; vì service API chưa nhận người thao tác nên chưa phải ranh giới phân quyền độc lập ở tầng Service. Giao diện chưa kiểm thử hiển thị trực tiếp trên desktop trong môi trường tự động.

## Phân tầng sau refactor DI

`Main → DI.getInstance() → 5 Service → JsonRepository`. DI là composition root và Singleton, khởi tạo một lần khi gọi `getInstance()`. Demo dữ liệu lần đầu ở `di/DemoData.java`, không nằm ở Swing. `BankWindow.launch()` nhận DI, giữ ứng dụng hoạt động cho đến khi người dùng đóng cửa sổ. **UI không import Repository, không trực tiếp lưu JSON hoặc xử lý băm/xác minh mật khẩu**. Đăng nhập/đổi mật khẩu và tra cứu khách/tài khoản qua `AccountService`; lịch sử qua `TransactionService`; tiền lãi/dư nợ qua `SavingsService` và `LoanService`.

`DITest` kiểm tra cấu trúc Singleton mà không khởi động ứng dụng trên file dữ liệu thật; các test nghiệp vụ dùng thư mục tạm. Phân quyền vai trò trong tầng Service vẫn là công việc còn lại, do Service API chưa nhận thông tin phiên đăng nhập.

## Tra cứu lãi / khoản vay trong GUI

Trong tab **Tài khoản của tôi**, thanh nút chỉ còn **Chuyển khoản** và **Làm mới**. Nhấp chuột phải (hoặc Ctrl+click trên Mac) trực tiếp vào dòng sổ tiết kiệm để chọn **Dự tính lãi tiết kiệm** hoặc **Tất toán tiết kiệm**; nhấp phải dòng khoản vay để chọn **Chi tiết khoản vay** hoặc **Trả nợ vay**. Không cần nhập lại mã tài khoản; hành động luôn sử dụng tài khoản ở dòng đã nhấp. Với sổ/khoản vay `INACTIVE`, mục tất toán/trả nợ bị vô hiệu hóa. Các thao tác vẫn phải vượt qua validation nghiệp vụ ở Service.

Khi dự tính lãi tiết kiệm, để trống ngày dự tính sẽ xem tại **ngày đáo hạn**; có thể nhập ngày từ hôm nay trở đi theo `yyyy-MM-dd`. Trước hạn áp dụng lãi suất rút trước hạn; đúng/sau hạn chỉ tính lãi đến ngày đáo hạn. Đây là **dự tính**, không thực hiện tất toán. **Chi tiết khoản vay** cho biết gốc, lãi suất cố định, kỳ hạn, lãi toàn kỳ và tổng phải trả. Sổ/khoản vay đã `INACTIVE` sẽ hướng người dùng sang lịch sử giao dịch thay vì lấy số dư 0 để tính lãi hoặc nợ. Các số tiền trên bảng và popup dùng dấu chấm ngăn cách hàng nghìn (VND); không thay đổi dữ liệu hay phép tính trong Model.


## Màn nhân viên: thao tác bằng menu chuột phải

Trong tab **Khách hàng & tài khoản**, thanh nút chỉ còn **Thêm khách hàng** và **Làm mới**. Tên đăng nhập mới: 4–20 ký tự, bắt đầu bằng `a-z`, chỉ chứa chữ thường `a-z`, số `0-9`, `_`, không trùng (kể cả khác chữ hoa/thường). Validation nằm ở `AccountServiceImpl`, thông báo lỗi trên GUI. Dữ liệu người dùng đã tạo trước khi bổ sung quy tắc này vẫn được đọc lại bình thường; không tự sửa hoặc xóa tài khoản cũ.

- Chuột phải vào **khách hàng**: xem chi tiết, sửa hồ sơ, mở tài khoản thanh toán, mở tiết kiệm, giải ngân vay, đặt lại mật khẩu, ngừng hoạt động. Các hành động không phù hợp trạng thái bị vô hiệu hóa. Tạo/đặt lại mật khẩu sinh mật khẩu tạm ngẫu nhiên, hiển thị một lần cho nhân viên trong ô văn bản có thể bôi đen, kèm nút **Sao chép**, và yêu cầu khách đổi khi đăng nhập.
- Chuột phải vào **tài khoản CHECKING**: xem chi tiết, lịch sử giao dịch, **nộp tiền mặt, rút tiền mặt**, đóng hoặc kích hoạt lại. Nộp/rút chỉ hỏi số tiền vì đã lấy mã tài khoản từ dòng được nhấp; chỉ hoạt động khi tài khoản và khách hàng ACTIVE. Service kiểm tra số tiền nguyên dương và số dư đủ khi rút.
- Chuột phải vào **SAVINGS**: xem chi tiết, dự tính lãi theo ngày, tất toán. Nhân viên và khách hàng đều được tất toán sổ đang ACTIVE.
- Chuột phải vào **LOAN**: xem chi tiết, xem dư nợ và tổng phải trả. Nhân viên không có thao tác trả nợ.

Menu lấy **mã trực tiếp từ dòng được nhấp**, kể cả khi sắp xếp lại bảng. Không hỏi nhập lại mã đối tượng. Các điều kiện nghiệp vụ vẫn được kiểm tra ở Service. Chỉ giữ tab **Khách hàng & tài khoản** và **Báo cáo**; các nghiệp vụ nộp/rút, tiết kiệm và khoản vay thao tác trực tiếp qua menu chuột phải, không có các tab nút trùng lặp.

Để tránh classloader của `mvn exec:java` đóng trước khi Swing kết thúc, `BankWindow.launch()` giữ luồng khởi động đến khi cửa sổ được đóng. `Main` không quản lý luồng Swing. Hãy build và chạy lại khi thay đổi GUI.

## UML và ảnh nộp

Các nguồn Mermaid, bản Markdown và **6 ảnh PNG/SVG** (tổng hợp, Model, Service/DI) đã cập nhật theo mã Java. Xem [hướng dẫn UML](uml/README.md). Khi đưa vào báo cáo nên dùng SVG để hình lớn không mất nét.

## Bắt đầu làm

1. Phạm vi đã chọn được ghi trong [mô tả đề tài](docs/MO-TA-DE-TAI.md); dàn trang một trang trước khi nộp M1.
2. Vẽ UML v1 trong [uml](uml/README.md), giải thích các quan hệ.
3. Triển khai từng phần theo [kế hoạch](docs/KE-HOACH.md), cập nhật [18 tiêu chí](docs/CHECKLIST-OOP.md) bằng đường dẫn mã thật.
4. Lưu bằng chứng kiểm thử và hoàn thiện [báo cáo](docs/bao-cao/README.md).

Nguồn yêu cầu: `.wiki/01-Do-an-nhom.pptx`, đặc biệt slide 9–13, 20–23 và 26–30. Thông tin cá nhân lấy từ `.wiki/image.png`. Slide nhắc tới `DO-AN-NHOM.md` và `HUONG-DAN-VSCODE.md`, hiện hai file này chưa có trong `.wiki`.

Yêu cầu cá nhân của dự án áp dụng cho bảng phân công. Các tiêu chí kỹ thuật và yêu cầu Git của slide vẫn được giữ để theo dõi. Lịch sử Git local được quản lý trong repository hiện tại; kiểm tra remote GitHub và số lượng commit trực tiếp bằng git log/git remote trước khi nộp.
