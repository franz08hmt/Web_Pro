# Chuyển động dùng chung — Tài

Ngày: 2026-09-12. Bài nhóm, phát triển từ working tree Phase 2 hiện có
(HEAD 43d37b8); giữ nguyên các thay đổi chưa commit trước lượt làm việc này.

| Thành phần | Hành vi | Reduced motion |
| --- | --- | --- |
| Thanh vị trí cuộn | Thanh 3px màu accent, scaleX theo tỷ lệ cuộn thực tế | Ẩn |
| Chỉ báo sidebar | Chấm hover/focus riêng bên phải, 180ms cubic-bezier(.22,.61,.36,1); giữ dấu aria-current | Ẩn |
| Reveal khi nhận focus | Hiện phần tử và vùng cha ngay, không thay đổi thứ tự Tab | Hiện ngay |
| Tùy chọn motion trong phiên | Dừng observer và CSS animation khi bật reduce | Không chạy lại reveal cũ |

Giữ màu, typography, Poppins, Heroicons, layout và hover/press hiện tại.
Chỉ báo sidebar ẩn dưới 1024px. Thanh cuộn aria-hidden và pointer-events:none;
không gửi thông báo cuộn liên tục cho trình đọc màn hình. requestAnimationFrame
gộp cập nhật, ResizeObserver theo dõi thay đổi chiều cao sau lọc/render nội dung.
Fallback cập nhật khi scroll/resize. Không có vòng lặp chuyển động liên tục mới.

## Phân công và tích hợp

- Tài: ui.js, CSS motion, tài liệu giao diện dùng chung.
- Nhi: dữ liệu, nội dung mẫu/linh kiện/thư viện và API nội dung.
- Tuấn Anh: mô phỏng 2D, kéo-thả, checklist và lưu tiến độ nghiệp vụ.
- Giữ sáu mục điều hướng; không tạo thêm xưởng kéo-thả.
- Lớp giao diện không ghi vào trạng thái nghiệp vụ hoặc sửa dữ liệu mẫu.
- Nạp ui.js một lần; giữ cấu trúc nav hiện có. Nếu tương lai render lại toàn bộ
  nav sau tải trang, cần bổ sung khởi tạo lại trong đợt tích hợp đó.

## Kiểm chứng

- tests/ui-smoke.cjs nguyên bản PASS: 24 lượt trang/breakpoint, tìm kiếm,
  lọc 9/2/2/3/2, ba mẫu robot, checklist, hover và bàn phím.
- Axe với AXE_PATH: 0 violation ở 320px và 1440px trên sáu trang.
- Kiểm bổ sung ngoài repo: cuộn tới cuối trang, focus sidebar, đổi reduced-motion
  ngay trong phiên, file:// ở 320px khi tắt JavaScript.
- Ảnh kiểm tra: D:/Web-Pro/.codex-tmp/tai-motion-review.
- Chưa đo FPS bằng Performance trace; không báo số FPS ước đoán.

## Các mục không triển khai trong đợt này

Xưởng kéo-thả thuộc Tuấn Anh. Bỏ parallax, nghiêng 3D, nền động, đếm số và
skeleton để ưu tiên nội dung ổn định và tích hợp luồng thật. Không tạo trạng thái
tải giả cho dữ liệu cục bộ. Không thêm dependency/build step. Không commit/push.

## Bổ sung danh sách trang chủ

> Cập nhật 12/09/2026: hai danh sách homepage đã chuyển sang slider ảnh theo
> mẫu tham chiếu mới. Vùng slider không dùng reveal dọc; xem `PHOTO_EXPLORER.md`.

Theo yêu cầu tiếp theo: ba thẻ robot và bốn thẻ linh kiện có reveal 44px/scale .97,
500ms, so le 0/90/180/270ms. Chạy lại khi thẻ rời vùng quan sát rồi quay lại;
không chạy trên phần màn hình đầu. Không animate cả section cha cùng với card.
Focus làm thẻ hiện ngay và ngừng replay; reduced motion tắt transform.

Bốn ảnh linh kiện dùng lại từ assets/images/components (không tải ảnh ngoài,
không thay đổi nguồn ảnh trong data.js). Thông tin tóm tắt trên index.html đối chiếu:
- UNO: https://docs.arduino.cc/hardware/uno-rev3/
- HC-SR04: https://cdn.sparkfun.com/datasheets/Sensors/Proximity/HCSR04.pdf
- L298: https://www.st.com/en/motor-drivers/l298.html
- Nguyên lý hộp giảm tốc: https://www.pololu.com/docs/pdf/0j21/3pi.pdf

Thông tin động cơ chỉ là nguyên lý, không gán thông số một sản phẩm Pololu cho
động cơ minh họa của nhóm. Nguồn ngoài là liên kết đọc thêm; nội dung và ảnh
trang chủ vẫn được lưu cục bộ. Bản quyền ảnh kế thừa từ tài nguyên nhóm.

Kiểm tra: smoke 24 lượt PASS và axe 0 violation; kiểm riêng bảy thẻ, replay,
focus, chữ không mờ và reduced motion PASS. Ảnh tại .codex-tmp/home-showcase-review.
