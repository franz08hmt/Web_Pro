# Đợt10 — Xưởng thiết kế: bằng chứng theo mốc

## M0/M1 — 14/10/2026

HEAD b0bf22c, ahead8; baseline183Node/109JUnit, JDK17 clean package xanh.
Snapshot44 tập hàng cũ và10 file được bảo vệ ngoài Git trước khi ghi dữ liệu.
ERD viết trước code; M0 commit19e344c. M1 luật/reader/015/seed,186Node/110JUnit.
Node/JUnit mới đỏ trước implementation. Guard hồ sơ chỉ thêm015 vào whitelist
migration chức năng khác; vẫn chặn migration lạ, không thêm bảng hồ sơ.

DDL MySQL thật phát hiện FK CASCADE của placements không phù hợp cột sinh root.
Sửa riêng015/schema mới sang RESTRICT (9571e51); xóa design phải xóa placements
trước. Không DROP/ALTER bảng cũ. Seed chạy bằng SQL UTF-8 nguyên file, trong
transaction; thử lệnh SOURCE qua stdin chưa chạy được, không phải lỗi dữ liệu.
Cuối có55 bảng,015 hiện diện,11 role/11 mount, rule v1 PUBLISHED; seed chạy lại
không đổi hash rule/mount/fit. Chưa có seed hay DDL nào sửa spec v1/v2.

## M2 — Bộ linh kiện/form/nhập mua

187Node/111JUnit/package xanh. GET MyKit owner/no-store,13 linh kiện; POST hidden
CSRF/getParameterValues, validation bean, transaction owner và PRG. Kit cho phép0
xóa,1..10 lưu. Import SUM đơn CONFIRMED của owner, bean cap10, GREATEST chống cộng
trùng và giữ số đang có lớn hơn. Không cập nhật order, không giảm kit khi import.

Chrome/HTTP13 kiểm đạt: GET200, hai import302/không tăng lại lượng, quantity11→422
không ghi một phần, thiếu CSRF403, JS-off13 inputnumber,390/1440 không tràn/pageerror.
Demo builder/practice/student không có đơn CONFIRMED nên tạo USER QA23
qa-design-buyer-20261014@robotlab.test qua đăng ký, mua mô phỏng một Arduino bằng
luồng shop hiện có, order2. Stock sản phẩm giảm một theo checkout; ghi metadata
không bí mật ở qa-user.json. Không sửa tài khoản/kit/phiên mẫu cũ để ép QA đạt.

Shop chỉ thêm id anchor trên card để link kit/BOM tới sản phẩm có thật, cache-busting
shop.js cập nhật; không thay luật/cart/checkout. Account HTML chỉ thêm link kit.
Guard redirect được mở rộng kiểm cả @WebServlet và file HTML, loại query khi so
đường dẫn; vẫn chặn trang dang-nhap.html không tồn tại. Không giấu URL để né guard.

Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase10; baseline/test/package logs,
migration-results.json, m2-results.json, kit390/1440.png, hash WIP/DB và metadata QA.
Các mốc tiếp theo chưa được coi đã kiểm hoặc hoàn thành trong mục này.

## M3 — Tạo/quản lý/snapshot

188Node/112JUnit/package xanh;18 HTTP/SQL/Chrome kiểm đạt. KIT vượt số đang có422
trên form, không header/instances mới; LIBRARY và KIT hợp lệ302, snapshot10 vật thể.
Kit đổi bánh về0 không đổi hai instance bánh đã chọn. Đổi tên302, form cũ409;
USER khác GET/POST404. Xóa thiếu xác nhận422, có xác nhận302, cascade snapshot
chỉ của thiết kế đó. Tên chứa chuỗi script hiển thị dạng chữ;390/1440 không tràn.
User QA23 có design1 LIBRARY và design2 KIT; design QA3 dùng kiểm xóa đã xóa qua form.
Không dùng thiết kế riêng làm phiên assembly/nhiệm vụ; không sửa luật mẫu.
