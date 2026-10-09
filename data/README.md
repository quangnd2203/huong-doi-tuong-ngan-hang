# Dữ liệu JSON

Ứng dụng lưu dữ liệu trong **một file duy nhất**: `data/bank-data.json`. File được đọc/ghi bằng Gson qua `JsonRepository`; không tách file theo thực thể và không lưu trùng tài khoản.

## Cấu trúc file

Dữ liệu có hai mảng cấp gốc:

```json
{
  "users": [
    {
      "kind": "CUSTOMER",
      "userId": "U01",
      "customerId": "KH01",
      "accounts": [
        {
          "kind": "CHECKING",
          "accountNumber": "CK01",
          "customerId": "KH01"
        }
      ]
    },
    {
      "kind": "EMPLOYEE",
      "userId": "E01"
    }
  ],
  "transactions": [
    {
      "transactionId": "TX01",
      "type": "DEPOSIT",
      "amount": 200000,
      "toAccountNumber": "CK01"
    }
  ]
}
```

Đây là **minh họa cấu trúc**, không phải dữ liệu đầy đủ để nạp: Gson còn ghi các trường còn lại trong Model (ví dụ username, passwordHash, balance, status, occurredAt). Các trường `kind` giúp phục hồi đúng `Customer`/`Employee` và `CheckingAccount`/`SavingsAccount`/`LoanAccount`. `Customer.accounts` là nơi duy nhất chứa tài khoản; `transactions` là danh sách chung, một bản ghi mỗi nghiệp vụ. Ngày được lưu ở định dạng ISO.

## Quy trình sử dụng

`JsonRepository.getInstance().load()` cần chạy thành công trước khi truy cập danh sách dữ liệu. Nếu file **chưa tồn tại**, bản thân repository khởi tạo danh sách rỗng trong bộ nhớ và **không tự ghi file**. Khi chạy ứng dụng GUI từ `Main`, `DemoData.initialize()` tạo một nhân viên, một khách hàng với một tài khoản thanh toán rồi `save()` **chỉ trong lần đầu file chưa tồn tại**. Nếu file đã có mà hỏng, `load()` báo lỗi và ứng dụng không ghi đè.

**Năm service đã được triển khai**: mỗi nghiệp vụ thành công sửa dữ liệu gọi **`save()` một lần**, ghi chung số dư/tài khoản và Transaction. Tất toán nhiều sổ đến hạn được gộp trong một lần lưu. Repository không tự theo dõi mọi thay đổi trực tiếp vào model/danh sách; ứng dụng cần thực hiện thay đổi thông qua service hoặc tự gọi `save()` khi tạo dữ liệu demo.

Mỗi `save()` kiểm tra trùng mã/tên đăng nhập/số tài khoản và ghi toàn bộ users cùng transactions vào file tạm UTF-8, sau đó thay file chính (ưu tiên atomic move; nếu hệ thống file không hỗ trợ thì dùng move thay thế). Khi ghi thất bại, repository báo lỗi; `ServiceSupport` gọi `repository.reload()` để khôi phục bộ nhớ từ đúng file đã lưu trước đó rồi chuyển tiếp lỗi. Sau lỗi, cần tra cứu lại đối tượng từ repository (tham chiếu Java cũ có thể đã hết hiệu lực).

Nếu file đã có nhưng JSON sai cú pháp, thiếu mảng bắt buộc, không nhận diện được loại lớp con hoặc dữ liệu model không hợp lệ, `load()` báo `IllegalStateException`, không dùng dữ liệu đó và **chặn `save()` để giữ nguyên file hỏng**. File cấp gốc chỉ chấp nhận hai mảng `users` và `transactions`, tránh vô tình bỏ mất dữ liệu không hiểu khi lưu lại.

Kiểm thử ở `src/test/java/vn/edu/pxu/bank/repository/JsonRepositoryTest.java` và `ServiceTest.java`; chạy bằng `mvn test`. Các test dùng thư mục tạm, không tạo hoặc ghi đè `data/bank-data.json` trong dự án. Giữ dữ liệu demo giả lập, không lưu thông tin ngân hàng thật. Tài khoản thử GUI: `admin` / `Admin@123` (nhân viên) và `khach` / `Khach@123` (khách hàng). Đây là mật khẩu demo; dữ liệu lưu chỉ chứa giá trị băm PBKDF2.
