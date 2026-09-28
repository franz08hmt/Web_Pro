-- Run once on an existing database before deploying the updated WAR.
-- Ghi lại thời điểm phiên chuyển sang COMPLETED để phiếu kết quả hiển thị đúng
-- thời gian bắt đầu/kết thúc; NULL nghĩa là phiên chưa hoàn tất. Điều kiện
-- hoàn tất (đã lắp đủ linh kiện bắt buộc) được kiểm tra ở AssemblySessionDB
-- trước khi cột này được ghi, không dựa vào giá trị client gửi lên.
ALTER TABLE assembly_sessions ADD COLUMN completed_at TIMESTAMP NULL DEFAULT NULL AFTER status;

INSERT INTO schema_migrations (version)
VALUES ('005_assembly_completion');
