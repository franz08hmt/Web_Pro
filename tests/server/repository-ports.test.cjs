"use strict";

const assert = require("node:assert/strict");
const test = require("node:test");
const { UserRepositoryPort } = require("../../server/repositories/ports/user-repository");
const { AuthSessionRepositoryPort } = require("../../server/repositories/ports/auth-session-repository");
const { AssemblySessionRepositoryPort } = require("../../server/repositories/ports/assembly-session-repository");

// Các port này là hợp đồng, không phải implementation. Test ở đây chỉ xác nhận
// rằng gọi trực tiếp lên port (quên cắm adapter) báo lỗi rõ ràng thay vì trả
// undefined im lặng — lỗi rõ ràng giúp phát hiện sớm khi service quên inject
// repository thật.

test("UserRepositoryPort ném NotImplementedError cho từng method khi chưa có adapter", async () => {
  const port = new UserRepositoryPort();
  await assert.rejects(() => port.findByEmail("a@example.com"), { name: "NotImplementedError" });
  await assert.rejects(() => port.findPublicById("id"), { name: "NotImplementedError" });
  await assert.rejects(() => port.create({}), { name: "NotImplementedError" });
});

test("AuthSessionRepositoryPort ném NotImplementedError cho từng method khi chưa có adapter", async () => {
  const port = new AuthSessionRepositoryPort();
  await assert.rejects(() => port.create({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.findActiveByTokenHash("hash"), { name: "NotImplementedError" });
  await assert.rejects(() => port.revokeByTokenHash("hash"), { name: "NotImplementedError" });
  await assert.rejects(() => port.revokeAllForUser("id"), { name: "NotImplementedError" });
});

test("AssemblySessionRepositoryPort ném NotImplementedError cho từng method khi chưa có adapter", async () => {
  const port = new AssemblySessionRepositoryPort();
  await assert.rejects(() => port.createOrResume({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.listByUser({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.findOwnedById({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.updateStatus({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.upsertComponentProgress({}), { name: "NotImplementedError" });
  await assert.rejects(() => port.upsertStepProgress({}), { name: "NotImplementedError" });
});
