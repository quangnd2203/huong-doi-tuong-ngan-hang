# Quy tắc nghiệp vụ đã chốt

Các quy tắc dưới đây do sinh viên lựa chọn trong phỏng vấn M2. Lãi suất là dữ liệu mô phỏng cho đồ án, không phải thông tin lãi suất ngân hàng thực tế. Chưa có mã nghiệp vụ triển khai.

## Tiết kiệm

| Nội dung | Quy tắc đã chọn |
| --- | --- |
| Kỳ hạn | 3 tháng và 12 tháng |
| Số tiền mở sổ tối thiểu | 1.000.000 đồng |
| Lãi suất kỳ hạn 3 tháng | 3%/năm |
| Lãi suất kỳ hạn 12 tháng | 6%/năm |
| Lãi suất tất toán trước hạn | 0,2%/năm, áp dụng toàn bộ thời gian thực gửi |
| Cố định lãi suất | Lưu cả lãi suất kỳ hạn và lãi trước hạn lúc mở sổ; thay đổi cấu hình chỉ áp dụng sổ mới |
| Công thức | Tiền lãi = tiền gốc × lãi suất năm × số ngày gửi / 365 |
| Mở sổ | Nhân viên thực hiện, trừ tiền từ tài khoản thanh toán của khách |
| Tất toán trước hạn | Cho phép tất toán toàn bộ, trả gốc và lãi trước hạn vào tài khoản thanh toán đó |
| Đến hạn | Chương trình tự trả gốc và lãi vào tài khoản thanh toán khi kiểm tra sổ đến hạn |
| Thời điểm kiểm tra đến hạn | Khi khởi động và trước mỗi thao tác nghiệp vụ |
| Phát hiện sổ quá hạn | Chỉ tính lãi đến ngày đến hạn, không tính thêm đến ngày kiểm tra |
| Tra cứu | Khách xem sổ và lãi dự kiến |

Khi đưa tỷ lệ vào công thức, 3% tương ứng 0,03; 6% tương ứng 0,06; 0,2% tương ứng 0,002.

Tiền nhập là số nguyên đồng VND. Tiền lãi làm tròn đến đồng, phần lẻ từ 0,5 đồng làm tròn lên.

## Quy ước ngày tiết kiệm

- Ngày đến hạn bằng ngày mở cộng 3 hoặc 12 tháng theo lịch.
- Nếu tháng đích không có ngày tương ứng, lấy ngày cuối cùng của tháng đích.
- Số ngày tính lãi bằng chênh lệch giữa ngày mở và ngày tính lãi phù hợp. Mở và tất toán cùng ngày được 0 ngày lãi.
- Tất toán trước hạn tính đến ngày tất toán; đúng hoặc quá hạn chỉ tính đến ngày đến hạn.

## Giao dịch và tài khoản

Khách tự chuyển khoản; nhân viên xử lý nộp/rút tại quầy. Theo lựa chọn GUI mới, **cả nhân viên và khách hàng đều được tất toán sổ tiết kiệm** đang ACTIVE; nhân viên không trả nợ thay khách. Các thao tác nhân viên dùng menu chuột phải theo dòng khách hàng/tài khoản, không nhập lại mã.

- Miễn phí cả nộp tiền, rút tiền và chuyển khoản.
- Chưa đặt hạn mức riêng theo giao dịch hoặc theo ngày.
- Số tiền giao dịch phải dương; tài khoản phải đủ số dư khi trừ tiền.
- Một khách có một tài khoản thanh toán và có thể có nhiều sổ tiết kiệm. Đã chọn `Customer.accounts` chứa các tài khoản thuộc đúng khách đó theo Composition; mỗi BankAccount chỉ có một chủ sở hữu, `customerId` phải khớp chủ sở hữu và tài khoản không được lưu trùng trong nhiều Customer.
- Nhân viên chỉ được đóng tài khoản thanh toán khi số dư bằng 0, không còn sổ tiết kiệm ACTIVE và không còn khoản vay ACTIVE; giữ lịch sử giao dịch.
- Khách quay lại được kích hoạt lại tài khoản thanh toán cũ, giữ nguyên số tài khoản.
- Hồ sơ khách hàng luôn được giữ lại; chỉ ngừng hoạt động sau khi đã xử lý hết tài khoản, sổ tiết kiệm và không còn khoản vay ACTIVE.
- Không sửa/xóa giao dịch đã hoàn tất. Nếu làm sai, nhân viên thực hiện giao dịch thông thường và ghi chú mã giao dịch sai; giữ lịch sử cả hai.

Chưa làm chức năng bù chuyên biệt hoặc tự động hoàn tiền. Giao dịch điều chỉnh vẫn tuân thủ quy tắc giao dịch thông thường, gồm kiểm tra số dư khi trừ tiền. Không mở rộng quyền nhân viên sang chuyển khoản nếu chưa được chọn.

## Sửa hồ sơ khách hàng

- Khách tự sửa số điện thoại, email và địa chỉ của chính mình.
- Nhân viên được sửa các trường trên và họ tên khách hàng.
- Mã khách hàng và số tài khoản giữ nguyên.

## Tra cứu và báo cáo

- Nhân viên được xem hai báo cáo đã chọn và tra cứu dữ liệu toàn hệ thống.
- Khách hàng chỉ được xem dữ liệu của chính mình.
- Báo cáo tài khoản thống kê tất cả theo loại và trạng thái: tài khoản thanh toán ACTIVE/INACTIVE, sổ tiết kiệm đang gửi/đã tất toán, khoản vay ACTIVE/INACTIVE. Với khoản vay, thống kê riêng số khoản vay và **tổng dư nợ gốc** (cộng LoanAccount.balance), không cộng dư nợ vào tổng tiền gửi.
- Báo cáo giao dịch theo khoảng ngày có **bảy nhóm riêng**: nộp tiền mặt, rút tiền mặt, chuyển khoản, mở sổ tiết kiệm, tất toán tiết kiệm, giải ngân khoản vay và trả nợ vay.
- Mỗi chuyển khoản chỉ tính một lần, không cộng hai lần theo bên gửi/bên nhận.
- Giao dịch sửa sai bằng thao tác thông thường vẫn được tính vào loại giao dịch tương ứng.
- Không gộp dòng tiền mở/tất toán tiết kiệm vào tổng nộp/rút tiền mặt hoặc chuyển khoản.
- Giao dịch tất toán tiết kiệm lưu amount bằng tổng gốc + lãi thực trả vào tài khoản thanh toán; note ghi chi tiết gốc/lãi, không thêm trường lãi riêng. Tổng tiền tất toán trong báo cáo được tính bằng tổng amount của nhóm SAVINGS_SETTLEMENT.
- Báo cáo giao dịch tính giải ngân (`LOAN_DISBURSEMENT`, amount = tiền gốc) và trả nợ (`LOAN_REPAYMENT`, amount = tổng gốc + lãi) thành hai nhóm độc lập, mỗi giao dịch tính một lần; không gộp vào nộp/rút/chuyển khoản thông thường.

## Đăng nhập và lưu dữ liệu

- Chuẩn bị sẵn tài khoản nhân viên demo để có thể dùng thử chương trình.
- Đã chọn lưu tập trung trong **một file `data/bank-data.json`** chứa người dùng (Customer chứa `accounts` lồng bên trong, không lưu thêm danh sách accounts cấp gốc), các loại tài khoản và danh sách giao dịch chung (không nhân đôi giao dịch nguồn/đích). Không chia file JSON theo từng loại; **đã chọn thư viện Gson** để đọc/ghi JSON, không tự xây parser hay ghép JSON thủ công. Chưa chốt schema hoặc cấu hình chuyển đổi lớp kế thừa và kiểu ngày.
- Tên đăng nhập khách hàng **mới** phải dài 4–20 ký tự, bắt đầu bằng chữ thường `a-z`, chỉ dùng `a-z`, `0-9`, `_`, không trùng (không phân biệt hoa/thường khi kiểm tra trùng). Loại bỏ khoảng trắng thừa đầu/cuối; không chấp nhận dấu cách ở giữa, dấu tiếng Việt hoặc ký tự đặc biệt. Không áp quy tắc mới hồi tố vào file JSON cũ để tránh khiến tài khoản đã tạo trước đó không thể nạp.
- Khách quên mật khẩu: nhân viên đặt lại mật khẩu tạm; khách bắt buộc đổi khi đăng nhập lại.
- File `data/bank-data.json` đã có nhưng hỏng hoặc không đọc được: báo lỗi rõ ràng, dừng sử dụng dữ liệu, giữ nguyên file; không tự tạo dữ liệu mới ghi đè file đó.
- **Tự động lưu sau mỗi nghiệp vụ thành công có thay đổi dữ liệu**, không chờ đến khi thoát. Bao gồm cập nhật thông tin/đăng nhập, mở/đóng tài khoản, nộp/rút/chuyển tiền, mở/tất toán tiết kiệm, giải ngân/trả nợ. Kiểm tra tất toán sổ đến hạn cũng lưu nếu phát sinh thay đổi; thao tác chỉ xem/tra cứu không ghi lại file.
- Khi ghi phải nhất quán tài khoản và giao dịch của cùng nghiệp vụ trong một lần lưu dữ liệu tổng hợp; chi tiết ghi file an toàn (file tạm/thay thế) và cách xử lý khi lưu thất bại sẽ chọn khi triển khai.

## Khoản vay

- LoanAccount.balance là dư nợ gốc dương, không lưu số âm và không cộng chung với tiền gửi trong báo cáo.
- Nhân viên giải ngân tiền vào tài khoản thanh toán của khách, đồng thời ghi nhận dư nợ gốc.
- Khách hàng đăng nhập tự trả khoản vay của chính mình từ tài khoản thanh toán. Phải đủ tổng gốc+lãi; khi thanh toán thành công, trừ tiền, đưa dư nợ về 0 và khoản vay tự chuyển sang INACTIVE, không cần thao tác đóng riêng.
- Kỳ hạn vay 6 hoặc 12 tháng; lãi suất cố định 10%/năm (annualRate = 0,10) được lưu lúc giải ngân.
- Giữ startDate và endDate là ngày bắt đầu/kết thúc kỳ hạn vay; endDate = startDate.plusMonths(6 hoặc 12). Không lưu thêm termMonths trong LoanAccount. Khi cần xác định kỳ hạn, đối chiếu endDate với startDate.plusMonths(6) hoặc startDate.plusMonths(12) để xử lý đúng cả ngày cuối tháng.
- Lãi vay cố định cho cả kỳ: lãi = dư nợ gốc × 0,10 × số tháng kỳ hạn / 12. Làm tròn đến đồng theo HALF_UP. Trả một lần toàn bộ gốc và lãi, không tính lại theo ngày thanh toán thực tế.
- Không phân biệt trả trước hay sau ngày kết thúc kỳ hạn: tổng tiền phải trả không đổi; không thêm lãi quá hạn hoặc phí phạt.
- TransactionType bổ sung LOAN_DISBURSEMENT và LOAN_REPAYMENT. Giao dịch giải ngân ghi tiền gốc, giao dịch trả nợ ghi tổng gốc+lãi và ghi chi tiết vào note.
- Công thức tính lãi và tổng phải trả nằm trong LoanAccount (`calculateInterest()` và `calculateTotalDue()`), không ở LoanService. Mỗi khách chỉ có tối đa **một khoản vay ACTIVE tại một thời điểm**. Sau khi trả hết và khoản cũ thành INACTIVE, khách được mở khoản vay mới. Giữ nguyên khoản vay cũ và các giao dịch để tra cứu lịch sử. Khoản vay được thống kê riêng trong hai báo cáo hiện có (dư nợ theo loại tài khoản; giải ngân/trả nợ theo loại giao dịch), không tạo báo cáo thứ ba. Không tự động thu nợ.

## Trạng thái đặc tả

Đã chốt nhân viên giải ngân, khách tự trả khoản vay của mình; mỗi khách tối đa một khoản vay đang ACTIVE, được vay lại sau khi trả hết và vẫn giữ lịch sử khoản cũ. Trả hết thì khoản vay tự đóng. Khi còn khoản vay ACTIVE, **không cho đóng tài khoản thanh toán hoặc ngừng hoạt động hồ sơ khách hàng**, kể cả số dư thanh toán bằng 0. Phạm vi báo cáo khoản vay đã chốt là bổ sung vào hai báo cáo hiện có.

Đã ghi nhận các lựa chọn nghiệp vụ và có bản phác UML đang thảo luận; chưa hoàn thành UML v1 hoặc triển khai mã nghiệp vụ. Đã chốt Gson, một file `data/bank-data.json` và tự lưu sau từng nghiệp vụ làm thay đổi dữ liệu; còn mô tả schema JSON, cách đọc lớp con/kiểu ngày, định dạng mã, lưu mật khẩu, xử lý lỗi ghi và file chưa tồn tại trước khi triển khai phần liên quan. Báo cáo tổng tiền tất toán đã chốt dùng tổng gốc+lãi.
