# M1 — Mô tả đề tài: Quản lý ngân hàng

**Sinh viên:** Nguyễn Đăng Quang — NBS2503ITA0062, nhóm 5, làm cá nhân.

## Bối cảnh và mục tiêu

Xây dựng chương trình console mô phỏng quản lý ngân hàng phục vụ học phần Lập trình hướng đối tượng. Chương trình chạy local, có đăng nhập bằng tên tài khoản và mật khẩu, phân quyền nhân viên/khách hàng, sẵn tài khoản nhân viên demo; lưu dữ liệu JSON để sử dụng lại sau khi khởi động.

## Các nhóm chức năng đã chọn

| Nhóm | Chức năng |
| --- | --- |
| Khách hàng | Nhân viên tạo, quản lý, tìm kiếm hồ sơ; ngừng hoạt động khi đủ điều kiện, giữ hồ sơ |
| Tài khoản | Nhân viên tạo tài khoản đăng nhập và tài khoản ngân hàng, đóng/kích hoạt lại tài khoản cũ, đặt lại mật khẩu tạm; khách đổi mật khẩu, xem số dư |
| Giao dịch | Nhân viên xử lý nộp/rút tại quầy; khách tự chuyển khoản và xem lịch sử |
| Tiết kiệm | Nhân viên mở sổ, tất toán trước hạn; chương trình tự tất toán sổ đến hạn khi kiểm tra; khách xem sổ và lãi dự kiến |
| Khoản vay | Nhân viên giải ngân, khách tự trả hết gốc+lãi một lần; mỗi khách tối đa một khoản ACTIVE, được vay lại sau khi trả hết |

Chỉ đóng tài khoản thanh toán khi số dư bằng 0, không còn sổ tiết kiệm ACTIVE hoặc khoản vay ACTIVE. Giao dịch đã hoàn tất không sửa/xóa; sai sót xử lý bằng giao dịch thông thường có ghi chú mã giao dịch sai, giữ lịch sử. Khách sửa điện thoại/email/địa chỉ; nhân viên được sửa thêm họ tên. Mã khách hàng và số tài khoản giữ nguyên.

## Hai báo cáo đã chọn

1. Tổng số tài khoản theo loại và trạng thái; tổng số dư tiền gửi và tổng dư nợ gốc khoản vay thống kê riêng.
2. Tổng tiền giao dịch theo khoảng ngày, tách nộp/rút/chuyển, mở/tất toán tiết kiệm và giải ngân/trả nợ vay (7 nhóm); mỗi giao dịch tính một lần.

## Thực thể và hướng thiết kế

Các thực thể dự kiến gồm khách hàng, người dùng đăng nhập, tài khoản ngân hàng, giao dịch, sổ tiết kiệm và khoản vay. Cây BankAccount có ba lớp con CheckingAccount, SavingsAccount và LoanAccount. Kết tập/Composition và hai interface tiếp tục chốt khi vẽ UML. Hướng mẫu thiết kế dự kiến: Factory tạo loại tài khoản và Strategy tính lãi đúng hạn/trước hạn.

## Giới hạn và điều cần chốt

Java thuần, JDK 17+, giao diện console, lưu JSON và được dùng thư viện JSON đơn giản. File đã có nhưng hỏng/không đọc được phải được giữ nguyên, báo lỗi và dừng sử dụng dữ liệu. Phạm vi đã bổ sung khoản vay, chưa có giao dịch trực tuyến hay tích hợp dịch vụ ngân hàng. GitHub để sau theo lựa chọn của sinh viên, vẫn theo dõi yêu cầu Git trong checklist học phần.

Phạm vi M1 và các quy tắc nghiệp vụ chính đã được sinh viên lựa chọn qua phỏng vấn. Chi tiết lưu trong [QUY-TAC-NGHIEP-VU.md](QUY-TAC-NGHIEP-VU.md). Bước tiếp theo là thiết kế UML v1 và đặc tả các quy ước kỹ thuật còn mở; bản M1 cần dàn thành một trang trước khi nộp.
