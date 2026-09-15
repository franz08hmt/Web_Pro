"use strict";

/**
 * TẠM THỜI. Đây KHÔNG phải bảng `robots` thật của Nhi — bảng đó chưa tồn tại
 * (xem docs/BACKEND_HANDOFF.md, mục 1.2 #5). File này chỉ sao chép ba ID và số
 * lượng linh kiện bắt buộc đang có thật trong `assets/js/data.js` tại thời điểm
 * viết (2026-09-16), để `assembly-session.service.js` có một nguồn sự thật hợp
 * lệ mà không cần đoán tên bảng/cột MySQL và không cần import file frontend
 * (assets/js/data.js gán vào `window`, không export CommonJS).
 *
 * XÓA FILE NÀY khi RobotRepository thật (đọc từ MySQL) sẵn sàng — lúc đó
 * `robotId` hợp lệ và số linh kiện bắt buộc phải đến từ database, không phải
 * từ đây. Cho tới lúc đó, mọi thay đổi ở assets/js/data.js (thêm/bớt mẫu robot,
 * đổi số linh kiện) phải được phản ánh lại thủ công ở đây.
 */
const KNOWN_ROBOTS = Object.freeze({
  "line-follower": Object.freeze({ requiredComponentCount: 8 }),
  "obstacle-avoider": Object.freeze({ requiredComponentCount: 8 }),
  "mini-arm": Object.freeze({ requiredComponentCount: 5 })
});

function isKnownRobotId(robotId) {
  return Object.hasOwn(KNOWN_ROBOTS, robotId);
}

function getRequiredComponentCount(robotId) {
  const robot = KNOWN_ROBOTS[robotId];
  return robot ? robot.requiredComponentCount : 0;
}

module.exports = { KNOWN_ROBOTS, isKnownRobotId, getRequiredComponentCount };
