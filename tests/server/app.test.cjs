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
