# Quy ước REST API

Nền API đặt tại `/api`. Lớp route chỉ nhận HTTP và kiểm tra input; logic nghiệp vụ
sẽ nằm trong `server/services`, còn truy vấn MySQL sẽ nằm trong
`server/repositories`. Cách tách này giúp dữ liệu do Nhi phụ trách và mô phỏng 2D
của Tuấn Anh không phụ thuộc trực tiếp vào Express.

## Response thành công

```json
{
  "data": {}
}
```

Danh sách có thể bổ sung `meta` mà không thay đổi `data`. Tên field dùng
camelCase; endpoint là danh từ số nhiều, ví dụ `/api/robots`.

## Response lỗi

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ.",
    "requestId": "..."
  }
}
```

Không trả stack trace, mật khẩu, chuỗi kết nối hoặc thông tin nội bộ. Các mã lỗi
nền hiện có: `INVALID_JSON` (400), `CORS_ORIGIN_DENIED` (403), `NOT_FOUND`
(404), `RATE_LIMITED` (429), `INTERNAL_SERVER_ERROR` (500).

## Endpoint hiện có

| Method | Path | Mục đích |
| --- | --- | --- |
| `GET` | `/api/health` | Kiểm tra API và trạng thái pool MySQL. |

`GET /api/health` trả `database: "not_configured"` khi máy local chưa điền đủ
biến MySQL, và `"connected"` khi query `SELECT 1` thành công. Vì vậy giao diện
tĩnh vẫn chạy được trước khi Nhi cung cấp schema và seed data.

## Hợp đồng cần chốt trước tích hợp

- Nhi và Tài chốt schema/response cho `users`, `robots` và `assemblySessions`.
- Tuấn Anh chỉ gọi API phiên lắp ráp qua service/API client, không gọi MySQL hay
  thao tác trực tiếp vào state của Express.
- Mọi endpoint ghi dữ liệu sẽ bổ sung validation tại route và authorization trước
  khi gọi service.
