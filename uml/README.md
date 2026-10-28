# Sơ đồ UML — Project NBS

Các sơ đồ **Mermaid đã đồng bộ với mã Java hiện tại**:

| Sơ đồ | Mermaid nguồn | Bản Markdown xem trực tiếp |
| --- | --- | --- |
| Tổng hợp | [class-diagram-current.mmd](class-diagram-current.mmd) | [class-diagram-current.md](class-diagram-current.md) |
| Model | [class-diagram-model.mmd](class-diagram-model.mmd) | [class-diagram-model.md](class-diagram-model.md) |
| Service/DI | [class-diagram-service-factory.mmd](class-diagram-service-factory.mmd) | [class-diagram-service-factory.md](class-diagram-service-factory.md) |

## Các quan hệ

- **Kế thừa:** User → Customer, Employee; BankAccount → CheckingAccount, SavingsAccount, LoanAccount.
- **Interface realization:** năm ServiceImpl triển khai năm interface Service tương ứng.
- **Composition (hình thoi đặc):** Customer sở hữu các tài khoản BankAccount qua danh sách accounts, xác nhận customerId khi thêm.
- **Aggregation (hình thoi rỗng):** JsonRepository quản lý hai tập hợp User và Transaction trong bộ nhớ; các đối tượng được lưu/khôi phục từ một JSON và không có vòng đời chặt như tài khoản thuộc Customer.
- **Dependency (nét đứt):** DI dùng Factory; UI chỉ dùng Service. Transaction lưu số tài khoản dưới dạng String, không chứa object BankAccount.

## Kiến trúc khởi động

Main.main() → DI.getInstance() → JsonRepository.load() và DemoData nếu cần → năm Factory tạo năm Service → xử lý tiết kiệm đến hạn → BankWindow.launch(di). DI là **lazy Singleton**, register là private, không tồn tại createDefault().

**Design Pattern** gồm JsonRepository/DI Singleton và năm Simple Factory; không gọi nhầm Simple Factory là GoF Factory Method. ReportService có default method generateTodayTransactionReport().

## Xuất ảnh khi đóng gói

**Đã xuất đủ 6 ảnh PNG/SVG** từ ba nguồn Mermaid: [tổng hợp PNG](class-diagram.png) / [SVG](class-diagram.svg), [Model PNG](class-diagram-model.png) / [SVG](class-diagram-model.svg), [Service/DI PNG](class-diagram-service-factory.png) / [SVG](class-diagram-service-factory.svg). Các ảnh này khớp bản .mmd hiện tại; ưu tiên SVG cho tài liệu lớn để không mất nét.

Có thể dùng Mermaid CLI để xuất lại trên máy với Node:

    npx -y @mermaid-js/mermaid-cli -i uml/class-diagram-current.mmd -o uml/class-diagram.svg -b white --size 3400
    npx -y @mermaid-js/mermaid-cli -i uml/class-diagram-current.mmd -o uml/class-diagram.png -b white --size 3400 -s 2

Kiểm tra ảnh kết quả, nhất là kích thước chữ và các đường nối. [Bản phác kế thừa User](user-inheritance-proposal.md) chỉ để tham khảo lịch sử thiết kế.
