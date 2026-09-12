# Slider khám phá bằng ảnh

Hai vùng trên trang chủ dùng bố cục theo ảnh tham chiếu của người dùng:
ảnh nền phủ toàn khung, chữ bên trái, thẻ ảnh dọc bên phải, số thứ tự và
điều khiển phía dưới. Dùng lớp tối chuyển sắc để đảm bảo độ tương phản,
không dùng khối nội dung riêng bên dưới ảnh.

## Phạm vi

- `assets/js/explore.js`: trình chiếu, chọn thumbnail, bàn phím, vuốt và render tư liệu.
- `assets/js/explore-data.js`: bốn bản ghi tư liệu mới (hai robot, hai bo mạch),
  có nguồn thông tin, tác giả và giấy phép ảnh.
- `assets/css/explore.css`: CSS thuần, phạm vi `.photo-explorer` và `.reference-*`.
- `index.html`: 5 slide robot (3 thực hành + 2 tham khảo) và 6 slide linh kiện
  (4 nền tảng + 2 bo mạch mở rộng).
- `pages/mau-robot.html`, `pages/linh-kien.html`, `pages/thu-vien.html` hiển thị
  tư liệu mới từ cùng nguồn dữ liệu. Mẫu tham khảo liên kết tới tư liệu, không
  đi vào checklist của các mẫu thực hành.

Các mảng `ROBOT_MODELS`, `COMPONENTS_DATA`, mã `main.js`, `home.js` và CSS
dùng chung giữ nguyên trong thay đổi này. Hợp đồng dữ liệu lắp ráp không đổi.
`ui.js` bỏ reveal dọc cho vùng trình chiếu để hai chuyển động không chồng nhau.

## Tương tác

- Chọn thẻ ảnh nhỏ hoặc dùng nút trước/sau; quay vòng ở hai đầu.
- Phím trái/phải, Home/End khi focus nằm trên thumbnail.
- Vuốt ngang trên vùng ảnh; dải thumbnail dùng cuộn ngang tự nhiên.
- Tự phát là lựa chọn bật bằng nút, khoảng cách 6 giây; dừng khi chọn mục bằng tay.
  Tạm dừng khi hover, focus bên trong, ra khỏi màn hình hoặc ẩn tab.
  Bấm Tự phát chủ động cho phép chạy dù nút còn focus.
- `prefers-reduced-motion` bỏ chuyển cảnh và vô hiệu hóa tự phát.
- Tải trước ảnh đích; lỗi ảnh giữ lại nội dung hiện tại và thông báo đọc màn hình.
- Khi JavaScript tắt, bốn thẻ linh kiện tĩnh vẫn có nội dung; tư liệu động có
  thông báo fallback. Không gọi API ngoài để hiển thị nội dung.

## Ảnh và kiểm tra

Bốn WebP mới tổng cộng 416716 byte. Nguồn và giấy phép ở
`assets/images/reference/CREDITS.md`, đồng thời hiển thị trong giao diện.

Bộ smoke hiện có: 24 lượt trang/breakpoint, tìm kiếm, bộ lọc và checklist PASS;
axe WCAG 2A/AA và 2.1AA không ghi nhận violation ở 320/1440 px.
`tests/explore.cjs`: tự phát, tạm dừng khi focus, dừng khi chọn thủ công,
giảm chuyển động, bàn phím, quay vòng, vuốt cảm ứng và fallback khi ảnh lỗi PASS.
Kiểm bổ sung: liên kết tư liệu và ảnh tải cục bộ.
Ảnh kiểm tra lưu ngoài repo tại `D:/Web-Pro/.codex-tmp/explorer-*.png`.
