"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const { KNOWN_ROBOTS, isKnownRobotId, getRequiredComponentCount } = require("../../server/config/known-robots");

// assets/js/data.js gán vào `window` cho trình duyệt, không export CommonJS.
// Test này đọc file thật và đánh giá trong một global giả để lấy dữ liệu gốc,
// không phải để coupling runtime — chỉ để phát hiện khi known-robots.js (tạm
// thời, xem docs/BACKEND_HANDOFF.md) bị lệch khỏi dữ liệu thật của Nhi.
function loadRealRobotModels() {
  const filePath = path.join(__dirname, "..", "..", "assets", "js", "data.js");
  const source = fs.readFileSync(filePath, "utf8");
  const fakeWindow = {};
  // eslint-disable-next-line no-new-func
  new Function("window", source)(fakeWindow);
  return fakeWindow.ROBOT_MODELS;
}

test("known-robots.js liệt kê đúng ba model ID thật, không hơn không kém", () => {
  const realModels = loadRealRobotModels();
  const realIds = realModels.map((model) => model.id).sort();
  const knownIds = Object.keys(KNOWN_ROBOTS).sort();
  assert.deepEqual(knownIds, realIds, "Nếu test này đỏ: assets/js/data.js đã thêm/bớt mẫu robot nhưng server/config/known-robots.js chưa được cập nhật theo (file tạm thời, xem docs/BACKEND_HANDOFF.md).");
});

test("known-robots.js khớp đúng số linh kiện bắt buộc thật của từng mẫu", () => {
  const realModels = loadRealRobotModels();
  for (const model of realModels) {
    assert.equal(
      getRequiredComponentCount(model.id),
      model.parts.length,
      `Số linh kiện bắt buộc của ${model.id} lệch giữa known-robots.js và data.js thật.`
    );
  }
});

test("isKnownRobotId từ chối ID không tồn tại", () => {
  assert.equal(isKnownRobotId("line-follower"), true);
  assert.equal(isKnownRobotId("robot-khong-ton-tai"), false);
  assert.equal(isKnownRobotId(""), false);
});

test("getRequiredComponentCount trả 0 cho ID không tồn tại thay vì ném lỗi", () => {
  assert.equal(getRequiredComponentCount("robot-khong-ton-tai"), 0);
});
