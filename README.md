# Robot Assembly Lab

Website tĩnh hướng dẫn người dùng chọn mẫu robot, kiểm tra linh kiện bắt buộc và thực hiện lắp ráp 2D theo từng bước.

## Phạm vi nghiệp vụ

- Người dùng chọn một mẫu robot trước khi lắp ráp.
- Mỗi mẫu có danh sách linh kiện và số lượng bắt buộc.
- Chỉ khi chọn đủ linh kiện, hướng dẫn lắp ráp mới chuyển sang trạng thái sẵn sàng.
- Các bước được trình bày theo đúng thứ tự.
- Giai đoạn đầu chưa sử dụng mô hình 3D, đăng nhập hoặc cơ sở dữ liệu.

## Thành viên và nhánh làm việc

- Tài: `feature/tai-layout-home` — trang chủ, bố cục và tích hợp.
- Thảo Nhi: `feature/nhi-content-structure` — nội dung mẫu robot, linh kiện và thư viện.
- Tuấn Anh: `feature/tuananh-operation-responsive` — luồng lắp ráp, JavaScript và responsive.

Không commit trực tiếp lên `main`. Mỗi thành viên làm trên nhánh riêng và tạo Pull Request để Tài kiểm tra trước khi merge.

## Cấu trúc thư mục

```text
robot-engine-website/
|-- index.html
|-- pages/
|   |-- mau-robot.html
|   |-- linh-kien.html
|   |-- lap-rap.html
|   |-- thu-vien.html
|   `-- thanh-vien.html
|-- assets/
|   |-- css/style.css
|   |-- js/data.js
|   |-- js/main.js
|   `-- images/
|-- .gitignore
`-- README.md
```

## Dữ liệu mẫu

`assets/js/data.js` hiện có ba mẫu: Robot dò đường, Robot tránh vật cản và Cánh tay robot mini. Mỗi mẫu gồm tên, mức độ, mô tả, danh sách linh kiện và các bước lắp ráp.

## Chạy project

Mở `index.html` bằng trình duyệt hoặc dùng tính năng preview của IntelliJ IDEA. Giai đoạn web tĩnh chưa cần Tomcat.
