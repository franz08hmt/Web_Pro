"use strict";

const assert = require("node:assert/strict");
const test = require("node:test");
const { getDatabaseStatus } = require("../../server/database/pool");

test("getDatabaseStatus does not query when MySQL is not configured", async () => {
  assert.equal(await getDatabaseStatus(null), "not_configured");
});

test("getDatabaseStatus uses a parameter-free connectivity query", async () => {
  const queries = [];
  const pool = { query: async (statement) => queries.push(statement) };

  assert.equal(await getDatabaseStatus(pool), "connected");
  assert.deepEqual(queries, ["SELECT 1 AS connected"]);
});
