# Giao diện Robot Assembly Lab — Phase 1

## Hướng thiết kế

Tham chiếu do người dùng cung cấp: bảng điều khiển công nghiệp (ảnh 1), thẻ kính với ảnh lớn (ảnh 2), nền graphite với điểm nhấn cam và lưới kỹ thuật (ảnh 3).

- Trang chủ/danh mục/thư viện: nền tối, ảnh robot sẵn có và thẻ viền kính. Thư viện dùng lớp nền tối sau chữ để giữ độ tương phản trên ảnh.
- Lắp ráp: mô hình bên trái, chọn mẫu và checklist bên phải; hướng dẫn theo thứ tự ở dưới. Chuyển thành một cột trên điện thoại.
- Linh kiện/thành viên: nền sáng và thẻ rõ nội dung, cùng hệ thống điều hướng và màu cam.
- Điều hướng desktop là sidebar; tablet/điện thoại chuyển sang danh sách trên đầu trang. Không cần JavaScript để mở menu.

## Quyết định kỹ thuật

Giữ HTML/CSS/JavaScript thuần theo xác nhận của người dùng. Quy tắc LogiRoute/Tailwind trong ../.agents/AGENTS.md không áp dụng cho dự án này. CSS dùng biến màu và các breakpoint 480/767/1024/1200px. Poppins được lưu local với bốn weight 400/500/600/700; SVG Heroicons dùng inline, có kích thước giới hạn và aria-hidden. Không cần npm build để chạy website.

Giữ nguyên assets/js/data.js, ID của ba mẫu robot, các selector tìm kiếm/lọc/checklist và công thức tiến độ. Sửa template ảnh trong home.js và main.js để dùng figure, alt, kích thước và lazy-loading; thêm phần tử ảnh preview để tận dụng logic chọn mẫu đã có. Bộ lọc components-filter.js không đổi.

## Ngữ nghĩa, SEO và AEO

- Mỗi trang có một h1, tiêu đề/mô tả riêng, main và điều hướng có tên rõ ràng.
- Skip link tới main; focus bàn phím hiển thị; bộ lọc có aria-pressed; ô tìm kiếm nằm trong landmark search.
- Select, checklist và progress có nhãn; thay đổi tiến độ được thông báo qua role=status.
- Ảnh dùng alt, width/height và figure. Ảnh hero ưu tiên tải; ảnh danh mục tải lazy.
- FAQ trang chủ có nội dung hiển thị trùng với FAQPage JSON-LD. Đây là mô tả nội dung, không phải cam kết được công cụ tìm kiếm hiển thị rich results.
- Lắp ráp và danh mục có thông báo khi JavaScript bị tắt. Danh mục động vẫn cần JavaScript để tìm kiếm và chọn mẫu.
- Tôn trọng prefers-reduced-motion. Không dùng dữ liệu giám sát máy móc, đánh giá, giá bán hoặc tính năng 2D giả.

## Kiểm tra

Khởi động trong thư mục dự án:

```powershell
python -m http.server 4173 --bind 127.0.0.1
```

Trong terminal khác, dùng môi trường có Playwright và Chrome:

```powershell
node tests/ui-smoke.cjs
```

Nếu Playwright nằm trong môi trường công cụ riêng, đặt NODE_PATH trỏ đến thư mục node_modules đó. Có thể đặt BASE_URL để kiểm thử server khác, ARTIFACT_DIR để lưu screenshot và AXE_PATH trỏ đến axe.min.js local để kiểm tra WCAG tự động. Không đưa các gói kiểm thử vào runtime của website.

Đã chạy trên Chrome headless:

- Sáu trang ở 320/768/1024/1440px: không tràn ngang; heading đúng cấp, ID không trùng, ảnh/font tải được.
- Bộ lọc linh kiện: 9 tổng, 2 bộ điều khiển, 2 cảm biến, 3 chuyển động, 2 nguồn/kết nối.
- Tìm kiếm có kết quả và trạng thái rỗng; liên kết chọn mẫu giữ đúng mô hình qua query string.
- Cả ba mô hình: preview đúng, tiến độ 0–100%, bỏ chọn trở lại trạng thái chưa đủ.
- Tab tới skip link và Enter đưa focus vào main.
- axe-core 4.10.3: không có violation trong nhóm WCAG 2 A/AA và 2.1 AA tại 320/1440px. Kiểm tra tự động không thay thế đánh giá đầy đủ bằng screen reader.
- Đã kiểm tra ảnh chụp desktop/mobile, cú pháp JavaScript và parse HTML5 cho cả sáu trang.

## Nguồn tài nguyên

- [Heroicons 2.2.0](https://github.com/tailwindlabs/heroicons/tree/v2.2.0): MIT, giấy phép ở assets/icons/LICENSE.txt.
- [Poppins](https://fonts.google.com/specimen/Poppins): SIL OFL, giấy phép ở assets/fonts/OFL.txt.
- Hình robot và linh kiện tái sử dụng từ assets/images của dự án.

Chỉ áp dụng trên nhánh feature/ui-enhancement-phase-1. Các bước full-stack và lắp ráp 2D tiếp tục theo phân công của nhóm.
