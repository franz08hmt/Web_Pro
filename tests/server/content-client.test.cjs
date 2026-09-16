"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const clientSource = fs.readFileSync(
  path.join(__dirname, "..", "..", "assets", "js", "content-api.js"),
  "utf8"
);

function loadClient({ port = "3000", fetch }) {
  const window = {
    location: {
      origin: `http://127.0.0.1:${port}`,
      port
    }
  };
  const document = {
    querySelector() { return null; }
  };

  vm.runInNewContext(clientSource, {
    AbortController,
    clearTimeout,
    console,
    document,
    fetch,
    setTimeout,
    URL,
    window
  });

  return window.RobotContentApi;
}

function jsonResponse(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    async json() { return body; }
  };
}

test("content client combines robots with component relations and ordered steps", async () => {
  const requests = [];
  const responses = new Map([
    ["/api/robots?limit=100", { data: [{ id: "demo-bot", name: "Robot demo", level: "Cơ bản", summary: "Mẫu kiểm thử", image: "/assets/demo.png", buildTime: "30 phút", mainSensor: "Không", skills: "API", wiring: [] }], meta: { page: 1, limit: 100, total: 1 } }],
    ["/api/components?limit=100", { data: [{ id: "demo-part", name: "Linh kiện demo", category: "Cảm biến", image: "/assets/part.png", description: "Mô tả", specs: {} }], meta: { page: 1, limit: 100, total: 1 } }],
    ["/api/robots/demo-bot/components", { data: [{ componentId: "demo-part", quantity: 2 }] }],
    ["/api/robots/demo-bot/steps?limit=100", { data: [{ id: "step-1", robotId: "demo-bot", stepOrder: 1, title: "Bước 1", instruction: "Lắp linh kiện.", illustration: {} }], meta: { page: 1, limit: 100, total: 1 } }]
  ]);
  const client = loadClient({
    fetch: async rawUrl => {
      const url = new URL(rawUrl);
      requests.push(url.pathname + url.search);
      return jsonResponse(responses.get(url.pathname + url.search));
    }
  });

  const models = await client.loadRobots();

  assert.equal(models.length, 1);
  assert.deepEqual(JSON.parse(JSON.stringify(models[0].parts)), [{ id: "demo-part", name: "Linh kiện demo", quantity: 2 }]);
  assert.deepEqual(JSON.parse(JSON.stringify(models[0].steps)), ["Lắp linh kiện."]);
  assert.deepEqual(requests, [
    "/api/robots?limit=100",
    "/api/components?limit=100",
    "/api/robots/demo-bot/components",
    "/api/robots/demo-bot/steps?limit=100"
  ]);
});

test("content client skips API calls on known static preview ports", async () => {
  let calls = 0;
  const client = loadClient({
    port: "63342",
    fetch: async () => { calls += 1; }
  });

  assert.equal(client.enabled, false);
  assert.equal(await client.loadComponents(), null);
  assert.equal(calls, 0);
});

test("content client rejects malformed API list responses", async () => {
  const client = loadClient({
    fetch: async () => jsonResponse({ data: "not-an-array" })
  });

  await assert.rejects(client.loadComponents(), /Dữ liệu API không hợp lệ/);
});
