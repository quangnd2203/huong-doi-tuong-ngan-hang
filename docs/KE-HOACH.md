# Kế hoạch thực hiện cá nhân

Thời điểm tương đối lấy từ slide 23. Chưa có lịch học cụ thể nên chưa gán ngày dương lịch.

| Mốc | Hạn theo slide | Sản phẩm | Trọng số | Trạng thái |
| --- | --- | --- | --- | --- |
| M0 | Cuối Buổi 1 | Danh sách và đề tài dự kiến | Điều kiện | Có ảnh đăng ký trong `.wiki`, chưa xác minh việc nộp |
| M1 | Trước Buổi 3 | Mô tả đề tài một trang | 5% | Đã có nội dung tại `docs/MO-TA-DE-TAI.md`; cần dàn đúng 1 trang và xác minh đã nộp |
| M2 | Trước Buổi 4 | UML v1, có kế thừa và kết tập | 10% | Đã chốt quy tắc vay, 5 interface/service và bản Mermaid tổng hợp; Đã chốt 1 Composition Customer–BankAccount; còn quan hệ kết tập thứ hai, chưa đóng băng UML v1; PNG/SVG hiện đã cũ |
| M3 | Trước Buổi 5 | UML v2 có abstract class, hai interface và mã nguồn phần khung | 10% | UML đã phác abstract class và 5 interface; chưa có mã lớp/service, UML v2 hoặc chứng cứ nộp |
| M4 | Một tuần sau Buổi 5 | Toàn bộ sản phẩm ZIP và link GitHub | — | Chưa làm |
| M5 | Hai tuần sau Buổi 5 | Bảo vệ đồ án | 75% | Chưa làm |

## Thứ tự làm — đối chiếu trực tiếp PowerPoint 37 slide

1. **Chốt thiết kế còn thiếu (slide 9–11, 23, 27):** quan hệ kết tập thứ hai (đã chốt một Composition Customer–BankAccount); đã chọn Singleton JsonRepository + Simple Factory cho service, còn hoàn thiện logic lựa chọn của Factory để chứng minh rubric; đã chốt `ReportService.generateTodayTransactionReport()` là default method; rà các lớp và UML v1/v2. Hoàn thiện quy ước `User`, đăng nhập, ngoại lệ, các loại tài khoản; không mở rộng nghiệp vụ ngoài phạm vi.
2. **Triển khai bộ khung M3 và mã OOP (slide 9–11):** model, constructor/validation, kế thừa/ghi chồng, collection đa hình, 5 interface + Impl, ≥ 2 nhóm overload, static/final và ≥ 2 exception tự định nghĩa. Có mã thật mới cập nhật checklist là hoàn thành.
3. **Hoàn thiện 5 nhóm nghiệp vụ và 2 báo cáo (slide 12):** menu nhân viên/khách, tài khoản/giao dịch/tiết kiệm/vay, tìm kiếm, tra cứu, sửa/ngừng hoạt động phù hợp; báo cáo tài khoản và báo cáo giao dịch 7 nhóm. Kiểm tra nhập sai không crash.
4. **Lưu trữ và chạy lại (slide 11, 13):** dùng Gson với một file `data/bank-data.json`, lưu khi thay đổi dữ liệu, đọc đúng kiểu lớp con/LocalDate; chốt chính sách ghi lỗi và file tạm, thêm dữ liệu giả lập, kiểm tra khởi động lại.
5. **Kiểm thử + bằng chứng (slide 11–12, 21, 24):** test luồng chính, trường hợp lỗi, kiểm tra 18 tiêu chí trong lớp/file thật, chụp ảnh màn hình, xuất lại UML PNG khớp code. ≥ 10 JUnit 5 test có ý nghĩa là điểm thưởng tùy chọn (slide 30).
6. **Sản phẩm nộp (slide 20–21):** dàn M1 đúng một trang; hoàn thiện `BAO-CAO.pdf` 10–20 trang/5 chương có bảng đối chiếu 18 mục; `PHAN-CONG.xlsx`, README hướng dẫn chạy thật, dữ liệu mẫu, UML PNG, mã nguồn, ZIP `OOP_Nhom05_QuanLyNganHang.zip`; video 3–5 phút chỉ là tùy chọn.
7. **Git và bảo vệ (slide 11, 23–24, 30, 35):** tạo repo GitHub, viết ≥ 25 commit thật có ý nghĩa trải đều tiến độ (không dồn cuối); kiểm tra nộp M1–M3, nộp ZIP + link GitHub M4; luyện thuyết trình 5 phút, demo 5 phút gồm dữ liệu nhập sai, vấn đáp 8 phút, nhận xét 2 phút.

**Lưu ý riêng:** Slide 5 yêu cầu nhóm 4–5 thành viên, trong khi project này làm cá nhân. Cần xác minh giảng viên đã duyệt ngoại lệ và cách chấm đóng góp; không tự nhận đã được duyệt. Slide 3/36 dẫn đến `DO-AN-NHOM.md` và `HUONG-DAN-VSCODE.md` nhưng hiện hai file này chưa có trong project, nên việc đối chiếu dựa trên 37 slide PowerPoint đang lưu.

## Git và bảo vệ

- Lựa chọn hiện tại: làm local trước, chưa tạo Git hoặc GitHub. Thực hiện phần Git sau theo yêu cầu sinh viên.
- Theo slide: repo GitHub, ít nhất 25 commit có nội dung rõ ràng, trải đều quá trình làm. Ghi nhận thay đổi thực tế, không tạo commit rỗng để đủ số.
- Một người đảm nhiệm toàn bộ các vai trò trong `PHAN-CONG.md`.
- Khung bảo vệ theo slide: 5 phút trình bày, 5 phút demo, 8 phút vấn đáp, 2 phút nhận xét.
- Chuẩn bị tình huống nhập sai, giải thích các quan hệ UML, đa hình, hai mẫu thiết kế và phần lưu file.
