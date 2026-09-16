"use strict";

const mysql = require("mysql2/promise");

function createDatabasePool(config) {
  if (!config.isConfigured) return null;

  return mysql.createPool({
    host: config.host,
    port: config.port,
    user: config.user,
    password: config.password,
    database: config.name,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    enableKeepAlive: true,
    timezone: "Z",
    supportBigNumbers: true,
    bigNumberStrings: true
  });
}

async function getDatabaseStatus(pool) {
  if (!pool) return "not_configured";
  await pool.query("SELECT 1 AS connected");
  return "connected";
}

module.exports = { createDatabasePool, getDatabaseStatus };
