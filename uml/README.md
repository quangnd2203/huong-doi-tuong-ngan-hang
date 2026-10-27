# Sơ đồ UML

Nguồn Mermaid đang cập nhật: [class-diagram-current.mmd](class-diagram-current.mmd) (và [bản hiển thị Markdown](class-diagram-current.md)). Các ảnh [class-diagram.png](class-diagram.png) và [class-diagram.svg](class-diagram.svg) là bản cũ, chưa có đầy đủ thay đổi về 5 service, Composition Customer–BankAccount, Singleton JsonRepository và 5 Simple Factory; phải xuất lại khi chốt UML và trước khi nộp.

**Hai sơ đồ rút gọn để xem và trao đổi:**

- [UML Model](class-diagram-model.md) — [nguồn Mermaid](class-diagram-model.mmd): User/Customer/Employee, các loại tài khoản, Transaction và Composition Customer–BankAccount.
- [UML Service – Factory – Repository](class-diagram-service-factory.md) — [nguồn Mermaid](class-diagram-service-factory.mmd): 5 interface/Impl, 5 Factory, Singleton JsonRepository, DI registry và default method.

Bản tổng hợp chi tiết vẫn được giữ ở [UML hiện tại](class-diagram-current.md), gồm enum và nhiều phương thức hơn. Hai sơ đồ rút gọn không thay thế bản chi tiết hoặc ảnh PNG/SVG nộp cuối. Đây vẫn là thiết kế đang thảo luận, chưa hoàn thành mốc M2.

- M2: lưu bản thiết kế v1, ví dụ `class-diagram-v1.puml` và ảnh xuất tương ứng.
- M3: lưu v2 có abstract class, ít nhất hai interface và quan hệ đầy đủ.
- Bản nộp cuối: `class-diagram.png`, khớp mã nguồn cuối cùng.
- Có thể dùng PlantUML, draw.io hoặc công cụ phù hợp; giữ file nguồn để chỉnh sửa.

Hiện có [bản phác kế thừa User](user-inheritance-proposal.md) và sơ đồ tổng hợp đang hoàn thiện. Phạm vi đã bổ sung khoản vay. Cây BankAccount có một cha–ba con, đã đáp ứng cấu trúc kế thừa trên UML; quy tắc nghiệp vụ LoanAccount đã chốt, **chưa cài đặt mã nguồn**. Đã chốt một Composition Customer–BankAccount, còn thiếu quan hệ kết tập thứ hai theo slide 10. Singleton + Simple Factory đã có trên UML, chưa có mã Java và chưa chứng minh đủ rubric.

Đọc `docs/MO-TA-DE-TAI.md` và `docs/YEU-CAU-DA-CHOT.md` để xác định thực thể, quyền và các điểm nghiệp vụ còn mở.
