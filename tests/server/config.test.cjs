"use strict";

const assert = require("node:assert/strict");
const test = require("node:test");
const { loadConfig } = require("../../server/config/env");

test("loadConfig supplies safe local defaults without enabling MySQL", () => {
  const config = loadConfig({});

  assert.equal(config.port, 3000);
  assert.deepEqual(config.corsOrigins, [
    "http://127.0.0.1:4173",
    "http://localhost:4173",
    "http://127.0.0.1:63342",
    "http://localhost:63342"
  ]);
  assert.equal(config.database.isConfigured, false);
});

test("loadConfig rejects an incomplete MySQL configuration", () => {
  assert.throws(
    () => loadConfig({ DB_HOST: "127.0.0.1", DB_USER: "root" }),
    { code: "CONFIGURATION_ERROR" }
  );
});

test("loadConfig rejects malformed server values at the boundary", () => {
  assert.throws(
    () => loadConfig({ PORT: "not-a-port" }),
    { code: "CONFIGURATION_ERROR" }
  );
  assert.throws(
    () => loadConfig({ CORS_ORIGINS: "https://example.test,not-a-url" }),
    { code: "CONFIGURATION_ERROR" }
  );
});

test("loadConfig accepts a rate limit window larger than a network port", () => {
  const config = loadConfig({ RATE_LIMIT_WINDOW_MS: "900000", RATE_LIMIT_MAX: "60" });

  assert.equal(config.rateLimit.windowMs, 900000);
  assert.equal(config.rateLimit.max, 60);
});
