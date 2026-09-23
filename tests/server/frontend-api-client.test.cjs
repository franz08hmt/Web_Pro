"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const clientSource = fs.readFileSync(
  path.join(__dirname, "..", "..", "assets", "js", "api.js"),
  "utf8"
);

function response(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    async json() { return body; }
  };
}

function loadClient(fetch) {
  const window = { location: { origin: "http://localhost:8080" } };
  const document = { querySelector() { return null; } };
  vm.runInNewContext(clientSource, {
    AbortController,
    clearTimeout,
    document,
    fetch,
    setTimeout,
    URL,
    URLSearchParams,
    window
  });
  return window.RobotAssemblyApi;
}

test("auth client logs in, discovers CSRF and sends it on assembly mutations", async () => {
  const calls = [];
  const client = loadClient(async (rawUrl, options = {}) => {
    const url = new URL(rawUrl);
    calls.push({ path: url.pathname + url.search, options });
    if (url.pathname === "/api/auth/login") {
      return response({ data: { user: { id: "7", fullName: "Tài", role: "USER" } } });
    }
    if (url.pathname === "/api/auth/me") {
      return response({ data: { user: { id: "7", fullName: "Tài", role: "USER" } }, csrfToken: "csrf-demo" });
    }
    return response({ data: { id: "42", robotId: "line-follower", status: "PREPARING" } }, 201);
  });

  const user = await client.auth.login({ email: "tai@example.com", password: "Mat-khau-123" });
  const session = await client.assemblySessions.createOrResume("line-follower");

  assert.equal(user.fullName, "Tài");
  assert.equal(session.id, "42");
  assert.deepEqual(calls.map((call) => call.path), [
    "/api/auth/login",
    "/api/auth/me",
    "/api/assembly-sessions"
  ]);
  assert.equal(calls[0].options.credentials, "same-origin");
  assert.equal(JSON.parse(calls[0].options.body).email, "tai@example.com");
  assert.equal(calls[2].options.headers["X-CSRF-Token"], "csrf-demo");
  assert.deepEqual(JSON.parse(calls[2].options.body), { robotId: "line-follower" });
});

test("account edits, logout, and password change include CSRF", async () => {
  const calls = [];
  const client = loadClient(async (rawUrl, options = {}) => {
    const path = new URL(rawUrl).pathname;
    calls.push({ path, options });
    if (path === "/api/auth/me") {
      return response({ data: { user: { id: "7", role: "USER" } }, csrfToken: "csrf-demo" });
    }
    if (path === "/api/auth/profile") {
      return response({ data: { user: { id: "7", fullName: "Tên mới" } } });
    }
    if (path === "/api/auth/password" || path === "/api/auth/logout") return response(null, 204);
    throw new Error(`Unexpected URL ${path}`);
  });

  await client.auth.me();
  const updated = await client.auth.updateProfile("Tên mới");
  await client.auth.logout();
  await client.auth.me();
  await client.auth.changePassword("old-secret", "new-secret");

  assert.equal(updated.fullName, "Tên mới");
  assert.deepEqual(calls.map((call) => `${call.options.method || "GET"} ${call.path}`), [
    "GET /api/auth/me", "PATCH /api/auth/profile", "POST /api/auth/logout",
    "GET /api/auth/me", "PUT /api/auth/password"
  ]);
  for (const call of calls.filter((entry) => entry.options.method && entry.options.method !== "GET")) {
    assert.equal(call.options.headers["X-CSRF-Token"], "csrf-demo");
  }
});

test("assembly client exposes the complete session contract", async () => {
  const calls = [];
  const client = loadClient(async (rawUrl, options = {}) => {
    const url = new URL(rawUrl);
    calls.push({ path: url.pathname + url.search, method: options.method || "GET", body: options.body, headers: options.headers });
    if (url.pathname === "/api/auth/me") {
      return response({ data: { user: { id: "1" } }, csrfToken: "csrf-token" });
    }
    if (url.pathname === "/api/assembly-sessions" && (options.method || "GET") === "GET") {
      return response({ data: [{ id: "42" }], meta: { page: 2, pageSize: 10, total: 11 } });
    }
    return response({ data: { id: "42" } });
  });

  const list = await client.assemblySessions.list({ status: "READY", page: 2, pageSize: 10 });
  await client.assemblySessions.get("42");
  await client.assemblySessions.updateStatus("42", "IN_PROGRESS");
  await client.assemblySessions.setComponentPrepared("42", "arduino-uno", true);
  await client.assemblySessions.setStepStatus("42", "line-follower-step-1", "COMPLETED");
  await client.assemblySessions.setVisualPart("42", "arduino-uno", true);
  const reset = await client.assemblySessions.resetProgress("42");

  assert.deepEqual(JSON.parse(JSON.stringify(list)), {
    items: [{ id: "42" }],
    meta: { page: 2, pageSize: 10, total: 11 }
  });
  assert.deepEqual(calls.map((call) => `${call.method} ${call.path}`), [
    "GET /api/assembly-sessions?status=READY&page=2&pageSize=10",
    "GET /api/assembly-sessions/42",
    "GET /api/auth/me",
    "PATCH /api/assembly-sessions/42",
    "PUT /api/assembly-sessions/42/components/arduino-uno",
    "PUT /api/assembly-sessions/42/steps/line-follower-step-1",
    "PUT /api/assembly-sessions/42/visual-parts/arduino-uno",
    "DELETE /api/assembly-sessions/42/progress"
  ]);
  assert.equal(reset.id, "42");
  assert.equal(calls.at(-1).headers["X-CSRF-Token"], "csrf-token");
  assert.deepEqual(JSON.parse(calls.at(-2).body), { isAssembled: true });
});

test("client normalizes API and network failures for the UI", async () => {
  const apiClient = loadClient(async () => response({
    error: { code: "INVALID_CREDENTIALS", message: "Email hoặc mật khẩu không đúng." }
  }, 401));
  await assert.rejects(
    apiClient.auth.login({ email: "bad@example.com", password: "wrong-pass" }),
    error => error.code === "INVALID_CREDENTIALS" && error.status === 401
  );

  const networkClient = loadClient(async () => { throw new TypeError("fetch failed"); });
  await assert.rejects(
    networkClient.auth.me(),
    error => error.code === "NETWORK_ERROR" && error.status === 0 && /kết nối/.test(error.message)
  );
});
