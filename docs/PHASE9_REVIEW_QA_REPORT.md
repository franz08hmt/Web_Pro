# Đợt 9 — Sau khi hoàn tất: báo cáo kiểm chứng

## M0 — Khảo sát và nợ kiểm chứng, 14/10/2026

HEAD8da8b15 đồng bộ origin. Baseline173Node/97JUnit, JDK17 clean package xanh.
Sau sửa M0:174Node/97JUnit xanh; guard byte dùng WAR riêng ngoài target của IDE.
Giữ góc khay từ clone trước preview; khi chưa chọn slot, chỉ cộng góc hướng.
Test nguồn đã đỏ trước sửa. Chrome kiểm góc gốc bằng fixture scene tạm, không lưu.
Trên scene thật: kéo vật thể có di chuyển, camera không đổi; kéo vùng trống có
xoay camera;390px không tràn, nút Gắn thấy được, không pageerror.
Script ban đầu chọn vùng trống trùng lớp nhãn trên canvas; đã sửa cách chọn
bằng elementFromPoint, không sửa ứng dụng để ép kết quả kiểm tra.

ADMIN QA mới qua đăng ký: qa-phase9-admin-20261014@robotlab.test, id22;
chỉ tài khoản mới này được nâng admin và tăng session_version theo README.
Không ghi mật khẩu vào báo cáo/file. GET /api/admin/robots, /api/admin/steps,
/admin-stats đều200. Phiên QA mới35 của demo student, v2 IN_PROGRESS, kit đã chuẩn bị;
không sửa các phiên cũ30/31/33/34 hoặc spec đã công bố.
Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase9/m0-results.json, m0-390.png,
baseline/m0 test logs, protected-before.json và database-before.json (hash, không dữ liệu thô).
Không bịa số chương/slide: slide pptx có Word nhúng; tài liệu đợt9 ghi tên chủ đề
Servlet/request/response/JavaBean/EL/JSTL/JDBC/session/transaction khi chưa trích xác minh.

## M1 — Nhật ký transaction

ERD viết trước migration014, thêm một bảng và spec_id phục vụ FK cùng phiên/spec.
DDL014 đã chạy đúng DB local, schema_migrations có014, tổng44 bảng. Không backfill
hoặc sửa spec/session hoàn tất cũ. ACCEPTED và placement/version cùng commit;
REJECTED commit riêng dữ kiện từ chối trong cùng khóa phiên, không đổi mô hình/version.
Không ghi NOT_FOUND/UNCHANGED hoặc thao tác trên phiên COMPLETED đã khóa; cap300 từ chối.
Bean sự kiện giữ hướng trước, nhãn tiếng Việt và Date/formatter giờ Việt Nam.
Node mới đã đỏ trước implementation; cuối177Node/100JUnit, package JDK17 xanh.
Guard hồ sơ chỉ thêm014 vào danh sách migration của chức năng khác, vẫn chặn migration lạ.
Live phiên35: ATTACH ACCEPTED → ATTACH REJECTED sai hướng → DETACH ACCEPTED → ATTACH ACCEPTED.
Retry và NOT_FOUND không thêm dòng; từ chối giữ layout_version. Evidence m1-results.json.
## M2 — Bản vẽ theo phiên

AssemblyDrawing/DrawingItem dựng hai phép chiếu x,z và x,y, khối gợi hình theo loại.
JUnit đã đỏ do thiếu bean, sau đó103/103; Node177/177 xanh. Test10 mục/cả hai góc,
bounds SVG, khung rỗng/một vật thể không chia0, GROUP không đoán tọa độ.
Fragment core JSTL/c:out, tọa độ SVG chỉ từ getter số; có chú giải và giới hạn tỉ lệ.
BEANS map đăng ký hai bean; Jasper/render fragment sẽ kiểm trên trang thật ở M3.
## M3 — Trang hoàn thiện Servlet/JSP

/assembly-review đọc owner từ session, no-store, setAttribute và forward. Có ảnh/bản
vẽ, bước chuẩn/trình tự/thống kê lỗi, phiếu/in và quiz đúng model. Điểm quiz hiển thị
score/total đã lưu, best so tỷ lệ câu đúng; không thêm thang điểm. Chưa COMPLETED chỉ
có thông báo/tiếp tục; GROUP có ảnh/bước/phiếu/quiz, không đoán tọa độ hoặc replay.
Điều hướng hoàn tất chuyển sang review, URL phiếu cũ vẫn giữ. Các assertion đường
dẫn cũ cập nhật có chủ đích theo route mới, vẫn kiểm ../ và quyền/escape của phiếu.
Manifest/fixture bổ sung đúng một trang mới (36); không sửa hợp đồng form trang cũ.
Cookie/session/CSRF/owner và các file WIP không đổi. JS trên trang chỉ gọi window.print.

179Node/105JUnit/package xanh. Live51 ca:30/31/33/34, GROUP16, bốn phần,20 SVG items
cho hai góc nhìn/10 hàng chú giải, quiz model đúng, pending35, foreign404, guest302,
no-store,390px không tràn, không browsererror/asset404. Đã export PDF3 trang, render
và xem mọi trang. Runtime/PDF phát hiện fragment thiếu pageEncoding bị Jasper đọc
sai dấu; đã thêm UTF-8 riêng, test đỏ→xanh và kiểm lại HTML thật đúng chữ.
Print áp box-sizing trong namespace riêng; không mang header/menu/theme tối vào PDF.
Evidence m3-results.json, review-old-desktop/390.png, review-old.pdf và review-print-*.png.
M4–M6 chưa hoàn tất; replay phiên mới có nhật ký sẽ kiểm sau M5 hoàn tất.
