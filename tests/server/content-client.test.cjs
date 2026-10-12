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
    URLSearchParams,
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
  assert.strictEqual(await client.loadRobots(), models, "robot loading stays cached");
  assert.strictEqual(await client.loadComponents(), await client.loadComponents());

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
  assert.equal(await client.loadLibraryResources(), null);
  assert.equal(await client.loadLibraryResourcesPage(), null);
  assert.equal(await client.loadRobotOptions(), null);
  assert.equal(calls, 0);
});

test("content client rejects malformed API list responses", async () => {
  const client = loadClient({
    fetch: async () => jsonResponse({ data: "not-an-array" })
  });

  await assert.rejects(client.loadComponents(), /Dữ liệu API không hợp lệ/);
});

test("legacy library loader returns a cached array with its original limit", async () => {
  const requests = [];
  const client = loadClient({ fetch: async url => {
    requests.push(new URL(url).pathname + new URL(url).search);
    return jsonResponse({ data: [{ id: "guide" }], meta: { page: 1, limit: 100, total: 1 } });
  } });
  const resources = await client.loadLibraryResources();
  assert.ok(Array.isArray(resources));
  assert.equal(resources[0].id, "guide");
  assert.strictEqual(await client.loadLibraryResources(), resources);
  assert.deepEqual(requests, ["/api/library-resources?limit=100"]);
});

test("paged library loader sends encoded filters and preserves response metadata without caching pages", async () => {
  const requests = [];
  const payload = { data: [{ id: "guide" }], meta: { page: 2, limit: 12, total: 25 } };
  const client = loadClient({ fetch: async url => {
    requests.push(new URL(url));
    return jsonResponse(payload);
  } });
  assert.deepEqual(await client.loadLibraryResourcesPage({ page: 2, limit: 12, robotId: "bot & arm" }), payload);
  assert.equal(requests[0].pathname, "/api/library-resources");
  assert.equal(requests[0].searchParams.get("page"), "2");
  assert.equal(requests[0].searchParams.get("limit"), "12");
  assert.equal(requests[0].searchParams.get("robotId"), "bot & arm");
  await client.loadLibraryResourcesPage();
  assert.equal(requests.length, 2);
  assert.equal(requests[1].searchParams.get("page"), "1");
  assert.equal(requests[1].searchParams.get("limit"), "6");
  assert.equal(requests[1].searchParams.has("robotId"), false);
});

test("paged library and robot options propagate failures and reject malformed payloads", async () => {
  const failing = loadClient({ fetch: async () => jsonResponse({ error: { message: "offline" } }, 503) });
  await assert.rejects(failing.loadLibraryResourcesPage(), /offline/);
  await assert.rejects(failing.loadRobotOptions(), /offline/);
  const malformed = loadClient({ fetch: async () => jsonResponse({ data: [] }) });
  await assert.rejects(malformed.loadLibraryResourcesPage(), /Dữ liệu API không hợp lệ/);
});

test("paged requests do not overwrite the legacy library cache and robot options avoid enrichment requests", async () => {
  const requests = [];
  const client = loadClient({ fetch: async url => {
    const request = new URL(url).pathname + new URL(url).search;
    requests.push(request);
    return jsonResponse({ data: [{ id: request }], meta: { page: 1, limit: 100, total: 1 } });
  } });
  const legacy = await client.loadLibraryResources();
  const paged = await client.loadLibraryResourcesPage({ robotId: "mini-arm" });
  assert.notEqual(paged.data[0].id, legacy[0].id);
  assert.strictEqual(await client.loadLibraryResources(), legacy);
  const options = await client.loadRobotOptions();
  assert.equal(options[0].id, "/api/robots?limit=100");
  assert.deepEqual(requests, [
    "/api/library-resources?limit=100",
    "/api/library-resources?page=1&limit=6&robotId=mini-arm",
    "/api/robots?limit=100"
  ]);
});
