"use strict";

const assert = require("node:assert/strict");
const http = require("node:http");
const test = require("node:test");
const { createApp } = require("../../server/app");
const { loadConfig } = require("../../server/config/env");

async function withServer(run) {
  await withServerOptions({}, run);
}

async function withServerOptions(options, run) {
  const app = createApp({
    config: loadConfig({ NODE_ENV: "test" }),
    database: null,
    logger: { info() {}, error() {} },
    ...options
  });
  const server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  try {
    await run(`http://127.0.0.1:${port}`);
  } finally {
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
}

function createContentRepository() {
  const component = {
    id: "battery-holder",
    name: "Hộp pin AA 4",
    category: "Nguồn điện",
    image: "/assets/images/assembly/battery-holder.png",
    description: "Cấp nguồn cho robot.",
    specs: { "Điện áp": "6V" }
  };

  return {
    async list(kind) {
      const data = kind === "components" ? [component] : [];
      return { data, meta: { page: 1, limit: 20, total: data.length } };
    },
    async get(kind, id) {
      if (kind === "components" && id === component.id) return component;
      throw Object.assign(new Error("Không tìm thấy nội dung."), { status: 404, code: "NOT_FOUND" });
    },
    async parts() { return []; },
    async create() { throw new Error("Không dùng trong test đọc công khai."); },
    async update() { throw new Error("Không dùng trong test đọc công khai."); },
    async remove() { throw new Error("Không dùng trong test đọc công khai."); },
    async setPart() { throw new Error("Không dùng trong test đọc công khai."); },
    async removePart() { throw new Error("Không dùng trong test đọc công khai."); }
  };
}

test("GET /api/health identifies the API without requiring MySQL locally", async () => {
  await withServer(async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/health`);
    const body = await response.json();

    assert.equal(response.status, 200);
    assert.equal(response.headers.get("x-content-type-options"), "nosniff");
    assert.deepEqual(body.data, {
      service: "robot-assembly-lab-api",
      status: "ok",
      database: "not_configured"
    });
  });
});

test("GET /api/health reports a connected database through the injected pool", async () => {
  await withServerOptions({ database: { query: async () => [[{ connected: 1 }]] } }, async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/health`);
    assert.equal(response.status, 200);
    assert.equal((await response.json()).data.database, "connected");
  });
});

test("GET /api/health hides database failures from API consumers", async () => {
  await withServerOptions({ database: { query: async () => { throw new Error("database password failed"); } } }, async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/health`);
    const body = await response.json();
    assert.equal(response.status, 500);
    assert.equal(body.error.code, "INTERNAL_SERVER_ERROR");
    assert.doesNotMatch(body.error.message, /password/i);
  });
});

test("API accepts configured origins and rejects unconfigured origins", async () => {
  await withServer(async (baseUrl) => {
    const permitted = await fetch(`${baseUrl}/api/health`, {
      headers: { Origin: "http://localhost:4173" }
    });
    assert.equal(permitted.headers.get("access-control-allow-origin"), "http://localhost:4173");

    const ideaPreview = await fetch(`${baseUrl}/api/health`, {
      headers: { Origin: "http://localhost:63342" }
    });
    assert.equal(ideaPreview.headers.get("access-control-allow-origin"), "http://localhost:63342");

    const blocked = await fetch(`${baseUrl}/api/health`, {
      headers: { Origin: "https://untrusted.example" }
    });
    const body = await blocked.json();
    assert.equal(blocked.status, 403);
    assert.equal(body.error.code, "CORS_ORIGIN_DENIED");
  });
});

test("API gives structured errors for malformed JSON and unknown paths", async () => {
  await withServer(async (baseUrl) => {
    const malformed = await fetch(`${baseUrl}/api/health`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: "{"
    });
    assert.equal(malformed.status, 400);
    assert.equal((await malformed.json()).error.code, "INVALID_JSON");

    const missing = await fetch(`${baseUrl}/api/does-not-exist`);
    assert.equal(missing.status, 404);
    assert.equal((await missing.json()).error.code, "NOT_FOUND");
  });
});

test("GET /api/components exposes Nhi content through the shared server", async () => {
  await withServerOptions({ contentRepository: createContentRepository() }, async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/components`);
    const body = await response.json();

    assert.equal(response.status, 200);
    assert.equal(body.meta.total, 1);
    assert.equal(body.data[0].name, "Hộp pin AA 4");
  });
});

test("content API reports a service error when MySQL is not configured", async () => {
  await withServer(async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/components`);
    const body = await response.json();

    assert.equal(response.status, 503);
    assert.equal(body.error.code, "DATABASE_NOT_CONFIGURED");
  });
});

test("admin content routes stay locked until production authentication is connected", async () => {
  await withServerOptions({ contentRepository: createContentRepository() }, async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/admin/components`);
    const body = await response.json();

    assert.equal(response.status, 401);
    assert.equal(body.error.code, "AUTH_REQUIRED");
  });
});

test("shared server serves the website on the same origin as the API", async () => {
  await withServer(async (baseUrl) => {
    const response = await fetch(`${baseUrl}/`);
    const html = await response.text();

    assert.equal(response.status, 200);
    assert.match(response.headers.get("content-type"), /text\/html/);
    assert.match(html, /Robot Assembly Lab/);
  });
});

test("API returns 413 for JSON bodies larger than the content contract", async () => {
  await withServerOptions({ contentRepository: createContentRepository() }, async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/admin/components`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ description: "x".repeat(270 * 1024) })
    });
    const body = await response.json();

    assert.equal(response.status, 413);
    assert.equal(body.error.code, "PAYLOAD_TOO_LARGE");
  });
});
