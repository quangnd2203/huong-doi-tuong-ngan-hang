# Kế hoạch kiểm thử

Chưa thực hiện các ca nghiệp vụ. Bổ sung đầu vào, kết quả mong đợi theo quy tắc đã chốt và kết quả thực tế khi có chương trình.

| Nhóm | Tình huống cần kiểm tra | Kết quả thực tế |
| --- | --- | --- |
| Console | Nhập chữ vào ô số, dòng rỗng, lựa chọn ngoài menu, kết thúc luồng nhập | Chưa chạy |
| Đăng nhập và phân quyền | Sai mật khẩu, đổi mật khẩu, đúng menu theo vai trò, khách chỉ xem/thao tác trên dữ liệu của mình | Chưa chạy |
| Khách hàng | Tạo hợp lệ, trùng mã, dữ liệu không hợp lệ, tìm mã không tồn tại; không cho ngừng hoạt động khi còn khoản vay ACTIVE | Chưa chạy |
| Tài khoản | Tạo từng loại, trùng mã, khách hàng không tồn tại; không đóng CheckingAccount khi còn LoanAccount ACTIVE dù số dư bằng 0; chỉ đóng khi đủ các điều kiện | Chưa chạy |
| Giao dịch | Gửi/rút/chuyển hợp lệ; tiền âm/0; thiếu số dư; mã không tồn tại | Chưa chạy |
| Chuyển khoản | Chuyển cùng tài khoản; lỗi giữa chừng; tổng tiền trước/sau đúng quy tắc phí | Chưa chạy |
| Tiết kiệm | Nhân viên mở sổ/tất toán; khách xem sổ và lãi dự kiến; kiểm tra kỳ hạn/lãi sau khi chốt quy tắc | Chưa chạy |
| Khoản vay | Nhân viên giải ngân; khách tự trả khoản vay của mình; không được trả khoản của khách khác; không đủ tiền thì báo lỗi; trả thành công đưa balance về 0 và status thành INACTIVE; không mở khoản thứ hai khi đang có khoản ACTIVE; được vay tiếp sau tất toán, lịch sử khoản cũ vẫn còn | Chưa chạy |
| Báo cáo | Hai phương thức ReportService trả về String (không in trực tiếp); kiểm tra dữ liệu trống, nhiều loại tài khoản, khoảng ngày hợp lệ/không hợp lệ; tách dư nợ vay khỏi tiền gửi, số khoản vay ACTIVE/INACTIVE; 7 nhóm giao dịch, mỗi giải ngân/trả nợ tính một lần, không cộng trùng nộp/rút/chuyển | Chưa chạy |
| Lưu trữ | Dùng Gson lưu/đọc toàn bộ người dùng/tài khoản/giao dịch trong đúng một file `data/bank-data.json`; đọc lại đúng các lớp con của User/BankAccount, BigDecimal và LocalDate/LocalDateTime; mỗi nghiệp vụ sửa dữ liệu thành công được ghi ngay, không cần thoát chương trình; tra cứu không ghi file; sổ tiết kiệm tự tất toán thì phải ghi; khởi động lại không mất dữ liệu; file chưa có; file hỏng/không đọc được phải giữ nguyên và không ghi đè; lỗi ghi không được làm mất dữ liệu đang có hoặc tạo số dư và giao dịch thiếu nhất quán | Chưa chạy |
| Đa hình | Collection kiểu cha chứa nhiều loại con theo UML đã chốt, gọi chung phương thức | Chưa chạy |

Ảnh minh chứng đặt trong `docs/screenshots/`. Cập nhật kết quả và liên kết ảnh vào chương 4, không đánh dấu đạt khi mới có kế hoạch.
