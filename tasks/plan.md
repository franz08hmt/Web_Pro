# Thiết kế UI phòng thực hành robot
## Phạm vi đã thống nhất
Sáu trang hiện có, CSS thuần, Poppins và Heroicons 2.2.0. Áp dụng quy tắc engineering phù hợp trong ../.agents; người dùng đã loại trừ các quy định LogiRoute/Tailwind. Giữ dữ liệu và nghiệp vụ hiện tại.
## Thiết kế
- Khung điều hướng graphite; điểm nhấn cam, đường viền mảnh và nền lưới theo ảnh 1/3.
- Trang chủ, danh mục robot và thư viện: ảnh có chiều sâu, thẻ kính theo ảnh 2; giữ độ tương phản và không chồng nội dung khi màn hình nhỏ.
- Lắp ráp: khu vực xem robot, bảng lựa chọn và tiến độ theo ảnh 1.
- Linh kiện/thành viên: nền sáng, hệ thống typography và khoảng cách đồng bộ.
## Cấu trúc và quy ước
HTML5 tại index.html, pages/; CSS tại assets/css/style.css; JavaScript hiện có tại assets/js.
Heroicons SVG và font lưu local để demo không phụ thuộc CDN. Không bổ sung framework/build pipeline.
Mỗi trang một h1; section có tiêu đề hoặc accessible name; form có label; figure/figcaption cho ảnh; chỉ dùng div để nhóm layout không có ngữ nghĩa riêng.
## Thứ tự
1. Tài nguyên và khung giao diện chung; kiểm tra trang chủ.
2. Danh mục/thư viện/linh kiện/thành viên; kiểm tra bộ lọc.
3. Bảng lắp ráp; kiểm tra chọn mẫu, ảnh và 0–100% tiến độ.
4. Kiểm tra mọi trang trên 320, 768, 1024, 1440px; kiểm tra keyboard, console, ảnh, liên kết và heading.
## Lệnh
- Chạy local: python -m http.server 4173 --bind 127.0.0.1
- JavaScript: node --check assets/js/main.js
- Kiểm tra trình duyệt: node tests/ui-smoke.cjs (Playwright qua NODE_PATH hoặc cài sẵn ở máy; BASE_URL mặc định http://127.0.0.1:4173)
- Diff: git diff --check
## Tiêu chí
Không tràn ngang ở các kích thước kiểm tra; tài nguyên ảnh/font/icon tải thành công; bộ lọc trả 9/2/2/3/2; tìm kiếm có trạng thái rỗng; chọn mẫu qua URL và select đúng; progress cập nhật và hướng dẫn sẵn sàng khi đủ linh kiện.
## SEO / AEO
Nội dung mô tả trực tiếp mục đích trang; heading tuần tự; liên kết thật; bổ sung FAQ hiển thị với structured data tương ứng ở trang chủ. Không tạo giá, đánh giá, canonical hoặc dữ liệu vận hành giả.
## Ranh giới
Luôn giữ nguyên tên/ID mô hình, selector logic hiện có; không triển khai database hoặc chức năng 2D mới trong đợt UI. Main chỉ cập nhật khi người dùng yêu cầu.
